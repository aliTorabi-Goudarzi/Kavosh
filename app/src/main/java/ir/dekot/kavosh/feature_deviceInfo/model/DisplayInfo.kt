package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DisplayInfo(
    val resolution: String = "0x0",
    val density: String = "0 dpi",
    val refreshRate: String = "0 Hz"
)