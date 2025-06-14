package ir.dekot.kavosh.data.source

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.NetworkInfo
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION")
@Singleton
class NetworkDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    /**
     * متد Reflection را برای چک کردن چند نام محتمل تقویت می‌کنیم.
     */
    private fun isTetheringActive(): Boolean {
        try {
            val method = connectivityManager.javaClass.getMethod("isTetheringOn")
            return method.invoke(connectivityManager) as? Boolean == true
        } catch (_: Exception) {
            // اگر متد بالا پیدا نشد، متد دیگری را امتحان می‌کنیم (رایج در برخی دستگاه‌ها)
            try {
                val method = wifiManager.javaClass.getMethod("isWifiApEnabled")
                return method.invoke(wifiManager) as? Boolean == true
            } catch (_: Exception) {
                return false
            }
        }
    }

    /**
     * این متد به طور کامل بازنویسی شده تا قوی‌تر عمل کند.
     */
    @SuppressLint("MissingPermission")
    fun getNetworkInfo(): NetworkInfo {
        val isHotspotOn = isTetheringActive()
        val ipAddresses = getIpAddresses()

        // به جای یک شبکه فعال، تمام شبکه‌ها را بررسی می‌کنیم
        for (network in connectivityManager.allNetworks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue

            // بررسی اتصال وای‌فای
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                @Suppress("DEPRECATION")
                val wifiInfo = wifiManager.connectionInfo
                @Suppress("DEPRECATION")
                val dhcpInfo = wifiManager.dhcpInfo

                return NetworkInfo(
                    networkType = "Wi-Fi",
                    ipAddressV4 = ipAddresses.first,
                    ipAddressV6 = ipAddresses.second,
                    isHotspotEnabled = isHotspotOn,
                    ssid = wifiInfo.ssid.removeSurrounding("\""),
                    bssid = wifiInfo.bssid,
                    linkSpeed = "${wifiInfo.linkSpeed} Mbps",
                    wifiSignalStrength = "${wifiInfo.rssi} dBm",
                    dns1 = intToIp(dhcpInfo.dns1),
                    dns2 = intToIp(dhcpInfo.dns2)
                )
            }

            // بررسی اتصال موبایل
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return NetworkInfo(
                    networkType = getNetworkType(),
                    ipAddressV4 = ipAddresses.first,
                    ipAddressV6 = ipAddresses.second,
                    isHotspotEnabled = isHotspotOn,
                    networkOperator = telephonyManager.networkOperatorName,
                    mobileSignalStrength = getMobileSignalStrength()
                )
            }
        }

        // اگر هیچ شبکه‌ای پیدا نشد
        return NetworkInfo(isHotspotEnabled = isHotspotOn)
    }

    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)
    }

    /**
     * این متد حالا هر دو آدرس IPv4 و IPv6 را برمی‌گرداند.
     */
    private fun getIpAddresses(): Pair<String, String> {
        var ipv4 = "نامشخص"
        var ipv6 = "نامشخص"
        try {
            val interfaces = Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        val isIPv4 = (sAddr?.indexOf(':') ?: -1) < 0
                        if (isIPv4 && ipv4 == "نامشخص") {
                            if (sAddr != null) {
                                ipv4 = sAddr.toString()
                            }
                        } else if (!isIPv4 && ipv6 == "نامشخص") {
                            val delim = sAddr?.indexOf('%') // حذف zone
                            ipv6 = if ((delim ?: -1) < 0) sAddr.toString().uppercase() else delim?.let { sAddr.toString().substring(0, it) }
                                ?.uppercase() ?: "نامشخص"
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        return Pair(ipv4, ipv6)
    }

    // ... (متدهای getMobileSignalStrength و getNetworkType بدون تغییر)
    private fun getMobileSignalStrength(): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "نیازمند مجوز"
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val signalStrength = telephonyManager.signalStrength
            val dbm = signalStrength?.cellSignalStrengths?.firstOrNull()?.dbm ?: "N/A"
            "$dbm dBm"
        } else {
            @Suppress("DEPRECATION")
            try {
                val signalStrength = telephonyManager.signalStrength
                val dbm = signalStrength?.let {
                    if (it.isGsm) (it.gsmSignalStrength * 2) - 113 else it.cdmaDbm
                } ?: "N/A"
                "$dbm dBm"
            } catch (_: SecurityException) {
                "نیازمند مجوز"
            }
        }
    }

    private fun getNetworkType(): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "نیازمند مجوز"
        }
        return when (telephonyManager.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "موبایل"
        }
    }
}