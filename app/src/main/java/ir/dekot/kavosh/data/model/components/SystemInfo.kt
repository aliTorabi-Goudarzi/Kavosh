package ir.dekot.kavosh.data.model.components

data class SystemInfo(
    val androidVersion: String = "نامشخص",
    val sdkLevel: String = "نامشخص",
    val buildNumber: String = "نامشخص",
    val isRooted: Boolean = false
)