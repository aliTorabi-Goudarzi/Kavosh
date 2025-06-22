package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable

@Immutable
data class RamInfo(
    val total: String = "0 GB",
    val available: String = "0 GB"
)