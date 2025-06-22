package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable

@Immutable
data class ThermalInfo(
    val type: String,
    val temperature: String
)