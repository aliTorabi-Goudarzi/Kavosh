package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SystemInfo(
    val androidVersion: String = "نامشخص",
    val sdkLevel: String = "نامشخص",
    val buildNumber: String = "نامشخص",
    val isRooted: Boolean = false
)