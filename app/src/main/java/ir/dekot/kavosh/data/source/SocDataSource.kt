package ir.dekot.kavosh.data.source

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.data.model.components.GpuInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * منبع داده برای اطلاعات SOC (پردازنده مرکزی و گرافیکی).
 */
@Singleton
class SocDataSource @Inject constructor() {

    /**
     * اطلاعات پردازنده مرکزی (CPU) را از فایل‌های سیستمی می‌خواند.
     * این تابع با رویکرد Functional بازنویسی شده تا خواناتر و بهینه‌تر باشد.
     */
    fun getCpuInfo(): CpuInfo {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val architecture = System.getProperty("os.arch") ?: "نامشخص"
        val model = getCpuModel()

        // با استفاده از map، اطلاعات هر هسته را به صورت یک Pair(رشته محدوده، ماکسیمم فرکانس) استخراج می‌کنیم
        val coreInfoList = (0 until coreCount).map { i ->
            try {
                val minFreq = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq").readText().trim().toLong()
                val maxFreq = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim().toLong()
                "هسته $i: ${minFreq / 1000} - ${maxFreq / 1000} MHz" to maxFreq
            } catch (_: Exception) {
                // در صورت خطا، یک مقدار پیش‌فرض برمی‌گردانیم
                "هسته $i: نامشخص" to 0L
            }
        }

        // لیست‌های نهایی را از لیست بالا استخراج می‌کنیم
        val clockSpeedRanges = coreInfoList.map { it.first }
        val maxFrequenciesKhz = coreInfoList.map { it.second }

        // توپولوژی را از لیست فرکانس‌های ماکسیمم می‌سازیم
        val topologyString = maxFrequenciesKhz
            .filter { it > 0 } // هسته‌هایی که اطلاعاتشان خوانده نشده را نادیده می‌گیریم
            .groupingBy { it } // بر اساس فرکانس گروه‌بندی می‌کنیم
            .eachCount()       // تعداد هر گروه را می‌شماریم
            .entries
            .sortedByDescending { it.key } // بر اساس فرکانس مرتب می‌کنیم
            .joinToString(" + ") { (maxFreq, count) ->
                "${count}x @ ${"%.2f".format(maxFreq / 1000000.0)} GHz"
            }

        return CpuInfo(
            model = model,
            architecture = architecture,
            coreCount = coreCount,
            process = "نامشخص",
            topology = if (topologyString.isNotBlank()) topologyString else "نامشخص",
            clockSpeedRanges = clockSpeedRanges,
            maxFrequenciesKhz = maxFrequenciesKhz,
            liveFrequencies = List(coreCount) { "..." }
        )
    }

    /**
     * مدل پردازنده را برمی‌گرداند.
     */
    private fun getCpuModel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.let {
                if (it.isNotBlank()) return it
            }
        }
        return Build.HARDWARE.takeIf { it.isNotBlank() }
            ?: Build.BOARD.takeIf { it.isNotBlank() }
            ?: "نامشخص"
    }

    /**
     * فرکانس لحظه‌ای هسته‌های CPU را از فایل‌های سیستمی می‌خواند.
     */
    fun getLiveCpuFrequencies(): List<String> {
        val freqs = mutableListOf<String>()
        for (i in 0 until Runtime.getRuntime().availableProcessors()) {
            try {
                val freqKhz = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq").readText().trim().toLong()
                freqs.add("${freqKhz / 1000} MHz")
            } catch (_: Exception) {
                freqs.add("خوابیده")
            }
        }
        return freqs
    }

    /**
     * درصد بار پردازشی GPU را از فایل سیستمی (در صورت وجود) می‌خواند.
     */
    fun getGpuLoadPercentage(): Int? {
        val kgslPath = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"
        return try {
            val file = File(kgslPath)
            if (file.exists() && file.canRead()) {
                file.readText().trim().substringBefore(" ").toIntOrNull()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
    /**
     * اطلاعات GPU را استخراج می‌کند.
     * @param activity این متد حالا Activity را به عنوان پارامتر ورودی می‌گیرد.
     */
    @Suppress("DEPRECATION")
    suspend fun getGpuInfo(activity: Activity): GpuInfo {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        val deferred = CompletableDeferred<GpuInfo>()

        withContext(Dispatchers.Main) {
            val glSurfaceView = GLSurfaceView(activity).apply {
                setRenderer(object : GLSurfaceView.Renderer {
                    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                        val vendor = gl?.glGetString(GL10.GL_VENDOR) ?: "نامشخص"
                        val renderer = gl?.glGetString(GL10.GL_RENDERER) ?: "نامشخص"
                        deferred.complete(GpuInfo(vendor = vendor, model = renderer))
                        rootView.post {
                            rootView.removeView(this@apply)
                        }
                    }
                    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
                    override fun onDrawFrame(gl: GL10?) {}
                })
                layoutParams = FrameLayout.LayoutParams(1, 1)
            }
            rootView.addView(glSurfaceView)
        }
        return deferred.await()
    }
}