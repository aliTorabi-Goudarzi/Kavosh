package ir.dekot.kavosh.feature_settings.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.feature_customeTheme.ColorTheme
import ir.dekot.kavosh.feature_customeTheme.CustomColorTheme
import ir.dekot.kavosh.feature_customeTheme.PredefinedColorTheme
import ir.dekot.kavosh.feature_customeTheme.Theme
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ApplicationRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.CameraRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ConnectivityRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.DeviceInfoRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.HardwareRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.PowerRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SystemRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.TestingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val systemRepository: SystemRepository,
) : ViewModel() {

    // --- State های مربوط به تنظیمات ---
    private val _themeState = MutableStateFlow(Theme.SYSTEM)
    val themeState: StateFlow<Theme> = _themeState.asStateFlow()

    private val _isReorderingEnabled = MutableStateFlow(true)
    val isReorderingEnabled: StateFlow<Boolean> = _isReorderingEnabled.asStateFlow()

    private val _isDynamicThemeEnabled = MutableStateFlow(true)
    val isDynamicThemeEnabled: StateFlow<Boolean> = _isDynamicThemeEnabled.asStateFlow()

    private val _appVersion = MutableStateFlow("")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _language = MutableStateFlow("fa")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _languageChangeRequest = MutableSharedFlow<Unit>()
    val languageChangeRequest = _languageChangeRequest.asSharedFlow()

    // --- State های جدید برای تم‌های رنگی ---
    private val _currentColorTheme = MutableStateFlow<ColorTheme?>(null)
    val currentColorTheme: StateFlow<ColorTheme?> = _currentColorTheme.asStateFlow()

    init {
        // بارگذاری تنظیمات ذخیره شده در شروع
        _themeState.value = settingsRepository.getTheme()
        _isReorderingEnabled.value = settingsRepository.isReorderingEnabled()
        _isDynamicThemeEnabled.value = settingsRepository.isDynamicThemeEnabled()
        _language.value = settingsRepository.getLanguage()
        _appVersion.value = systemRepository.getAppVersion()
        _currentColorTheme.value = settingsRepository.getCurrentColorTheme()
    }

    // --- توابع مربوط به رویدادهای کاربر ---

    fun onLanguageSelected(lang: String) {
        // اگر زبان تغییری نکرده، کاری انجام نده
        if (lang == _language.value) return

        viewModelScope.launch {
            settingsRepository.saveLanguage(lang)
            _language.value = lang
            // ارسال رویداد برای بازسازی Activity
            _languageChangeRequest.emit(Unit)
        }
    }

    fun onThemeSelected(theme: Theme) {
        _themeState.value = theme
        viewModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    fun onDynamicThemeToggled(enabled: Boolean) {
        _isDynamicThemeEnabled.value = enabled
        viewModelScope.launch {
            settingsRepository.setDynamicThemeEnabled(enabled)
        }
    }

    fun onReorderingToggled(enabled: Boolean) {
        _isReorderingEnabled.value = enabled
        viewModelScope.launch {
            settingsRepository.setReorderingEnabled(enabled)
        }
    }

    /**
     * پاک کردن کش اطلاعات دستگاه
     */
    fun clearCache() {
        viewModelScope.launch {
            settingsRepository.clearDeviceInfoCache()
        }
    }

    /**
     * بازنشانی همه تنظیمات به حالت پیش‌فرض
     */
    fun resetAllSettings() {
        viewModelScope.launch {
            // بازنشانی تم به حالت سیستم
            settingsRepository.saveTheme(Theme.SYSTEM)
            _themeState.value = Theme.SYSTEM

            // بازنشانی زبان به فارسی
            settingsRepository.saveLanguage("fa")
            _language.value = "fa"

            // بازنشانی تم پویا به فعال
            settingsRepository.setDynamicThemeEnabled(true)
            _isDynamicThemeEnabled.value = true

            // بازنشانی قابلیت جابجایی به فعال
            settingsRepository.setReorderingEnabled(true)
            _isReorderingEnabled.value = true

            // پاک کردن کش
            settingsRepository.clearDeviceInfoCache()

            // ارسال رویداد برای بازسازی Activity (در صورت تغییر زبان)
            _languageChangeRequest.emit(Unit)
        }
    }

    // --- توابع جدید برای مدیریت تم‌های رنگی ---

    /**
     * انتخاب تم رنگی از پیش تعریف شده
     */
    fun selectPredefinedColorTheme(colorTheme: PredefinedColorTheme) {
        viewModelScope.launch {
            settingsRepository.savePredefinedColorTheme(colorTheme)
            _currentColorTheme.value = colorTheme.toColorTheme()
        }
    }

    /**
     * انتخاب تم رنگی سفارشی
     */
    fun selectCustomColorTheme(customTheme: CustomColorTheme) {
        viewModelScope.launch {
            settingsRepository.saveCustomColorTheme(customTheme)
            _currentColorTheme.value = customTheme.toColorTheme()
        }
    }

    /**
     * بازنشانی تم رنگی به حالت پیش‌فرض
     */
    fun resetColorTheme() {
        viewModelScope.launch {
            settingsRepository.resetColorTheme()
            _currentColorTheme.value = null
        }
    }

    /**
     * دریافت لیست تم‌های رنگی از پیش تعریف شده
     */
    fun getPredefinedColorThemes(): List<PredefinedColorTheme> {
        return PredefinedColorTheme.entries
    }

    /**
     * بررسی اینکه آیا تم رنگی سفارشی انتخاب شده یا خیر
     */
    fun hasCustomColorTheme(): Boolean {
        return settingsRepository.hasCustomColorTheme()
    }

    companion object {
        /**
         * متد استاتیک برای دسترسی به زبان قبل از اینکه ViewModel ساخته شود.
         * این متد برای `attachBaseContext` در MainActivity ضروری است.
         */
        fun getSavedLanguage(context: Context): String {
            val prefs = context.getSharedPreferences("device_inspector_prefs", Context.MODE_PRIVATE)
            return prefs.getString("app_language", "fa") ?: "fa"
        }
    }
}