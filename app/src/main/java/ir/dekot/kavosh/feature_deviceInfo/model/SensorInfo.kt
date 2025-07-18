package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SensorInfo(
    val name: String,
    val vendor: String,
    // *** فیلد جدید: نوع سنسور برای شناسایی ***
    val type: Int
)