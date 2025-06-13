package ir.dekot.kavosh.data.model.components

data class GpuInfo(
    val model: String = "نامشخص",
    val vendor: String = "نامشخص",
    val loadPercentage: Int? = null // فیلد جدید برای درصد لود
)