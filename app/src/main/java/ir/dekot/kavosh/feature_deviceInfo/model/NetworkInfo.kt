package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable /**
 * مدل داده برای نگهداری اطلاعات شبکه.
 */
@Serializable
data class NetworkInfo(
    val networkType: String = "متصل نیست",
    val ipAddressV4: String = "نامشخص", // نام را برای وضوح بیشتر تغییر دادیم
    val ipAddressV6: String = "نامشخص", // فیلد جدید برای IPv6
    val isHotspotEnabled: Boolean = false,
    // Wi-Fi Specific
    val ssid: String = "نامشخص",
    val bssid: String = "نامشخص",
    val linkSpeed: String = "نامشخص",
    val wifiSignalStrength: String = "نامشخص",
    val dns1: String = "نامشخص", // فیلد جدید
    val dns2: String = "نامشخص", // فیلد جدید
    // Mobile Specific
    val networkOperator: String = "نامشخص",
    val mobileSignalStrength: String = "نامشخص"
)