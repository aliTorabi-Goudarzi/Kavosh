package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SimInfo(
    val slotIndex: Int,
    val subscriptionId: Int,
    val carrierName: String,
    val countryIso: String,
    val mobileNetworkCode: String,
    val mobileCountryCode: String,
    val isRoaming: Boolean,
    val dataRoaming: String
)