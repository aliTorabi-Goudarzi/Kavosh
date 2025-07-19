package ir.dekot.kavosh.feature_deviceInfo.model

import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.core.util.formatSizeOrSpeed
import javax.inject.Inject
import javax.inject.Singleton

/**
 * منبع داده برای اطلاعات ذخیره‌سازی
 * مسئول دریافت اطلاعات حافظه داخلی و خارجی دستگاه
 */
@Singleton
class StorageDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    /**
     * دریافت اطلاعات ذخیره‌سازی
     * @return اطلاعات کامل حافظه داخلی شامل کل و آزاد
     */
    fun getStorageInfo(): StorageInfo {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = internalStat.blockCountLong * internalStat.blockSizeLong
        val availableBytes = internalStat.availableBlocksLong * internalStat.blockSizeLong
        return StorageInfo(
            total = formatSizeOrSpeed(context, totalBytes),
            available = formatSizeOrSpeed(context, availableBytes)
        )
    }
}
