package ir.dekot.kavosh.feature_deviceInfo.model

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات نمایشگر
 * مسئول دریافت مشخصات نمایشگر شامل رزولوشن، DPI و نرخ تازه‌سازی
 */
@Suppress("DEPRECATION")
@Singleton
class DisplayDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * دریافت اطلاعات نمایشگر
     * @param activity Activity مورد نیاز برای دسترسی به اطلاعات نمایش
     * @return اطلاعات کامل نمایشگر شامل رزولوشن، DPI و نرخ تازه‌سازی
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun getDisplayInfo(activity: Activity): DisplayInfo {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val refreshRate = activity.display?.refreshRate ?: 60.0f
        return DisplayInfo(
            resolution = "${displayMetrics.heightPixels}x${displayMetrics.widthPixels}",
            density = "${displayMetrics.densityDpi} dpi",
            refreshRate = "${DecimalFormat("#.##").format(refreshRate)} Hz"
        )
    }
}
