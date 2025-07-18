package ir.dekot.kavosh.feature_deviceInfo.model

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات پردازنده
 * مسئول دریافت مشخصات CPU و مانیتورینگ فرکانس زنده هسته‌ها
 */
@Singleton
class CpuDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * دریافت اطلاعات کامل پردازنده
     * @return اطلاعات کامل CPU شامل تعداد هسته، معماری و فرکانس
     */
    fun getCpuInfo(): CpuInfo {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val architecture = System.getProperty("os.arch") ?: context.getString(R.string.label_undefined)
        val model = getCpuModel()

        val coreInfoList = (0 until coreCount).map { i ->
            try {
                val minFreq = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq").readText().trim().toLong()
                val maxFreq = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq").readText().trim().toLong()
                val coreName = context.getString(R.string.cpu_core_prefix, i)
                val minMhz = minFreq / 1000
                val maxMhz = maxFreq / 1000
                // Using a trick to get the unit (MHz) without the number
                val mhzUnit = context.getString(R.string.unit_format_mhz, 0).substringAfter("0").trim()
                "$coreName: $minMhz - $maxMhz $mhzUnit" to maxFreq
            } catch (_: Exception) {
                "${context.getString(R.string.cpu_core_prefix, i)}: ${context.getString(R.string.label_undefined)}" to 0L
            }
        }

        val clockSpeedRanges = coreInfoList.map { it.first }
        val maxFrequenciesKhz = coreInfoList.map { it.second }

        val topologyString = maxFrequenciesKhz
            .filter { it > 0 }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.key }
            .joinToString(" + ") { (maxFreq, count) ->
                val freqGhz = maxFreq / 1000000.0
                "${count}x @ ${context.getString(R.string.unit_format_ghz, freqGhz)}"
            }

        return CpuInfo(
            model = model,
            architecture = architecture,
            coreCount = coreCount,
            process = context.getString(R.string.label_undefined),
            topology = topologyString.ifBlank { context.getString(R.string.label_undefined) },
            clockSpeedRanges = clockSpeedRanges,
            maxFrequenciesKhz = maxFrequenciesKhz,
            liveFrequencies = List(coreCount) { "..." }
        )
    }

    /**
     * دریافت فرکانس زنده هسته‌های پردازنده
     * @return لیست فرکانس فعلی هر هسته
     */
    fun getLiveCpuFrequencies(): List<String> {
        return (0 until Runtime.getRuntime().availableProcessors()).map { i ->
            try {
                val freqKhz = File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq").readText().trim().toLong()
                context.getString(R.string.unit_format_mhz, freqKhz / 1000)
            } catch (_: Exception) {
                context.getString(R.string.label_sleeping)
            }
        }
    }

    /**
     * دریافت مدل پردازنده
     * @return نام مدل CPU
     */
    private fun getCpuModel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.let {
                if (it.isNotBlank() && it != "unknown") return it
            }
        }
        return Build.HARDWARE.takeIf { !it.isNullOrBlank() }
            ?: Build.BOARD.takeIf { !it.isNullOrBlank() }
            ?: context.getString(R.string.label_undefined)
    }
}
