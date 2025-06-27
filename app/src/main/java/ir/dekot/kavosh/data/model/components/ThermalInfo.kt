package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ThermalInfo(
    val type: String,
    val temperature: String
)