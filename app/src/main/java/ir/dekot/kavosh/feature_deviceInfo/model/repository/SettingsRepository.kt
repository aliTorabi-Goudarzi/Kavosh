package ir.dekot.kavosh.feature_deviceInfo.model.repository

import ir.dekot.kavosh.feature_customeTheme.ColorTheme
import ir.dekot.kavosh.feature_customeTheme.CustomColorTheme
import ir.dekot.kavosh.feature_customeTheme.PredefinedColorTheme
import ir.dekot.kavosh.feature_customeTheme.Theme
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_settings.model.SettingsDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن تنظیمات - مسئول مدیریت تمام تنظیمات برنامه
 * شامل زبان، تم، داشبورد، کش و تم‌های رنگی
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource
) {

    // --- مدیریت زبان ---

    /**
     * ذخیره زبان انتخاب شده
     * @param language کد زبان (مثل "fa" یا "en")
     */
    fun saveLanguage(language: String) = settingsDataSource.saveLanguage(language)

    /**
     * دریافت زبان فعلی
     * @return کد زبان فعلی
     */
    fun getLanguage(): String = settingsDataSource.getLanguage()

    // --- مدیریت اولین اجرا ---

    /**
     * بررسی اولین اجرای برنامه
     * @return true اگر اولین اجرا باشد
     */
    fun isFirstLaunch(): Boolean = settingsDataSource.isFirstLaunch()

    /**
     * تنظیم تکمیل اولین اجرا
     */
    fun setFirstLaunchCompleted() = settingsDataSource.setFirstLaunchCompleted()

    // --- مدیریت تم ---

    /**
     * ذخیره تم انتخاب شده
     * @param theme تم مورد نظر
     */
    fun saveTheme(theme: Theme) = settingsDataSource.saveTheme(theme)

    /**
     * دریافت تم فعلی
     * @return تم فعلی برنامه
     */
    fun getTheme(): Theme = settingsDataSource.getTheme()

    /**
     * فعال/غیرفعال کردن تم پویا
     * @param enabled وضعیت تم پویا
     */
    fun setDynamicThemeEnabled(enabled: Boolean) = settingsDataSource.setDynamicThemeEnabled(enabled)

    /**
     * بررسی فعال بودن تم پویا
     * @return true اگر تم پویا فعال باشد
     */
    fun isDynamicThemeEnabled(): Boolean = settingsDataSource.isDynamicThemeEnabled()

    // --- مدیریت داشبورد ---

    /**
     * ذخیره ترتیب دسته‌بندی‌های داشبورد
     * @param categories لیست دسته‌بندی‌ها به ترتیب مطلوب
     */
    fun saveDashboardOrder(categories: List<InfoCategory>) = settingsDataSource.saveDashboardOrder(categories)

    /**
     * دریافت ترتیب دسته‌بندی‌های داشبورد
     * @return لیست دسته‌بندی‌ها به ترتیب ذخیره شده
     */
    fun getDashboardOrder(): List<InfoCategory> = settingsDataSource.getDashboardOrder()

    /**
     * ذخیره دسته‌بندی‌های مخفی
     * @param hidden مجموعه دسته‌بندی‌های مخفی
     */
    fun saveHiddenCategories(hidden: Set<InfoCategory>) = settingsDataSource.saveHiddenCategories(hidden)

    /**
     * دریافت دسته‌بندی‌های مخفی
     * @return مجموعه دسته‌بندی‌های مخفی
     */
    fun getHiddenCategories(): Set<InfoCategory> = settingsDataSource.getHiddenCategories()

    /**
     * فعال/غیرفعال کردن قابلیت جابجایی
     * @param enabled وضعیت قابلیت جابجایی
     */
    fun setReorderingEnabled(enabled: Boolean) = settingsDataSource.setReorderingEnabled(enabled)

    /**
     * بررسی فعال بودن قابلیت جابجایی
     * @return true اگر جابجایی فعال باشد
     */
    fun isReorderingEnabled(): Boolean = settingsDataSource.isReorderingEnabled()

    // --- مدیریت کش اطلاعات دستگاه ---

    /**
     * ذخیره کش اطلاعات دستگاه
     * @param deviceInfo اطلاعات کامل دستگاه
     */
    fun saveDeviceInfoCache(deviceInfo: DeviceInfo) = settingsDataSource.saveDeviceInfoCache(deviceInfo)

    /**
     * دریافت کش اطلاعات دستگاه
     * @return اطلاعات دستگاه از کش یا null
     */
    fun getDeviceInfoCache(): DeviceInfo? = settingsDataSource.getDeviceInfoCache()

    /**
     * پاک کردن کش اطلاعات دستگاه
     */
    fun clearDeviceInfoCache() = settingsDataSource.clearDeviceInfoCache()

    // --- مدیریت تم‌های رنگی ---

    /**
     * ذخیره تم رنگی از پیش تعریف شده
     * @param colorTheme تم رنگی انتخاب شده
     */
    fun savePredefinedColorTheme(colorTheme: PredefinedColorTheme) =
        settingsDataSource.savePredefinedColorTheme(colorTheme)

    /**
     * ذخیره تم رنگی سفارشی
     * @param customTheme تم رنگی سفارشی
     */
    fun saveCustomColorTheme(customTheme: CustomColorTheme) =
        settingsDataSource.saveCustomColorTheme(customTheme)

    /**
     * دریافت تم رنگی فعلی
     * @return تم رنگی فعلی یا null
     */
    fun getCurrentColorTheme(): ColorTheme? = settingsDataSource.getCurrentColorTheme()

    /**
     * بازنشانی تم رنگی به حالت پیش‌فرض
     */
    fun resetColorTheme() = settingsDataSource.resetColorTheme()

    /**
     * بررسی وجود تم رنگی سفارشی
     * @return true اگر تم سفارشی موجود باشد
     */
    fun hasCustomColorTheme(): Boolean = settingsDataSource.hasCustomColorTheme()
}
