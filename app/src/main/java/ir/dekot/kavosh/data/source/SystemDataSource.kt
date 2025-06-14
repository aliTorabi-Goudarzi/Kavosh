package ir.dekot.kavosh.data.source

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.data.model.components.SensorInfo
import ir.dekot.kavosh.data.model.components.SystemInfo
import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات عمومی سیستم (اندروید، نمایشگر، سنسورها).
 * این کلاس دیگر به Context در constructor خود وابسته نیست.
 */
@Suppress("DEPRECATION")
@Singleton
class SystemDataSource @Inject constructor() {

    // ما نمی‌توانیم SensorManager را در constructor بسازیم چون به context نیاز دارد.
    // آن را بعداً در متد مربوطه می‌سازیم.

    /**
     * اطلاعات صفحه نمایش را برمی‌گرداند.
     * @param activity این متد حالا Activity را به عنوان پارامتر ورودی می‌گیرد.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisplayInfo(activity: Activity): DisplayInfo {
        val displayMetrics = DisplayMetrics()
        // از پارامتر ورودی activity برای دسترسی به WindowManager استفاده می‌کنیم
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val refreshRate = activity.display?.refreshRate ?: 60.0f
        return DisplayInfo(
            resolution = "${displayMetrics.heightPixels}x${displayMetrics.widthPixels}",
            density = "${displayMetrics.densityDpi} dpi",
            refreshRate = "${DecimalFormat("#.##").format(refreshRate)} Hz"
        )
    }

    /**
     * اطلاعات سیستم عامل را برمی‌گرداند.
     */
    fun getSystemInfo(): SystemInfo {
        return SystemInfo(
            androidVersion = Build.VERSION.RELEASE,
            sdkLevel = Build.VERSION.SDK_INT.toString(),
            buildNumber = Build.DISPLAY,
            isRooted = isDeviceRooted()
        )
    }

    /**
     * لیست تمام سنسورهای موجود در دستگاه را برمی‌گرداند.
     * @param activity برای دسترسی به SensorManager به Context نیاز داریم.
     */
    fun getSensorInfo(activity: Activity): List<SensorInfo> {
        // SensorManager را در لحظه نیاز و با استفاده از context دریافتی می‌سازیم
        val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        return sensorManager.getSensorList(Sensor.TYPE_ALL).map {
            SensorInfo(name = it.name, vendor = it.vendor)
        }
    }

    /**
     * یک متد کمکی برای بررسی وضعیت روت بودن دستگاه.
     */
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
}