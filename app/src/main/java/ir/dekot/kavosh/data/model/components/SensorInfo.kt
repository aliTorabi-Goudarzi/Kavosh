package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable

@Immutable
data class SensorInfo(
    val name: String,
    val vendor: String,
    // *** فیلد جدید: نوع سنسور برای شناسایی ***
    val type: Int
)