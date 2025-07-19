package ir.dekot.kavosh.feature_deviceInfo.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات برنامه
 * مسئول دریافت اطلاعات نسخه برنامه و متادیتای مربوطه
 */
@Singleton
class AppInfoDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * دریافت اطلاعات نسخه برنامه
     * @return رشته حاوی نام نسخه و کد نسخه برنامه
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
