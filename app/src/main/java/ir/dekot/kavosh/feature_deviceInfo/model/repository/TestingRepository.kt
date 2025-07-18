package ir.dekot.kavosh.feature_deviceInfo.model.repository

import ir.dekot.kavosh.feature_deviceInfo.model.MemoryDataSource
import ir.dekot.kavosh.feature_settings.model.SettingsDataSource
import ir.dekot.kavosh.feature_testing.model.SpeedDataPoint
import ir.dekot.kavosh.feature_testing.model.StorageSpeedTestResult
import ir.dekot.kavosh.feature_testing.model.StorageTestSummary
import ir.dekot.kavosh.feature_testing.viewModel.StorageTestViewModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن تست‌ها و تشخیص - مسئول مدیریت تست‌های عملکرد و تاریخچه آن‌ها
 * شامل تست سرعت حافظه، تاریخچه تست‌ها و مدیریت نتایج
 */
@Singleton
class TestingRepository @Inject constructor(
    private val memoryDataSource: MemoryDataSource,
    private val settingsDataSource: SettingsDataSource,
) {

    // --- تست‌های سرعت حافظه ---

    /**
     * انجام تست ساده سرعت حافظه
     * @param onProgress callback برای نمایش پیشرفت تست
     * @return جفت رشته حاوی سرعت نوشتن و خواندن
     */
    fun performStorageSpeedTest(onProgress: (Float) -> Unit): Pair<String, String> =
        memoryDataSource.performStorageSpeedTest(onProgress)

    /**
     * انجام تست پیشرفته سرعت حافظه با فایل ۱ گیگابایتی
     * @param onProgress callback برای نمایش پیشرفت کلی
     * @param onSpeedUpdate callback برای به‌روزرسانی سرعت لحظه‌ای
     * @param onPhaseChange callback برای تغییر فاز تست
     * @param onSpeedHistoryUpdate callback برای به‌روزرسانی تاریخچه سرعت
     * @return نتیجه کامل تست سرعت حافظه
     */
    fun performEnhancedStorageSpeedTest(
        onProgress: (Float) -> Unit,
        onSpeedUpdate: (writeSpeed: Double, readSpeed: Double) -> Unit,
        onPhaseChange: (phase: String) -> Unit,
        onSpeedHistoryUpdate: (writeHistory: List<SpeedDataPoint>, readHistory: List<SpeedDataPoint>) -> Unit
    ): StorageSpeedTestResult = memoryDataSource.performEnhancedStorageSpeedTest(
        onProgress, onSpeedUpdate, onPhaseChange, onSpeedHistoryUpdate
    )

    // --- مدیریت تاریخچه تست‌ها ---

    /**
     * ذخیره تاریخچه تست سرعت حافظه
     * @param history لیست خلاصه تست‌های انجام شده
     */
    fun saveStorageSpeedTestHistory(history: List<StorageTestSummary>) =
        settingsDataSource.saveStorageSpeedTestHistory(history)

    /**
     * دریافت تاریخچه تست سرعت حافظه
     * @return لیست خلاصه تست‌های قبلی
     */
    fun getStorageSpeedTestHistory(): List<StorageTestSummary> =
        settingsDataSource.getStorageSpeedTestHistory()
}
