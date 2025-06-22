package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.NetworkInfo
import ir.dekot.kavosh.ui.screen.SectionTitleInCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoRow

@Composable
fun NetworkInfoCard(info: NetworkInfo) {
    InfoCard("اطلاعات شبکه") {
        InfoRow("وضعیت هات‌اسپات", if (info.isHotspotEnabled) "روشن" else "خاموش")
        InfoRow("نوع اتصال", info.networkType)
        InfoRow("آدرس IPv4", info.ipAddressV4)
        InfoRow("آدرس IPv6", info.ipAddressV6)

        if (info.networkType == "Wi-Fi") {
            SectionTitleInCard(title = "مشخصات Wi-Fi")
            InfoRow("SSID", info.ssid)
            InfoRow("BSSID", info.bssid)
            InfoRow("قدرت سیگنال", info.wifiSignalStrength)
            InfoRow("سرعت اتصال", info.linkSpeed)
            InfoRow("DNS 1", info.dns1)
            InfoRow("DNS 2", info.dns2)
        } else if(info.networkType != "متصل نیست") {
            SectionTitleInCard(title = "مشخصات شبکه موبایل")
            InfoRow("اپراتور شبکه", info.networkOperator)
            InfoRow("قدرت سیگنال", info.mobileSignalStrength)
        }
    }
}