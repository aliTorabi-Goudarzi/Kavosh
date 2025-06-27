package ir.dekot.kavosh.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DeviceInfoRepository
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

    init {
        // بارگذاری تنظیمات ذخیره شده در شروع
        _themeState.value = repository.getTheme()
        _isReorderingEnabled.value = repository.isReorderingEnabled()
        _isDynamicThemeEnabled.value = repository.isDynamicThemeEnabled()
        _language.value = repository.getLanguage()
        _appVersion.value = repository.getAppVersion()
    }

    // --- توابع مربوط به رویدادهای کاربر ---

    fun onLanguageSelected(lang: String) {
        // اگر زبان تغییری نکرده، کاری انجام نده
        if (lang == _language.value) return

        viewModelScope.launch {
            repository.saveLanguage(lang)
            _language.value = lang
            // ارسال رویداد برای بازسازی Activity
            _languageChangeRequest.emit(Unit)
        }
    }

    fun onThemeSelected(theme: Theme) {
        _themeState.value = theme
        viewModelScope.launch {
            repository.saveTheme(theme)
        }
    }

    fun onDynamicThemeToggled(enabled: Boolean) {
        _isDynamicThemeEnabled.value = enabled
        viewModelScope.launch {
            repository.setDynamicThemeEnabled(enabled)
        }
    }

    fun onReorderingToggled(enabled: Boolean) {
        _isReorderingEnabled.value = enabled
        viewModelScope.launch {
            repository.setReorderingEnabled(enabled)
        }
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