package ir.dekot.kavosh.feature_testing.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * نتیجه بررسی سلامت کلی دستگاه
 * شامل بررسی مختلف جنبه‌های سخت‌افزاری و نرم‌افزاری
 */
@Serializable
data class HealthCheckResult(
    val overallScore: Int, // امتیاز کلی از 0 تا 100
    val overallStatus: HealthStatus,
    val checks: List<HealthCheck>,
    val recommendations: List<String>,
    val lastCheckTime: Long = System.currentTimeMillis()
)

/**
 * وضعیت کلی سلامت دستگاه
 */
@Serializable
enum class HealthStatus {
    EXCELLENT,  // عالی (90-100)
    GOOD,       // خوب (70-89)
    FAIR,       // متوسط (50-69)
    POOR,       // ضعیف (30-49)
    CRITICAL    // بحرانی (0-29)
}

/**
 * هر بررسی جداگانه در Health Check
 */
@Serializable
data class HealthCheck(
    val category: HealthCategory,
    val name: String,
    val score: Int, // امتیاز از 0 تا 100
    val status: HealthStatus,
    val description: String,
    val details: String? = null,
    val recommendation: String? = null
)

/**
 * دسته‌بندی بررسی‌های سلامت
 */
@Serializable
enum class HealthCategory {
    PERFORMANCE,    // عملکرد
    STORAGE,        // حافظه
    BATTERY,        // باتری
    TEMPERATURE,    // دما
    MEMORY,         // رم
    NETWORK,        // شبکه
    SECURITY,       // امنیت
    SYSTEM          // سیستم
}

/**
 * توصیه‌های بهبود عملکرد
 */
@Serializable
data class HealthRecommendation(
    val category: HealthCategory,
    val priority: RecommendationPriority,
    val title: String,
    val description: String,
    val actionText: String? = null
)

/**
 * اولویت توصیه‌ها
 */
@Serializable
enum class RecommendationPriority {
    HIGH,       // بالا - نیاز به اقدام فوری
    MEDIUM,     // متوسط - توصیه می‌شود
    LOW         // پایین - اختیاری
}

/**
 * خلاصه نتیجه تست برای نمایش در تاریخچه
 */
@Serializable
data class HealthCheckSummary(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long,
    val overallScore: Int,
    val overallStatus: HealthStatus,
    val deviceName: String,
    val androidVersion: String,
    // جزئیات کامل برای گزارش
    val checks: List<HealthCheck> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val testDuration: Long = 0L, // مدت زمان تست به میلی‌ثانیه
    val criticalIssuesCount: Int = 0,
    val warningsCount: Int = 0
)
