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
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.NetworkInterface
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
     * وضعیت Hotspot (Tethering) را بررسی می‌کند.
     * از آنجایی که هیچ API عمومی و مستقیمی برای این کار وجود ندارد،
     * از Reflection برای فراخوانی متدهای داخلی و مخفی اندروید استفاده می‌کنیم.
     * این روش یک "ترفند" رایج اما شکننده است.
     */
    private fun isTetheringActive(): Boolean {
        try {
            // این متد در اکثر دستگاه‌ها وجود دارد
            val method = connectivityManager.javaClass.getMethod("isTetheringOn")
            return method.invoke(connectivityManager) as? Boolean == true
        } catch (_: Exception) {
            try {
                // این متد در برخی دستگاه‌های قدیمی‌تر استفاده می‌شود
                val method = wifiManager.javaClass.getMethod("isWifiApEnabled")
                return method.invoke(wifiManager) as? Boolean == true
            } catch (_: Exception) {
                return false
            }
        }
    }

    /**
     * اطلاعات کامل شبکه را با بررسی تمام اینترفیس‌های فعال دستگاه برمی‌گرداند.
     */
    @SuppressLint("MissingPermission")
    fun getNetworkInfo(): NetworkInfo {
        val isHotspotOn = isTetheringActive()
        val (ipv4, ipv6) = getIpAddresses()

        // به جای یک شبکه فعال، تمام شبکه‌ها را بررسی می‌کنیم تا دقیق‌ترین اطلاعات را پیدا کنیم
        for (network in connectivityManager.allNetworks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue

            // بررسی اتصال وای‌فای
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiInfo = wifiManager.connectionInfo
                val dhcpInfo = wifiManager.dhcpInfo

                return NetworkInfo(
                    networkType = "Wi-Fi",
                    ipAddressV4 = ipv4,
                    ipAddressV6 = ipv6,
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
                    networkType = getMobileNetworkType(),
                    ipAddressV4 = ipv4,
                    ipAddressV6 = ipv6,
                    isHotspotEnabled = isHotspotOn,
                    networkOperator = telephonyManager.networkOperatorName,
                    mobileSignalStrength = getMobileSignalStrength()
                )
            }
        }

        // اگر هیچ شبکه‌ای پیدا نشد
        return NetworkInfo(isHotspotEnabled = isHotspotOn)
    }

    /**
     * آدرس‌های IPv4 و IPv6 دستگاه را پیدا می‌کند.
     * این تابع با رویکرد Functional بازنویسی شده تا خواناتر باشد.
     */
    private fun getIpAddresses(): Pair<String, String> {
        try {
            val allAddresses = Collections.list(NetworkInterface.getNetworkInterfaces())
                .flatMap { networkInterface ->
                    Collections.list(networkInterface.inetAddresses)
                        .filter { !it.isLoopbackAddress && it.hostAddress != null }
                }

            val ipv4 = allAddresses.firstOrNull { it is Inet4Address }?.hostAddress ?: "نامشخص"

            val ipv6 = allAddresses.firstOrNull { it is Inet6Address }?.let {
                val rawAddress = it.hostAddress
                // حذف scope id از انتهای آدرس IPv6 (مثال: %wlan0)
                val scopeIndex = rawAddress.indexOf('%')
                if (scopeIndex > 0) rawAddress.substring(0, scopeIndex) else rawAddress
            }?.uppercase() ?: "نامشخص"

            return ipv4 to ipv6
        } catch (_: Exception) {
            // در صورت بروز هرگونه خطا، مقادیر پیش‌فرض برگردانده می‌شود
            return "نامشخص" to "نامشخص"
        }
    }

    private fun intToIp(i: Int): String {
        return (i and 0xFF).toString() + "." +
                (i shr 8 and 0xFF) + "." +
                (i shr 16 and 0xFF) + "." +
                (i shr 24 and 0xFF)
    }

    private fun getMobileSignalStrength(): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "نیازمند مجوز"
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val dbm = telephonyManager.signalStrength?.cellSignalStrengths?.firstOrNull()?.dbm ?: "N/A"
            "$dbm dBm"
        } else {
            // استفاده از API منسوخ شده برای دستگاه‌های قدیمی‌تر
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

    private fun getMobileNetworkType(): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "نیازمند مجوز"
        }
        // استفاده از API منسوخ شده dataNetworkType چون networkType نیاز به API 24 دارد
        // و اطلاعات دقیق‌تری در لحظه می‌دهد.
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