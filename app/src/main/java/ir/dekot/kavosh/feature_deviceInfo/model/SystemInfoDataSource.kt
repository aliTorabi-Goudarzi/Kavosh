package ir.dekot.kavosh.feature_deviceInfo.model

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.SystemInfo
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات سیستم عامل
 * مسئول دریافت اطلاعات Android، SDK، Build و تشخیص روت
 */
@Singleton
class SystemInfoDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * دریافت اطلاعات سیستم عامل
     * @return اطلاعات کامل سیستم شامل نسخه Android، SDK، Build و وضعیت روت
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
     * تشخیص روت بودن دستگاه
     * @return true اگر دستگاه روت باشد، در غیر این صورت false
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
