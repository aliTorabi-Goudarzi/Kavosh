package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class RamInfo(
    val total: String = "0 GB",
    val available: String = "0 GB"
)