package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable

@Immutable
data class WifiScanResult(
    val ssid: String, // نام شبکه
    val bssid: String, // آدرس MAC
    val capabilities: String, // نوع امنیت
    val level: Int, // قدرت سیگنال (dBm)
    val frequency: Int // فرکانس (MHz)
)