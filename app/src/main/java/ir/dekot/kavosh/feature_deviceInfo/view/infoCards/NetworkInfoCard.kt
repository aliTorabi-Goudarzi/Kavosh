package ir.dekot.kavosh.feature_deviceInfo.view.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.core.ui.shared_components.SectionTitleInCard
import ir.dekot.kavosh.feature_deviceInfo.model.NetworkInfo
import ir.dekot.kavosh.feature_deviceInfo.view.InfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.InfoRow

@Composable
fun NetworkInfoCard(
    info: NetworkInfo,
    downloadSpeed: String,
    uploadSpeed: String
) {
    InfoCard(stringResource(R.string.network_info_title)) {
        InfoRow(stringResource(R.string.network_download_speed), downloadSpeed)
        InfoRow(stringResource(R.string.network_upload_speed), uploadSpeed)
        InfoRow(
            stringResource(R.string.network_hotspot_status),
            stringResource(if (info.isHotspotEnabled) R.string.label_on else R.string.label_off)
        )
        InfoRow(stringResource(R.string.network_connection_type), info.networkType)
        InfoRow(stringResource(R.string.network_ipv4), info.ipAddressV4)
        InfoRow(stringResource(R.string.network_ipv6), info.ipAddressV6)

        if (info.networkType == "Wi-Fi") {
            SectionTitleInCard(title = stringResource(R.string.network_wifi_details))
            InfoRow(stringResource(R.string.network_ssid), info.ssid)
            InfoRow(stringResource(R.string.network_bssid), info.bssid)
            InfoRow(stringResource(R.string.network_signal_strength), info.wifiSignalStrength)
            InfoRow(stringResource(R.string.network_link_speed), info.linkSpeed)
            InfoRow(stringResource(R.string.network_dns1), info.dns1)
            InfoRow(stringResource(R.string.network_dns2), info.dns2)
        } else if(info.networkType != stringResource(R.string.label_disconnected)) {
            SectionTitleInCard(title = stringResource(R.string.network_mobile_details))
            InfoRow(stringResource(R.string.network_operator), info.networkOperator)
            InfoRow(stringResource(R.string.network_signal_strength), info.mobileSignalStrength)
        }
    }
}