// Path: app/src/main/java/ir/dekot/kavosh/data/source/AppsDataSource.kt
package ir.dekot.kavosh.feature_deviceInfo.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * لیستی از تمام برنامه‌های نصب شده (سیستمی و کاربری) را به همراه جزئیاتشان برمی‌گرداند.
     * @return لیستی از [AppInfo].
     */
    @Suppress("DEPRECATION")
    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        // فلگ GET_PERMISSIONS برای دسترسی به لیست مجوزهای هر برنامه ضروری است
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        return packages.mapNotNull { packageInfo ->
            val appName = packageInfo.applicationInfo?.loadLabel(pm).toString()
            val packageName = packageInfo.packageName
            val versionName = packageInfo.versionName ?: "N/A"
            val versionCode =
                packageInfo.longVersionCode
            val installTime = packageInfo.firstInstallTime
            val isSystemApp = (packageInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) != 0
            val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

            AppInfo(
                appName = appName,
                packageName = packageName,
                versionName = versionName,
                versionCode = versionCode,
                installTime = installTime,
                isSystemApp = isSystemApp,
                permissions = permissions,
            )
        }.sortedBy { it.appName.lowercase() } // مرتب‌سازی لیست بر اساس نام برنامه
    }

    /**
     * فقط تعداد کل پکیج‌های نصب شده را برمی‌گرداند.
     * این عملیات بسیار سریع‌تر از واکشی لیست کامل است.
     */
    fun getPackageCount(): Int {
        return try {
            context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA).size
        } catch (_: Exception) {
            0
        }
    }
}

//این DataSource با استفاده از PackageManager لیست تمام پکیج‌های نصب شده را دریافت می‌کند.
//
//برای هر پکیج، یک شی AppInfo ساخته شده و اطلاعاتی مانند نام، ورژن، تاریخ نصب، مجوزها و سیستمی بودن یا نبودن برنامه استخراج می‌شود.
//
//در نهایت، لیست برنامه‌ها بر اساس نامشان مرتب‌سازی می‌شود تا نمایش کاربرپسندتری داشته باشد.