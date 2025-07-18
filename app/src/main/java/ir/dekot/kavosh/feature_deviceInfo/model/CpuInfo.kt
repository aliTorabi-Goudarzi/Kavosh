package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CpuInfo(
    val model: String = "نامشخص",
    val architecture: String = "نامشخص",
    val coreCount: Int = 0,
    val process: String = "نامشخص",
    val topology: String = "نامشخص",
    val clockSpeedRanges: List<String> = emptyList(),
    // فیلد جدید برای نگهداری حداکثر فرکانس هر هسته (بر حسب KHz)
    val maxFrequenciesKhz: List<Long> = emptyList(),
    val liveFrequencies: List<String> = List(coreCount) { "خوابیده" }
)