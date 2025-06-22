package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable

@Immutable
data class GpuInfo(
    val model: String = "نامشخص",
    val vendor: String = "نامشخص",
    val loadPercentage: Int? = null // فیلد جدید برای درصد لود
)