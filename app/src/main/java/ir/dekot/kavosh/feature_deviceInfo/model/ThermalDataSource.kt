package ir.dekot.kavosh.feature_deviceInfo.model

import android.content.Context
import android.os.HardwarePropertiesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای مانیتورینگ حرارتی
 * مسئول دریافت اطلاعات دمای تمام اجزای دستگاه شامل CPU، GPU، باتری و پوسته
 */
@Singleton
class ThermalDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    private val hardwareService = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as? HardwarePropertiesManager

    /**
     * دریافت اطلاعات حرارتی دستگاه
     * @return لیست اطلاعات حرارتی از منابع مختلف
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
                    val tempFormatted = context.getString(R.string.unit_format_celsius, temp)
                    thermalList.add(ThermalInfo(type = sensorName, temperature = tempFormatted))
                }
            } catch (_: Exception) {
                // Ignore if a sensor is not supported
            }
        }
        return thermalList
    }

    /**
     * دریافت نام سنسور حرارتی
     * @param sensorType نوع سنسور
     * @return نام سنسور به زبان فارسی
     */
    private fun getSensorName(sensorType: Int): String {
        return when (sensorType) {
            HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU -> context.getString(R.string.cpu_title)
            HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU -> context.getString(R.string.gpu_title)
            HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY -> context.getString(R.string.category_battery)
            HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN -> context.getString(R.string.category_device)
            else -> context.getString(R.string.label_undefined)
        }
    }
}
