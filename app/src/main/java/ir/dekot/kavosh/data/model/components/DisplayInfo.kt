package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable

@Immutable
data class DisplayInfo(
    val resolution: String = "0x0",
    val density: String = "0 dpi",
    val refreshRate: String = "0 Hz"
)