package ir.dekot.kavosh.data.source

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.HardwarePropertiesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات مربوط به نیرو (باتری و دما).
 * مسئولیت واکشی داده از BatteryManager و HardwarePropertiesManager را بر عهده دارد.
 */
@Singleton
class PowerDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private val hardwareService = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as? HardwarePropertiesManager

    /**
     * اطلاعات باتری را از یک Intent دریافتی استخراج می‌کند.
     */
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

    /**
     * آخرین اطلاعات ثبت شده باتری توسط سیستم را فورا برمی‌گرداند.
     */
    fun getInitialBatteryInfo(): BatteryInfo? {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent: Intent? = context.registerReceiver(null, filter)
        return intent?.let { getBatteryInfo(it) }
    }

    /**
     * اطلاعات دمای قطعات مختلف دستگاه را با استفاده از API رسمی برمی‌گرداند.
     */
    fun getThermalInfo(): List<ThermalInfo> {
        val thermalList = mutableListOf<ThermalInfo>()
        hardwareService ?: return emptyList()

        val sensorTypes = intArrayOf(
            HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY,
            HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN
        )

        for (sensorType in sensorTypes) {
            try {
                val temperatures = hardwareService.getDeviceTemperatures(
                    sensorType,
                    HardwarePropertiesManager.TEMPERATURE_CURRENT
                )
                temperatures.firstOrNull { it > 0 }?.let { temp ->
                    val sensorName = getSensorName(sensorType)
                    val tempFormatted = "%.1f °C".format(temp)
                    thermalList.add(ThermalInfo(type = sensorName, temperature = tempFormatted))
                }
            } catch (_: Exception) {
                // اگر سنسوری پشتیبانی نشود، از آن عبور می‌کنیم.
            }
        }
        return thermalList
    }

    /**
     * یک تابع کمکی برای تبدیل ثابت عددی سنسور دما به نام خوانا.
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
}

