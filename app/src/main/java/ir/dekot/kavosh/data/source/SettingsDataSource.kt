package ir.dekot.kavosh.data.source

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای مدیریت تنظیمات برنامه با استفاده از SharedPreferences.
 */
@Singleton
class SettingsDataSource @Inject constructor(@ApplicationContext context: Context) {

    // یک نمونه از SharedPreferences ایجاد می‌کنیم
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("device_inspector_prefs", Context.MODE_PRIVATE)
    }

    /**
     * چک می‌کند که آیا اولین اجرای برنامه است یا نه.
     * @return true اگر اولین اجرا باشد.
     */
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean("is_first_launch", true) // مقدار پیش‌فرض true است
    }

    /**
     * وضعیت "اولین اجرا" را به "انجام شده" تغییر می‌دهد.
     */
    fun setFirstLaunchCompleted() {
        prefs.edit { putBoolean("is_first_launch", false) }
    }
}