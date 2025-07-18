package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class BatteryInfo(
    val health: String = "نامشخص",
    val level: Int = 0,
    val status: String = "نامشخص",
    val technology: String = "نامشخص",
    val temperature: String = "0 °C",
    val voltage: String = "0 V",
    // --- فیلدهای جدید ---
    val designCapacity: Int = 0, // به میلی‌آمپر ساعت (mAh)
    val actualCapacity: Double = 0.0, // به میلی‌آمپر ساعت (mAh)
    val chargeCurrent: Int = 0, // به میلی‌آمپر (mA)
    val chargePower: Float = 0.0f // به وات (W)
)