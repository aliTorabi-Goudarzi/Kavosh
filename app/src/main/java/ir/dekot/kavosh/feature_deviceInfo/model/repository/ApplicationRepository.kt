package ir.dekot.kavosh.feature_deviceInfo.model.repository

import ir.dekot.kavosh.feature_deviceInfo.model.AppInfo
import ir.dekot.kavosh.feature_deviceInfo.model.AppsDataSource
import ir.dekot.kavosh.feature_settings.model.SettingsDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن اطلاعات برنامه‌ها - مسئول مدیریت اطلاعات مربوط به برنامه‌های نصب شده
 * شامل لیست برنامه‌ها، کش برنامه‌ها و شمارش پکیج‌ها
 */
@Singleton
class ApplicationRepository @Inject constructor(
    private val appsDataSource: AppsDataSource,
    private val settingsDataSource: SettingsDataSource
) {

    /**
     * دریافت لیست برنامه‌های نصب شده
     * @return لیست کامل برنامه‌های نصب شده در دستگاه
     */
    fun getInstalledApps(): List<AppInfo> = appsDataSource.getInstalledApps()

    /**
     * دریافت اطلاعات برنامه‌ها (نام مستعار)
     * @return لیست کامل برنامه‌های نصب شده
     */
    fun getAppsInfo(): List<AppInfo> = appsDataSource.getInstalledApps()

    /**
     * دریافت تعداد پکیج‌های فعلی
     * @return تعداد کل پکیج‌های نصب شده
     */
    fun getCurrentPackageCount(): Int = appsDataSource.getPackageCount()

    // --- مدیریت کش برنامه‌ها ---

    /**
     * ذخیره کش برنامه‌ها
     * @param userApps لیست برنامه‌های کاربری
     * @param systemApps لیست برنامه‌های سیستمی
     * @param count تعداد کل پکیج‌ها
     */
    fun saveAppsCache(userApps: List<AppInfo>, systemApps: List<AppInfo>, count: Int) =
        settingsDataSource.saveAppsCache(userApps, systemApps, count)

    /**
     * دریافت کش برنامه‌های کاربری
     * @return لیست برنامه‌های کاربری از کش یا null
     */
    fun getUserAppsCache(): List<AppInfo>? = settingsDataSource.getUserAppsCache()

    /**
     * دریافت کش برنامه‌های سیستمی
     * @return لیست برنامه‌های سیستمی از کش یا null
     */
    fun getSystemAppsCache(): List<AppInfo>? = settingsDataSource.getSystemAppsCache()

    /**
     * دریافت تعداد پکیج‌ها از کش
     * @return تعداد پکیج‌ها از کش
     */
    fun getPackageCountCache(): Int = settingsDataSource.getPackageCountCache()
}
