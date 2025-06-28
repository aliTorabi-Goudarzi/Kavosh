package ir.dekot.kavosh.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
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
    private val repository: DeviceInfoRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // **اصلاح ۱: اضافه کردن پشته برای تاریخچه ناوبری**
    private val _backStack = mutableListOf<Screen>()

    init {
        if (repository.isFirstLaunch()) {
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    /**
     * **اصلاح ۲: تابع اصلی برای مدیریت ناوبری و پشته**
     * این تابع صفحه فعلی را به پشته اضافه کرده و به مقصد جدید می‌رود.
     */
    private fun navigateTo(destination: Screen) {
        // جلوگیری از اضافه شدن صفحات تکراری به پشته
        if (_currentScreen.value != destination) {
            _backStack.add(_currentScreen.value)
            _currentScreen.value = destination
        }
    }

    // **اصلاح ۳: بازنویسی تابع بازگشت**
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun navigateBack() {
        // اگر پشته خالی نباشد، به آخرین صفحه برمی‌گردیم
        if (_backStack.isNotEmpty()) {
            _currentScreen.value = _backStack.removeLast()
        } else {
            // به عنوان fallback، اگر پشته خالی بود به داشبورد برو
            _currentScreen.value = Screen.Dashboard
        }
    }

    /**
     * این تابع برای زمانی است که می‌خواهیم به داشبورد برگردیم
     * و تمام تاریخچه قبلی را پاک کنیم.
     */
    fun navigateToDashboardAndClearHistory() {
        _backStack.clear()
        _currentScreen.value = Screen.Dashboard
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

    fun onScanCompleted() {
        // پس از اسکن، تاریخچه باید پاک شود
        _backStack.clear()
        _currentScreen.value = Screen.Dashboard
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
}