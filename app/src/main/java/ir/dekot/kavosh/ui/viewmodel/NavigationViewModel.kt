package ir.dekot.kavosh.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    // --- State مربوط به ناوبری ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- توابع ناوبری ---

    fun navigateToDetail(category: InfoCategory) {
        _currentScreen.value = Screen.Detail(category)
    }

    fun navigateBack() {
        // در آینده می‌توان منطق پیچیده‌تری برای بازگشت مدیریت کرد
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

    /**
     * این تابع پس از اتمام اسکن اولیه، صفحه را به داشبورد تغییر می‌دهد.
     */
    fun onScanCompleted() {
        _currentScreen.value = Screen.Dashboard
    }
}