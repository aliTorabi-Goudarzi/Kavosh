package ir.dekot.kavosh.data.source

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.util.formatSizeOrSpeed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return RamInfo(
            // *** تغییر کلیدی: پاس دادن context ***
            total = formatSizeOrSpeed(context, memoryInfo.totalMem),
            available = formatSizeOrSpeed(context, memoryInfo.availMem)
        )
    }

    fun getStorageInfo(): StorageInfo {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = internalStat.blockCountLong * internalStat.blockSizeLong
        val availableBytes = internalStat.availableBlocksLong * internalStat.blockSizeLong
        return StorageInfo(
            // *** تغییر کلیدی: پاس دادن context ***
            total = formatSizeOrSpeed(context, totalBytes),
            available = formatSizeOrSpeed(context, availableBytes)
        )
    }
}