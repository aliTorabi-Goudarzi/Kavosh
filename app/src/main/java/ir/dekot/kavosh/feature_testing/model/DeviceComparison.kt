package ir.dekot.kavosh.feature_testing.model

import kotlinx.serialization.Serializable

/**
 * مقایسه دستگاه فعلی با دستگاه‌های مشابه
 */
@Serializable
data class DeviceComparison(
    val currentDevice: DeviceProfile,
    val comparedDevices: List<DeviceProfile>,
    val comparisonResults: List<ComparisonResult>,
    val overallComparison: OverallComparison,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * پروفایل دستگاه
 */
@Serializable
data class DeviceProfile(
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val specifications: DeviceSpecs,
    val performanceScore: Int,
    val marketPrice: Double? = null,
    val releaseYear: Int? = null,
    val isCurrentDevice: Boolean = false
)

/**
 * مشخصات دستگاه
 */
@Serializable
data class DeviceSpecs(
    val cpu: CpuSpec,
    val gpu: GpuSpec? = null,
    val ram: RamSpec,
    val storage: StorageSpec,
    val display: DisplaySpec,
    val battery: BatterySpec,
    val camera: CameraSpec? = null
)

/**
 * مشخصات پردازنده
 */
@Serializable
data class CpuSpec(
    val name: String,
    val architecture: String,
    val cores: Int,
    val maxFrequency: Double, // GHz
    val process: String? = null // مثل "7nm"
)

/**
 * مشخصات GPU
 */
@Serializable
data class GpuSpec(
    val name: String,
    val frequency: Double? = null
)

/**
 * مشخصات رم
 */
@Serializable
data class RamSpec(
    val totalSize: Long, // MB
    val type: String? = null // مثل "LPDDR5"
)

/**
 * مشخصات حافظه
 */
@Serializable
data class StorageSpec(
    val totalSize: Long, // GB
    val type: String, // مثل "UFS 3.1"
    val readSpeed: Double? = null, // MB/s
    val writeSpeed: Double? = null // MB/s
)

/**
 * مشخصات نمایشگر
 */
@Serializable
data class DisplaySpec(
    val sizeInches: Double,
    val resolution: String,
    val refreshRate: Int,
    val pixelDensity: Int
)

/**
 * مشخصات باتری
 */
@Serializable
data class BatterySpec(
    val capacity: Int, // mAh
    val fastCharging: Boolean = false,
    val chargingSpeed: Int? = null // Watts
)

/**
 * مشخصات دوربین
 */
@Serializable
data class CameraSpec(
    val mainCamera: Int, // MP
    val frontCamera: Int? = null, // MP
    val features: List<String> = emptyList()
)

/**
 * نتیجه مقایسه در هر دسته
 */
@Serializable
data class ComparisonResult(
    val category: ComparisonCategory,
    val currentScore: Double,
    val averageScore: Double,
    val bestScore: Double,
    val worstScore: Double,
    val ranking: Int, // رتبه در بین دستگاه‌های مقایسه شده
    val totalDevices: Int,
    val unit: String,
    val description: String
)

/**
 * دسته‌های مقایسه
 */
@Serializable
enum class ComparisonCategory {
    CPU_PERFORMANCE,
    GPU_PERFORMANCE,
    RAM_SIZE,
    STORAGE_SIZE,
    STORAGE_SPEED,
    DISPLAY_QUALITY,
    BATTERY_CAPACITY,
    CAMERA_QUALITY,
    OVERALL_PERFORMANCE,
    PRICE_PERFORMANCE
}

/**
 * مقایسه کلی
 */
@Serializable
data class OverallComparison(
    val overallRanking: Int,
    val totalDevices: Int,
    val percentile: Double, // درصدک
    val strengths: List<String>, // نقاط قوت
    val weaknesses: List<String>, // نقاط ضعف
    val recommendation: String
)
