package ir.dekot.kavosh.data.source

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.settings.Theme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataSource @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("device_inspector_prefs", Context.MODE_PRIVATE)
    }

    // یک کلید برای ذخیره نام تم در SharedPreferences
    private companion object {
        const val KEY_THEME = "app_theme"
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit { putBoolean("is_first_launch", false) }
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
}