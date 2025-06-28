package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable

@Immutable
data class PingResult(
    val host: String = "",
    val outputLines: List<String> = emptyList(),
    val isPinging: Boolean = false,
    val error: String? = null
)