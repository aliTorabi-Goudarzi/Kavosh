package ir.dekot.kavosh.feature_testing.model

import android.os.Build
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * نتیجه تست سرعت حافظه
 * شامل اطلاعات کامل تست و تاریخچه سرعت‌ها
 */
@Serializable
data class StorageSpeedTestResult(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val testDuration: Long, // مدت زمان تست به میلی‌ثانیه
    val fileSizeBytes: Long, // اندازه فایل تست به بایت
    val writeSpeed: Double, // سرعت نوشتن به MB/s
    val readSpeed: Double, // سرعت خواندن به MB/s
    val averageWriteSpeed: Double, // میانگین سرعت نوشتن
    val averageReadSpeed: Double, // میانگین سرعت خواندن
    val maxWriteSpeed: Double, // حداکثر سرعت نوشتن
    val maxReadSpeed: Double, // حداکثر سرعت خواندن
    val minWriteSpeed: Double, // حداقل سرعت نوشتن
    val minReadSpeed: Double, // حداقل سرعت خواندن
    val writeSpeedHistory: List<SpeedDataPoint> = emptyList(), // تاریخچه سرعت نوشتن
    val readSpeedHistory: List<SpeedDataPoint> = emptyList(), // تاریخچه سرعت خواندن
    val testStatus: StorageTestStatus = StorageTestStatus.COMPLETED,
    val errorMessage: String? = null
)

/**
 * نقطه داده سرعت برای نمودار زنده
 */
@Serializable
data class SpeedDataPoint(
    val timestamp: Long, // زمان اندازه‌گیری
    val speed: Double, // سرعت به MB/s
    val phase: TestPhase // مرحله تست (نوشتن یا خواندن)
)

/**
 * مراحل مختلف تست
 */
@Serializable
enum class TestPhase {
    PREPARING, // آماده‌سازی
    WRITING, // نوشتن
    READING, // خواندن
    CLEANUP // پاک‌سازی
}

/**
 * وضعیت‌های مختلف تست
 */
@Serializable
enum class StorageTestStatus {
    IDLE, // آماده
    PREPARING, // در حال آماده‌سازی
    RUNNING, // در حال اجرا
    COMPLETED, // تکمیل شده
    FAILED, // ناموفق
    CANCELLED // لغو شده
}

/**
 * اطلاعات خلاصه تست برای نمایش در تاریخچه
 */
@Serializable
data class StorageTestSummary(
    val id: String,
    val timestamp: Long,
    val writeSpeed: Double,
    val readSpeed: Double,
    val testDuration: Long,
    val fileSizeBytes: Long,
    val deviceName: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val androidVersion: String = Build.VERSION.RELEASE
)

/**
 * پیکربندی تست سرعت حافظه
 */
data class StorageTestConfig(
    val fileSizeGB: Int = 1, // اندازه فایل تست به گیگابایت
    val testDurationSeconds: Int = 10, // مدت زمان تست به ثانیه
    val bufferSizeKB: Int = 64, // اندازه بافر به کیلوبایت
    val samplingIntervalMs: Long = 100, // فاصله نمونه‌برداری به میلی‌ثانیه
    val requirePermission: Boolean = true // نیاز به مجوز کاربر
)

/**
 * وضعیت فعلی تست برای UI
 */
data class StorageTestState(
    val status: StorageTestStatus = StorageTestStatus.IDLE,
    val progress: Float = 0f, // پیشرفت از 0 تا 1
    val currentPhase: TestPhase = TestPhase.PREPARING,
    val currentWriteSpeed: Double = 0.0,
    val currentReadSpeed: Double = 0.0,
    val elapsedTime: Long = 0L,
    val statusMessage: String = "",
    val result: StorageSpeedTestResult? = null
)
