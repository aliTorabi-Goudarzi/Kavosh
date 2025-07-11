package ir.dekot.kavosh.data.source

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.AppInfo
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.data.model.diagnostic.HealthCheckSummary
import ir.dekot.kavosh.data.model.diagnostic.PerformanceScore
import ir.dekot.kavosh.data.model.diagnostic.DeviceComparison
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataSource @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("device_inspector_prefs", Context.MODE_PRIVATE)
    }

    // کلیدهای SharedPreferences
    private companion object {
        const val KEY_DEVICE_INFO_CACHE = "device_info_cache"
        const val KEY_THEME = "app_theme"
        const val KEY_FIRST_LAUNCH = "is_first_launch"
        const val KEY_DASHBOARD_ORDER = "dashboard_order"
        const val KEY_HIDDEN_CATEGORIES = "hidden_categories"
        const val KEY_DASHBOARD_REORDER_ENABLED = "dashboard_reorder_enabled"
        const val KEY_DYNAMIC_THEME_ENABLED = "dynamic_theme_enabled"
        // کلید جدید برای زبان
        const val KEY_APP_LANGUAGE = "app_language"
        const val KEY_USER_APPS_CACHE = "user_apps_cache"
        const val KEY_SYSTEM_APPS_CACHE = "system_apps_cache"
        const val KEY_PACKAGE_COUNT = "package_count"
        // کلیدهای جدید برای ذخیره تاریخچه تست‌های تشخیصی
        const val KEY_HEALTH_CHECK_HISTORY = "health_check_history"
        const val KEY_PERFORMANCE_SCORE_HISTORY = "performance_score_history"
        const val KEY_DEVICE_COMPARISON_HISTORY = "device_comparison_history"
    }

    // --- متدهای جدید برای کش برنامه‌ها ---

    fun saveAppsCache(userApps: List<AppInfo>, systemApps: List<AppInfo>, count: Int) {
        prefs.edit {
            putString(KEY_USER_APPS_CACHE, Json.encodeToString(userApps))
            putString(KEY_SYSTEM_APPS_CACHE, Json.encodeToString(systemApps))
            putInt(KEY_PACKAGE_COUNT, count)
        }
    }

    fun getUserAppsCache(): List<AppInfo>? {
        val jsonString = prefs.getString(KEY_USER_APPS_CACHE, null)
        return jsonString?.let { Json.decodeFromString<List<AppInfo>>(it) }
    }

    fun getSystemAppsCache(): List<AppInfo>? {
        val jsonString = prefs.getString(KEY_SYSTEM_APPS_CACHE, null)
        return jsonString?.let { Json.decodeFromString<List<AppInfo>>(it) }
    }

    fun getPackageCountCache(): Int {
        return prefs.getInt(KEY_PACKAGE_COUNT, -1)
    }

    // --- متدهای جدید برای ذخیره و بازیابی تاریخچه تست‌های تشخیصی ---

    /**
     * ذخیره تاریخچه بررسی سلامت
     */
    fun saveHealthCheckHistory(history: List<HealthCheckSummary>) {
        val jsonString = Json.encodeToString(history)
        prefs.edit {
            putString(KEY_HEALTH_CHECK_HISTORY, jsonString)
        }
    }

    /**
     * بازیابی تاریخچه بررسی سلامت
     */
    fun getHealthCheckHistory(): List<HealthCheckSummary> {
        val jsonString = prefs.getString(KEY_HEALTH_CHECK_HISTORY, null)
        return jsonString?.let {
            try {
                Json.decodeFromString<List<HealthCheckSummary>>(it)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } ?: emptyList()
    }

    /**
     * ذخیره تاریخچه امتیاز عملکرد
     */
    fun savePerformanceScoreHistory(history: List<PerformanceScore>) {
        val jsonString = Json.encodeToString(history)
        prefs.edit {
            putString(KEY_PERFORMANCE_SCORE_HISTORY, jsonString)
        }
    }

    /**
     * بازیابی تاریخچه امتیاز عملکرد
     */
    fun getPerformanceScoreHistory(): List<PerformanceScore> {
        val jsonString = prefs.getString(KEY_PERFORMANCE_SCORE_HISTORY, null)
        return jsonString?.let {
            try {
                Json.decodeFromString<List<PerformanceScore>>(it)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } ?: emptyList()
    }

    /**
     * ذخیره تاریخچه مقایسه دستگاه
     */
    fun saveDeviceComparisonHistory(history: List<DeviceComparison>) {
        val jsonString = Json.encodeToString(history)
        prefs.edit {
            putString(KEY_DEVICE_COMPARISON_HISTORY, jsonString)
        }
    }

    /**
     * بازیابی تاریخچه مقایسه دستگاه
     */
    fun getDeviceComparisonHistory(): List<DeviceComparison> {
        val jsonString = prefs.getString(KEY_DEVICE_COMPARISON_HISTORY, null)
        return jsonString?.let {
            try {
                Json.decodeFromString<List<DeviceComparison>>(it)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } ?: emptyList()
    }

    // **متدهای جدید برای مدیریت کش**
    fun saveDeviceInfoCache(deviceInfo: DeviceInfo) {
        val jsonString = Json.encodeToString(deviceInfo)
        prefs.edit {
            putString(KEY_DEVICE_INFO_CACHE, jsonString)
        }
    }

    fun getDeviceInfoCache(): DeviceInfo? {
        val jsonString = prefs.getString(KEY_DEVICE_INFO_CACHE, null)
        return jsonString?.let {
            try {
                Json.decodeFromString<DeviceInfo>(it)
            } catch (e: Exception) {
                e.printStackTrace()
                null // در صورت خطا در پارس کردن، کش را نادیده بگیر
            }
        }
    }

    fun clearDeviceInfoCache() {
        prefs.edit {
            remove(KEY_DEVICE_INFO_CACHE)
        }
    }

    // ... (متدهای دیگر بدون تغییر) ...

    /**
     * زبان انتخاب شده توسط کاربر را (به صورت تگ زبان مثل "fa" یا "en") ذخیره می‌کند.
     */
    fun saveLanguage(language: String) {
        prefs.edit {
            putString(KEY_APP_LANGUAGE, language)
        }
    }

    /**
     * آخرین زبان ذخیره شده را می‌خواند.
     * @return در صورتی که زبانی ذخیره نشده باشد، زبان پیش‌فرض دستگاه را برمی‌گرداند.
     */
    fun getLanguage(): String {
        return prefs.getString(KEY_APP_LANGUAGE, "fa") ?: "fa"
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
    }

    /**
     * تم انتخاب شده توسط کاربر را ذخیره می‌کند.
     */
    fun saveTheme(theme: Theme) {
        prefs.edit {
            putString(KEY_THEME, theme.name)
        }
    }

    /**
     * آخرین تم ذخیره شده را می‌خواند.
     * @return مقدار Theme.SYSTEM به عنوان پیش‌فرض.
     */
    fun getTheme(): Theme {
        val themeName = prefs.getString(KEY_THEME, Theme.SYSTEM.name)
        return Theme.valueOf(themeName ?: Theme.SYSTEM.name)
    }

    /**
     * ترتیب آیتم‌های داشبورد را به صورت یک رشته جدا شده با کاما ذخیره می‌کند.
     */
    fun saveDashboardOrder(categories: List<InfoCategory>) {
        val orderString = categories.joinToString(",") { it.name }
        prefs.edit {
            putString(KEY_DASHBOARD_ORDER, orderString)
        }
    }

    /**
     * ترتیب ذخیره شده آیتم‌های داشبورد را بازخوانی می‌کند.
     * اگر ترتیبی ذخیره نشده باشد، ترتیب پیش‌فرض را برمی‌گرداند.
     */
    fun getDashboardOrder(): List<InfoCategory> {
        val defaultOrder = InfoCategory.entries.joinToString(",") { it.name }
        val orderString = prefs.getString(KEY_DASHBOARD_ORDER, defaultOrder) ?: defaultOrder
        return orderString.split(",").mapNotNull { try { InfoCategory.valueOf(it) } catch (_: Exception) { null } }
    }

    /**
     * مجموعه‌ای از دسته‌بندی‌های مخفی را ذخیره می‌کند.
     */
    fun saveHiddenCategories(hidden: Set<InfoCategory>) {
        val hiddenSetString = hidden.map { it.name }.toSet()
        prefs.edit {
            putStringSet(KEY_HIDDEN_CATEGORIES, hiddenSetString)
        }
    }

    /**
     * دسته‌بندی‌های مخفی شده را بازخوانی می‌کند.
     */
    fun getHiddenCategories(): Set<InfoCategory> {
        val hiddenSetString = prefs.getStringSet(KEY_HIDDEN_CATEGORIES, emptySet()) ?: emptySet()
        return hiddenSetString.mapNotNull { try { InfoCategory.valueOf(it) } catch (_: Exception) { null } }.toSet()
    }

    // --- متدهای جدید برای کنترل قابلیت جابجایی ---
    /**
     * وضعیت قابلیت جابجایی داشبورد را ذخیره می‌کند.
     */
    fun setReorderingEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DASHBOARD_REORDER_ENABLED, enabled) }
    }

    /**
     * وضعیت ذخیره شده قابلیت جابجایی را بازخوانی می‌کند.
     * به صورت پیش‌فرض، این قابلیت فعال است.
     */
    fun isReorderingEnabled(): Boolean {
        return prefs.getBoolean(KEY_DASHBOARD_REORDER_ENABLED, true)
    }

    // --- متدهای جدید برای کنترل تم پویا ---
    /**
     * وضعیت قابلیت تم پویا را ذخیره می‌کند.
     */
    fun setDynamicThemeEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_THEME_ENABLED, enabled) }
    }

    /**
     * وضعیت ذخیره شده تم پویا را بازخوانی می‌کند.
     * به صورت پیش‌فرض، این قابلیت فعال است.
     */
    fun isDynamicThemeEnabled(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_THEME_ENABLED, true)
    }
}