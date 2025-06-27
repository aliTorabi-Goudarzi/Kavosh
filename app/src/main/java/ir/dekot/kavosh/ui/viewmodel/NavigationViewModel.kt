package ir.dekot.kavosh.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    // **اصلاح ۱: تزریق ریپازیتوری برای دسترسی به تنظیمات**
    private val repository: DeviceInfoRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    init {
        // **اصلاح ۲: اضافه کردن منطق بررسی اجرای اول**
        // این بلاک تشخیص می‌دهد که برنامه برای اولین بار اجرا شده یا نه
        // و صفحه شروع را بر اساس آن تنظیم می‌کند.
        if (repository.isFirstLaunch()) {
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    // --- توابع ناوبری (بدون تغییر) ---

    fun navigateToDetail(category: InfoCategory) {
        _currentScreen.value = Screen.Detail(category)
    }

    fun navigateBack() {
        _currentScreen.value = Screen.Dashboard
    }

    fun navigateToSettings() {
        _currentScreen.value = Screen.Settings
    }

    fun navigateToAbout() {
        _currentScreen.value = Screen.About
    }

    fun navigateToEditDashboard() {
        _currentScreen.value = Screen.EditDashboard
    }

    fun navigateToSensorDetail(sensorType: Int) {
        _currentScreen.value = Screen.SensorDetail(sensorType)
    }

    fun onScanCompleted() {
        _currentScreen.value = Screen.Dashboard
    }
}