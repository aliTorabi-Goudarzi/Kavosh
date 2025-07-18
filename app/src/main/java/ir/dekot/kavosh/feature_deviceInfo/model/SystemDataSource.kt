package ir.dekot.kavosh.feature_deviceInfo.model

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.SystemInfo

import java.io.File
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION")
@Singleton
class SystemDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisplayInfo(activity: Activity): DisplayInfo {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val refreshRate = activity.display?.refreshRate ?: 60.0f
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

    fun getSensorInfo(activity: Activity): List<SensorInfo> {
        val sensorManager = activity.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        return sensorManager.getSensorList(Sensor.TYPE_ALL).map {
            // *** تغییر کلیدی: پاس دادن نوع سنسور به مدل ***
            SensorInfo(name = it.name, vendor = it.vendor, type = it.type)
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

    /**
     * *** تابع جدید: ***
     * اطلاعات نسخه برنامه را از پکیج منیجر دریافت می‌کند.
     */
    fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (_: Exception) {
            "N/A"
        }
    }

}