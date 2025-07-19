package ir.dekot.kavosh.feature_deviceInfo.model

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات حسگرها
 * مسئول دریافت لیست حسگرهای موجود در دستگاه و مشخصات آن‌ها
 */
@Singleton
class SensorDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * دریافت لیست حسگرهای دستگاه
     * @param activity Activity مورد نیاز برای دسترسی به حسگرها
     * @return لیست کامل حسگرهای موجود در دستگاه
     */
    fun getSensorInfo(activity: Activity): List<SensorInfo> {
        val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        return sensorManager.getSensorList(Sensor.TYPE_ALL).map {
            // *** تغییر کلیدی: پاس دادن نوع سنسور به مدل ***
            SensorInfo(name = it.name, vendor = it.vendor, type = it.type)
        }
    }
}
