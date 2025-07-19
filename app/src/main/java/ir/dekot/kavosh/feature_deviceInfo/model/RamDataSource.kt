package ir.dekot.kavosh.feature_deviceInfo.model

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.core.util.formatSizeOrSpeed
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات RAM
 * مسئول دریافت اطلاعات حافظه RAM و مانیتورینگ استفاده از حافظه
 */
@Singleton
class RamDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    // *** تغییر کلیدی و نهایی در این خط ***
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    /**
     * دریافت اطلاعات حافظه RAM
     * @return اطلاعات کامل RAM شامل کل، استفاده شده و آزاد
     */
    fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return RamInfo(
            total = formatSizeOrSpeed(context, memoryInfo.totalMem),
            available = formatSizeOrSpeed(context, memoryInfo.availMem)
        )
    }
}
