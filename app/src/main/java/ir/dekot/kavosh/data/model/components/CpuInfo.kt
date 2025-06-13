package ir.dekot.kavosh.data.model.components

data class CpuInfo(
    val model: String = "نامشخص",
    val architecture: String = "نامشخص",
    val coreCount: Int = 0,
    val process: String = "نامشخص", // فیلد جدید برای لیتوگرافی
    val topology: String = "نامشخص", // فیلد جدید برای توپولوژی
    val clockSpeedRanges: List<String> = emptyList(), // فیلد جدید برای بازه سرعت
    val liveFrequencies: List<String> = List(coreCount) { "خوابیده" } // برای فرکانس‌های زنده
)