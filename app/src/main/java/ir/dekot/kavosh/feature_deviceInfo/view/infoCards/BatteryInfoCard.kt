package ir.dekot.kavosh.feature_deviceInfo.view.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.view.InfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.InfoRow
import ir.dekot.kavosh.feature_deviceInfo.model.BatteryInfo


@Composable
fun BatteryInfoCard(info: BatteryInfo) {
    InfoCard(stringResource(R.string.category_battery)) {
        InfoRow(stringResource(R.string.battery_health), info.health)
        InfoRow(stringResource(R.string.battery_level), stringResource(R.string.unit_format_percent, info.level))
        InfoRow(stringResource(R.string.battery_status), info.status)
        InfoRow(stringResource(R.string.battery_technology), info.technology)
        InfoRow(stringResource(R.string.battery_temperature), info.temperature)
        InfoRow(stringResource(R.string.battery_voltage), info.voltage)
    }
}