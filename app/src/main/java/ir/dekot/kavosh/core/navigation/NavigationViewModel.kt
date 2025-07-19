package ir.dekot.kavosh.core.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // **اصلاح ۱: اضافه کردن پشته برای تاریخچه ناوبری**
    private val _backStack = mutableListOf<Screen>()

    // **اصلاح جدید: ردیابی بخش فعلی bottom navigation**
    private val _currentBottomNavSection = MutableStateFlow(BottomNavItem.INFO)
    val currentBottomNavSection: StateFlow<BottomNavItem> = _currentBottomNavSection.asStateFlow()

    // **اصلاح جدید: ردیابی آخرین بخش قبل از رفتن به صفحه جزئیات**
    private var lastBottomNavSection: BottomNavItem = BottomNavItem.INFO

    init {
        // همیشه با صفحه اسپلش شروع می‌کنیم که خود داده‌ها را بارگذاری می‌کند
        // Always start with splash screen which handles data loading itself
        if (settingsRepository.isFirstLaunch()) {
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    /**
     * **اصلاح جدید: تابع برای تغییر بخش bottom navigation**
     */
    fun setBottomNavSection(section: BottomNavItem) {
        _currentBottomNavSection.value = section
        // اگر در حال حاضر در صفحه Dashboard هستیم، تغییری در currentScreen نمی‌دهیم
        // فقط بخش bottom nav را تغییر می‌دهیم
    }

    /**
     * **اصلاح ۲: تابع اصلی برای مدیریت ناوبری و پشته**
     * این تابع صفحه فعلی را به پشته اضافه کرده و به مقصد جدید می‌رود.
     */
    private fun navigateTo(destination: Screen) {
        // جلوگیری از اضافه شدن صفحات تکراری به پشته
        if (_currentScreen.value != destination) {
            // اگر از Dashboard به صفحه دیگری می‌رویم، بخش فعلی را ذخیره می‌کنیم
            if (_currentScreen.value == Screen.Dashboard) {
                lastBottomNavSection = _currentBottomNavSection.value
            }
            _backStack.add(_currentScreen.value)
            _currentScreen.value = destination
        }
    }

    // **اصلاح ۳: بازنویسی تابع بازگشت با پشتیبانی از بخش‌های bottom nav**
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun navigateBack() {
        // اگر پشته خالی نباشد، به آخرین صفحه برمی‌گردیم
        if (_backStack.isNotEmpty()) {
            val previousScreen = _backStack.removeLast()
            _currentScreen.value = previousScreen

            // اگر به Dashboard برمی‌گردیم، بخش bottom nav را به آخرین بخش بازگردانیم
            if (previousScreen == Screen.Dashboard) {
                _currentBottomNavSection.value = lastBottomNavSection
            }
        } else {
            // به عنوان fallback، اگر پشته خالی بود به داشبورد برو
            _currentScreen.value = Screen.Dashboard
            _currentBottomNavSection.value = BottomNavItem.INFO
        }
    }

    /**
     * این تابع برای زمانی است که می‌خواهیم به داشبورد برگردیم
     * و تمام تاریخچه قبلی را پاک کنیم.
     */
    fun navigateToDashboardAndClearHistory() {
        _backStack.clear()
        _currentScreen.value = Screen.Dashboard
        _currentBottomNavSection.value = BottomNavItem.INFO
        lastBottomNavSection = BottomNavItem.INFO
    }


    // **اصلاح ۴: تمام توابع ناوبری حالا از navigateTo استفاده می‌کنند**
    fun navigateToDetail(category: InfoCategory) {
        navigateTo(Screen.Detail(category))
    }

    fun navigateToSettings() {
        navigateTo(Screen.Settings)
    }



    fun navigateToAbout() {
        navigateTo(Screen.About)
    }

    fun navigateToEditDashboard() {
        navigateTo(Screen.EditDashboard)
    }

    fun navigateToSensorDetail(sensorType: Int) {
        navigateTo(Screen.SensorDetail(sensorType))
    }

    fun navigateToCpuStressTest() {
        navigateTo(Screen.CpuStressTest)
    }

    fun navigateToNetworkTools() {
        navigateTo(Screen.NetworkTools)
    }

    fun navigateToDisplayTest() {
        navigateTo(Screen.DisplayTest)
    }

    fun navigateToStorageTest() {
        navigateTo(Screen.StorageTest)
    }

    // توابع ناوبری برای ابزارهای تشخیصی جدید
    fun navigateToHealthCheck() {
        navigateTo(Screen.HealthCheck)
    }

    fun navigateToPerformanceScore() {
        navigateTo(Screen.PerformanceScore)
    }

    fun navigateToDeviceComparison() {
        navigateTo(Screen.DeviceComparison)
    }

    fun onScanCompleted() {
        // پس از اسکن، مستقیماً به داشبورد می‌رویم
        // After scan, go directly to dashboard
        _backStack.clear()
        _currentScreen.value = Screen.Dashboard
        _currentBottomNavSection.value = BottomNavItem.INFO
        lastBottomNavSection = BottomNavItem.INFO
    }
}