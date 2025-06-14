package ir.dekot.kavosh.data.source

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.pow

/**
 * منبع داده برای اطلاعات حافظه (RAM و Storage).
 */
@Singleton
class MemoryDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    /**
     * اطلاعات کلی و در دسترس RAM را برمی‌گرداند.
     */
    fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        return RamInfo(
            total = formatSize(totalRam),
            available = formatSize(availableRam)
        )
    }

    /**
     * اطلاعات کلی و در دسترس حافظه داخلی را برمی‌گرداند.
     */
    fun getStorageInfo(): StorageInfo {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = internalStat.blockCountLong * internalStat.blockSizeLong
        val availableBytes = internalStat.availableBlocksLong * internalStat.blockSizeLong
        return StorageInfo(
            total = formatSize(totalBytes),
            available = formatSize(availableBytes)
        )
    }

    /**
     * یک متد کمکی برای فرمت کردن سایز از بایت به واحدهای خواناتر.
     */
    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}