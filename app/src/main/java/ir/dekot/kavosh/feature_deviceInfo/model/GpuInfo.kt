package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class GpuInfo(
    val model: String = "نامشخص",
    val vendor: String = "نامشخص",
    val loadPercentage: Int? = null // فیلد جدید برای درصد لود
)