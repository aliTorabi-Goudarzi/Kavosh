package ir.dekot.kavosh.data.model.diagnostic

import kotlinx.serialization.Serializable

/**
 * امتیاز کلی عملکرد دستگاه
 * بر اساس تست‌های مختلف سخت‌افزاری
 */
@Serializable
data class PerformanceScore(
    val overallScore: Int, // امتیاز کلی از 0 تا 100
    val performanceGrade: PerformanceGrade,
    val categoryScores: List<CategoryScore>,
    val benchmarkResults: List<BenchmarkResult>,
    val deviceRanking: DeviceRanking? = null,
    val lastTestTime: Long = System.currentTimeMillis()
)

/**
 * درجه‌بندی عملکرد
 */
@Serializable
enum class PerformanceGrade {
    S_PLUS,     // S+ (95-100) - فوق‌العاده
    S,          // S (90-94) - عالی
    A_PLUS,     // A+ (85-89) - خیلی خوب
    A,          // A (80-84) - خوب
    B_PLUS,     // B+ (75-79) - بالای متوسط
    B,          // B (70-74) - متوسط
    C_PLUS,     // C+ (65-69) - زیر متوسط
    C,          // C (60-64) - ضعیف
    D,          // D (50-59) - خیلی ضعیف
    F           // F (0-49) - ناکافی
}

/**
 * امتیاز هر دسته عملکرد
 */
@Serializable
data class CategoryScore(
    val category: PerformanceCategory,
    val score: Int, // امتیاز از 0 تا 100
    val grade: PerformanceGrade,
    val details: String,
    val testResults: List<TestResult> = emptyList()
)

/**
 * دسته‌های عملکرد
 */
@Serializable
enum class PerformanceCategory {
    CPU,            // پردازنده
    GPU,            // پردازنده گرافیکی
    RAM,            // حافظه رم
    STORAGE,        // حافظه داخلی
    NETWORK,        // شبکه
    BATTERY,        // باتری
    THERMAL         // مدیریت حرارت
}

/**
 * نتیجه هر تست benchmark
 */
@Serializable
data class BenchmarkResult(
    val testName: String,
    val category: PerformanceCategory,
    val score: Int,
    val unit: String,
    val description: String,
    val duration: Long, // مدت زمان تست به میلی‌ثانیه
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * نتیجه تست‌های جزئی
 */
@Serializable
data class TestResult(
    val name: String,
    val value: Double,
    val unit: String,
    val description: String
)

/**
 * رتبه‌بندی دستگاه
 */
@Serializable
data class DeviceRanking(
    val globalRank: Int,        // رتبه جهانی
    val totalDevices: Int,      // تعداد کل دستگاه‌ها
    val percentile: Double,     // درصدک (0-100)
    val similarDevices: List<SimilarDevice> = emptyList()
)

/**
 * دستگاه‌های مشابه
 */
@Serializable
data class SimilarDevice(
    val name: String,
    val score: Int,
    val difference: Int // اختلاف امتیاز با دستگاه فعلی
)
