package ir.dekot.kavosh.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.HardwarePropertiesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class PowerDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    private val hardwareService = context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as? HardwarePropertiesManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun getBatteryInfo(intent: Intent): BatteryInfo {
        val temperatureValue = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
        val voltageValue = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0f

        val designCapacity = getDesignCapacity()
        val actualCapacity = getActualCapacity()
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000
        val powerNow = if (voltageValue > 0) abs(currentNow * voltageValue / 1000.0f) else 0.0f

        return BatteryInfo(
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
            health = getHealthString(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)),
            status = getStatusString(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)),
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: context.getString(R.string.label_undefined),
            temperature = context.getString(R.string.unit_format_celsius, temperatureValue),
            voltage = context.getString(R.string.unit_format_volt, voltageValue),
            designCapacity = designCapacity,
            actualCapacity = actualCapacity,
            chargeCurrent = currentNow,
            chargePower = powerNow
        )
    }

    private fun getDesignCapacity(): Int {
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        return if (capacity != Int.MIN_VALUE && capacity > 0) capacity / 1000 else 0
    }

    @SuppressLint("PrivateApi")
    private fun getActualCapacity(): Double {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
            powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    // **اصلاح ۱: افزودن بدنه کامل تابع getHealthString**
    private fun getHealthString(health: Int): String {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.battery_health_good)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.battery_health_dead)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.battery_health_overheat)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.battery_health_over_voltage)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.battery_health_unspecified_failure)
            else -> context.getString(R.string.label_undefined)
        }
    }

    // **اصلاح ۲: افزودن بدنه کامل تابع getStatusString**
    private fun getStatusString(status: Int): String {
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.battery_status_charging)
            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.battery_status_discharging)
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.battery_status_full)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.battery_status_not_charging)
            else -> context.getString(R.string.label_undefined)
        }
    }

    fun getInitialBatteryInfo(): BatteryInfo? {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent: Intent? = context.registerReceiver(null, filter)
        return intent?.let { getBatteryInfo(it) }
    }

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