package ir.dekot.kavosh.feature_deviceInfo.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getSimInfo(): List<SimInfo> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return emptyList() // اگر مجوز نباشد، لیست خالی برگردان
        }

        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return subscriptionManager.activeSubscriptionInfoList?.mapNotNull { subInfo ->
            val tmForSub = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.createForSubscriptionId(subInfo.subscriptionId)
            } else {
                telephonyManager
            }

            val dataRoaming = when(tmForSub.isDataRoamingEnabled) {
                true -> "Enabled"
                false -> "Disabled"
            }

            @Suppress("DEPRECATION")
            SimInfo(
                slotIndex = subInfo.simSlotIndex,
                subscriptionId = subInfo.subscriptionId,
                carrierName = subInfo.carrierName?.toString() ?: "Unknown",
                countryIso = subInfo.countryIso?.uppercase() ?: "N/A",
                mobileNetworkCode = subInfo.mnc.toString(),
                mobileCountryCode = subInfo.mcc.toString(),
                isRoaming = tmForSub.isNetworkRoaming,
                dataRoaming = dataRoaming
            )
        } ?: emptyList()
    }
}