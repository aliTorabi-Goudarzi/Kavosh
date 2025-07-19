package ir.dekot.kavosh.core.splash

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen
import ir.dekot.kavosh.feature_customeTheme.Theme
import ir.dekot.kavosh.feature_dashboard.viewModel.DashboardViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * مدیریت صفحه اسپلش بر اساس تم فعلی برنامه و بارگذاری داده‌ها
 * Manages splash screen configuration based on current app theme and data loading
 */
class SplashScreenManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    // CoroutineScope برای مدیریت کوروتین‌ها
    // CoroutineScope for managing coroutines
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // --- State های مربوط به بارگذاری داده‌ها ---
    private val _isDataLoading = MutableStateFlow(false)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    private val _isDataLoadingComplete = MutableStateFlow(false)
    val isDataLoadingComplete: StateFlow<Boolean> = _isDataLoadingComplete.asStateFlow()

    /**
     * پیکربندی صفحه اسپلش بر اساس تم فعلی و شروع بارگذاری داده‌ها
     * Configure splash screen based on current theme and start data loading
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun configureSplashScreen(
        splashScreen: SplashScreen,
        activity: Activity,
        deviceInfoViewModel: DeviceInfoViewModel,
        dashboardViewModel: DashboardViewModel,
        onDataLoadingComplete: () -> Unit
    ) {
        // دریافت تم فعلی از تنظیمات
        // Get current theme from settings
        val currentTheme = getCurrentTheme()
        val isSystemDark = isSystemInDarkTheme()
        val isDarkTheme = when (currentTheme) {
            Theme.SYSTEM -> isSystemDark
            Theme.LIGHT -> false
            Theme.DARK -> true
            Theme.AMOLED -> true
        }

        // اعمال تنظیمات به صفحه اسپلش
        // Apply settings to splash screen
        applySplashScreenSettings(splashScreen, onDataLoadingComplete)

        // شروع بارگذاری داده‌ها
        // Start data loading
        startDataLoading(activity, deviceInfoViewModel, dashboardViewModel, onDataLoadingComplete)
    }

    /**
     * دریافت تم فعلی از SharedPreferences
     * Get current theme from SharedPreferences
     */
    private fun getCurrentTheme(): Theme {
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val themeOrdinal = sharedPrefs.getInt("theme", Theme.SYSTEM.ordinal)
        return Theme.entries.toTypedArray().getOrElse(themeOrdinal) { Theme.SYSTEM }
    }

    /**
     * تشخیص حالت تاریک سیستم
     * Detect system dark mode
     */
    private fun isSystemInDarkTheme(): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * شروع فرآیند بارگذاری داده‌ها در پس‌زمینه
     * Start data loading process in background
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun startDataLoading(
        activity: Activity,
        deviceInfoViewModel: DeviceInfoViewModel,
        dashboardViewModel: DashboardViewModel,
        onDataLoadingComplete: () -> Unit
    ) {
        if (_isDataLoading.value) return

        coroutineScope.launch {
            _isDataLoading.value = true
            _isDataLoadingComplete.value = false

            try {
                // بارگذاری کامل تمام داده‌های مورد نیاز
                // Load all required data
                preloadAllData(activity, deviceInfoViewModel, dashboardViewModel)

                // تاخیر کوتاه برای اطمینان از تکمیل انیمیشن اسپلش
                // Short delay to ensure splash animation completion
                delay(1000)

                _isDataLoadingComplete.value = true
                _isDataLoading.value = false

                // اطلاع به MainActivity که داده‌ها آماده است
                // Notify MainActivity that data is ready
                onDataLoadingComplete()
            } catch (_: Exception) {
                // در صورت بروز خطا، حداقل داده‌های پایه را بارگذاری می‌کنیم
                // In case of error, load at least basic data
                dashboardViewModel.loadDashboardItems()
                _isDataLoadingComplete.value = true
                _isDataLoading.value = false
                onDataLoadingComplete()
            }
        }
    }

    /**
     * بارگذاری کامل تمام داده‌های مورد نیاز برای داشبورد
     * Load all required data for dashboard
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun preloadAllData(
        activity: Activity,
        deviceInfoViewModel: DeviceInfoViewModel,
        dashboardViewModel: DashboardViewModel
    ) {
        try {
            // بارگذاری اطلاعات دستگاه
            // Load device information
            if (settingsRepository.isFirstLaunch()) {
                // برای اولین اجرا، مستقیماً داده‌ها را بارگذاری می‌کنیم
                // For first launch, directly load data
                deviceInfoViewModel.loadDataForFirstLaunch(activity)
            } else {
                // برای اجراهای بعدی، اطمینان از بارگذاری کامل داده‌ها
                // For subsequent launches, ensure complete data loading
                deviceInfoViewModel.ensureDataIsLoaded(activity)
            }

            // بارگذاری آیتم‌های داشبورد
            // Load dashboard items
            dashboardViewModel.loadDashboardItems()

            // بارگذاری اطلاعات برنامه‌ها (در صورت نیاز)
            // Load apps information if needed
            deviceInfoViewModel.loadAppsListIfNeeded()
        } catch (_: Exception) {
            // در صورت بروز خطا، حداقل داده‌های پایه را بارگذاری می‌کنیم
            // In case of error, load at least basic data
            dashboardViewModel.loadDashboardItems()
        }
    }

    /**
     * اعمال تنظیمات به صفحه اسپلش
     * Apply settings to splash screen
     */
    private fun applySplashScreenSettings(
        splashScreen: SplashScreen,
        onDataLoadingComplete: () -> Unit
    ) {
        // صفحه اسپلش را تا زمان تکمیل بارگذاری داده‌ها نگه می‌داریم
        // Keep splash screen until data loading is complete
        splashScreen.setKeepOnScreenCondition {
            !_isDataLoadingComplete.value
        }

        // تنظیم انیمیشن خروج نرم
        // Set smooth exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // انیمیشن محو شدن نرم
            // Smooth fade out animation
            splashScreenView.iconView.animate()
                ?.alpha(0f)
                ?.setDuration(300)
                ?.withEndAction {
                    splashScreenView.remove()
                }
        }
    }

    /**
     * ریست کردن وضعیت بارگذاری
     * Reset loading state
     */
    fun resetLoadingState() {
        _isDataLoading.value = false
        _isDataLoadingComplete.value = false
    }

    /**
     * پاک کردن منابع
     * Clean up resources
     */
    fun cleanup() {
        coroutineScope.coroutineContext.job.cancel()
    }

    companion object {
        /**
         * ایجاد نمونه از SplashScreenManager
         * Create instance of SplashScreenManager
         */
        fun create(context: Context, settingsRepository: SettingsRepository): SplashScreenManager {
            return SplashScreenManager(context, settingsRepository)
        }
    }
}
