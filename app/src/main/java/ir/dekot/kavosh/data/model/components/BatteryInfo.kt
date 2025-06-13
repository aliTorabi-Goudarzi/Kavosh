package ir.dekot.kavosh.data.model.components

data class BatteryInfo(
    val health: String = "نامشخص",
    val level: Int = 0,
    val status: String = "نامشخص",
    val technology: String = "نامشخص",
    val temperature: String = "0 °C",
    val voltage: String = "0 V"
)