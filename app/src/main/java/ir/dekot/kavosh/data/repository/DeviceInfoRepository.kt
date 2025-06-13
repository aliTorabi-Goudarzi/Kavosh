package ir.dekot.kavosh.data.repository

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow
import android.opengl.GLSurfaceView
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.content.SharedPreferences
import android.os.HardwarePropertiesManager
import androidx.core.content.edit
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.SensorInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo


// ریپازیتوری تنها منبع حقیقت (Single Source of Truth) برای داده‌های برنامه است.
// این کلاس مسئولیت خواندن اطلاعات سخت‌افزاری و نرم‌افزاری از سیستم عامل را بر عهده دارد.

@Suppress("DEPRECATION")
class DeviceInfoRepository(private val context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // این متد را به طور کامل جایگزین نسخه قبلی کنید
    // این متد را به طور کامل جایگزین نسخه قبلی کنید
    fun getCpuInfo(): CpuInfo {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val architecture = System.getProperty("os.arch") ?: "نامشخص"
        val model = getCpuModel()

        // --- محاسبه توپولوژی و بازه سرعت ---
        val ranges = mutableListOf<String>()
        val maxFreqMap = mutableMapOf<Long, Int>() // Map<MaxFreq, CoreCount>

        for (i in 0 until coreCount) {
            try {
                val minFreq = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq").readText().trim().toLong()
                val maxFreq = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim().toLong()

                ranges.add("هسته $i: ${minFreq/1000} - ${maxFreq/1000} MHz")
                maxFreqMap[maxFreq] = (maxFreqMap[maxFreq] ?: 0) + 1
            } catch (_: Exception) {
                // اگر برای یک هسته ممکن نبود، از آن عبور می‌کنیم
                continue
            }
        }

        // ساختن رشته توپولوژی بر اساس فرکانس‌های ماکسیمم
        val topologyString = maxFreqMap.entries
            .sortedByDescending { it.key } // مرتب‌سازی بر اساس فرکانس (از بزرگ به کوچک)
            .joinToString(" + ") { (maxFreq, count) ->
                "${count}x @ ${"%.2f".format(maxFreq / 1000000.0)} GHz"
            }

        return CpuInfo(
            model = model,
            architecture = architecture,
            coreCount = coreCount,
            process = "نامشخص", // لیتوگرافی از طریق API استاندارد در دسترس نیست
            topology = if (topologyString.isNotBlank()) topologyString else "نامشخص",
            clockSpeedRanges = ranges,
            liveFrequencies = List(coreCount) { "..." } // مقدار اولیه برای فرکانس‌های زنده
        )
    }

    // این متد را برای خواندن فرکانس‌های زنده اضافه کنید
    fun getLiveCpuFrequencies(): List<String> {
        val freqs = mutableListOf<String>()
        for (i in 0 until Runtime.getRuntime().availableProcessors()) {
            try {
                val freqKhz = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq").readText().trim().toLong()
                freqs.add("${freqKhz / 1000} MHz")
            } catch (_: Exception) {
                freqs.add("خوابیده") // هسته ممکن است در حالت deep sleep باشد
            }
        }
        return freqs
    }

    // این متد را برای خواندن لود GPU اضافه کنید (Best-Effort)
    fun getGpuLoadPercentage(): Int? {
        val kgslPath = "/sys/class/kgsl/gisel-3d0/gpu_busy_percentage"
        return try {
            val file = File(kgslPath)
            if (file.exists() && file.canRead()) {
                file.readText().trim().substringBefore(" ").toIntOrNull()
            } else {
                null // File does not exist or is not readable
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getCpuModel(): String {
        // Try to use Build.SOC_MODEL if available (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.let {
                if (it.isNotBlank()) return it
            }
        }
        // Fallback to Build.HARDWARE or Build.BOARD
        return Build.HARDWARE.takeIf { it.isNotBlank() }
            ?: Build.BOARD.takeIf { it.isNotBlank() }
            ?: "نامشخص"
    }

    // این متد با ایجاد یک سطح OpenGL به صورت موقت، اطلاعات GPU را می‌خواند.
    suspend fun getGpuInfo(): GpuInfo {
        // برای انجام عملیات مربوط به UI (ایجاد View) به ترد اصلی نیاز داریم.
        val activity = context as? Activity ?: return GpuInfo()
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)

        // از CompletableDeferred برای منتظر ماندن نتیجه از یک ترد دیگر استفاده می‌کنیم.
        val deferred = CompletableDeferred<GpuInfo>()

        withContext(Dispatchers.Main) {
            val glSurfaceView = GLSurfaceView(context).apply {
                // یک رندرکننده برای دسترسی به محیط OpenGL تنظیم می‌کنیم.
                setRenderer(object : GLSurfaceView.Renderer {
                    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                        val vendor = gl?.glGetString(GL10.GL_VENDOR) ?: "نامشخص"
                        val renderer = gl?.glGetString(GL10.GL_RENDERER) ?: "نامشخص"

                        // وقتی اطلاعات دریافت شد، آن را به deferred پاس می‌دهیم.
                        deferred.complete(GpuInfo(vendor = vendor, model = renderer))

                        // بعد از انجام کار، View موقت را از صفحه حذف می‌کنیم.
                        rootView.post {
                            rootView.removeView(this@apply)
                        }
                    }

                    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
                    override fun onDrawFrame(gl: GL10?) {}
                })

                // برای اینکه View دیده نشود، اندازه‌اش را ۱ در ۱ پیکسل می‌کنیم.
                layoutParams = FrameLayout.LayoutParams(1, 1)
            }
            // View موقت را به صفحه اضافه می‌کنیم تا رندرکننده فعال شود.
            rootView.addView(glSurfaceView)
        }

        // منتظر می‌مانیم تا اطلاعات از رندرکننده برگردد.
        return deferred.await()
    }

    fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        return RamInfo(
            total = formatSize(totalRam),
            available = formatSize(availableRam)
        )
    }

    fun getStorageInfo(): StorageInfo {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = internalStat.blockCountLong * internalStat.blockSizeLong
        val availableBytes = internalStat.availableBlocksLong * internalStat.blockSizeLong
        return StorageInfo(
            total = formatSize(totalBytes),
            available = formatSize(availableBytes)
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisplayInfo(): DisplayInfo {
        val displayMetrics = DisplayMetrics()
        (context as? Activity)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val refreshRate = (context as? Activity)?.display?.refreshRate ?: 60.0f
        return DisplayInfo(
            resolution = "${displayMetrics.heightPixels}x${displayMetrics.widthPixels}",
            density = "${displayMetrics.densityDpi} dpi",
            refreshRate = "${DecimalFormat("#.##").format(refreshRate)} Hz"
        )
    }

    fun getSystemInfo(): SystemInfo {
        return SystemInfo(
            androidVersion = Build.VERSION.RELEASE,
            sdkLevel = Build.VERSION.SDK_INT.toString(),
            buildNumber = Build.DISPLAY,
            isRooted = isDeviceRooted()
        )
    }

    fun getSensorInfo(): List<SensorInfo> {
        return sensorManager.getSensorList(Sensor.TYPE_ALL).map {
            SensorInfo(name = it.name, vendor = it.vendor)
        }
    }

    fun getBatteryInfo(intent: Intent): BatteryInfo {
        return BatteryInfo(
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
            health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "خوب"
                BatteryManager.BATTERY_HEALTH_DEAD -> "خراب"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "بسیار گرم"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "ولتاژ بالا"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "خطای نامشخص"
                else -> "نامشخص"
            },
            status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "در حال شارژ"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "در حال تخلیه"
                BatteryManager.BATTERY_STATUS_FULL -> "کامل"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "عدم شارژ"
                else -> "نامشخص"
            },
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "نامشخص",
            temperature = "${intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f} °C",
            voltage = "${intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0f} V"
        )
    }
    // متد زیر را داخل کلاس DeviceInfoRepository اضافه کنید
    /**
     * اطلاعات دمای دستگاه را با استفاده از API رسمی HardwarePropertiesManager دریافت می‌کند.
     * این روش پیشنهادی و امن گوگل است.
     */
    fun getThermalInfo(): List<ThermalInfo> {
        // این API از اندروید 7 (API 24) به بعد در دسترس است.

        val thermalList = mutableListOf<ThermalInfo>()
        // چون کلاس ما از قبل context را دارد، از همان استفاده می‌کنیم
        val hardwareService = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as? HardwarePropertiesManager
            ?: return emptyList() // اگر سرویس در دسترس نبود، لیست خالی برمی‌گردانیم

        // لیست سنسورهایی که می‌خواهیم دمای آن‌ها را بپرسیم
        val sensorTypes = intArrayOf(
            HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN // دمای سطح بدنه دستگاه
        )

        for (sensorType in sensorTypes) {
            try {
                // دریافت دمای فعلی برای سنسور مورد نظر
                val temperatures = hardwareService.getDeviceTemperatures(
                    sensorType,
                    HardwarePropertiesManager.TEMPERATURE_CURRENT
                )

                // فقط دماهای معتبر را در نظر می‌گیریم
                temperatures.firstOrNull { true && it > 0 }?.let { temp ->
                    val sensorName = getSensorName(sensorType)
                    // دما را به صورت رشته فرمت‌بندی شده درمی‌آوریم تا با مدل ما سازگار باشد
                    val tempFormatted = "%.1f °C".format(temp)
                    thermalList.add(ThermalInfo(type = sensorName, temperature = tempFormatted))
                }
            } catch (e: Exception) {
                // اگر سنسوری در دستگاه پشتیبانی نشود، از آن عبور می‌کنیم
                e.printStackTrace()
            }
        }

        return thermalList
    }

    /**
     * یک تابع کمکی برای تبدیل ثابت‌های عددی سنسور به نام‌های خوانا
     */
    private fun getSensorName(sensorType: Int): String {
        return when (sensorType) {
            HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU -> "پردازنده (CPU)"
            HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU -> "پردازنده گرافیکی (GPU)"
            HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY -> "باتری"
            HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN -> "بدنه دستگاه"
            else -> "سنسور نامشخص"
        }
    }

    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }


    // یک نمونه از SharedPreferences ایجاد می‌کنیم
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("device_inspector_prefs", Context.MODE_PRIVATE)
    }

    // این متد چک می‌کند که آیا اولین اجرای برنامه است یا نه
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true) // مقدار پیش‌فرض true است
    }

    // این متد وضعیت "اولین اجرا" را به "انجام شده" تغییر می‌دهد
    fun setFirstLaunchCompleted() {
        prefs.edit { putBoolean("is_first_launch", false) }
    }

    // این متد آخرین اطلاعات ثبت شده باتری توسط سیستم را فورا برمی‌گرداند
    fun getInitialBatteryInfo(context: Context): BatteryInfo? {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent: Intent? = context.registerReceiver(null, filter)
        return intent?.let { getBatteryInfo(it) }
    }

}
