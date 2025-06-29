package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun BatteryPage(viewModel: DeviceInfoViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // کارت اول: اطلاعات عمومی
        InfoCard(stringResource(R.string.category_battery)) {
            InfoRow(stringResource(R.string.battery_level), stringResource(R.string.unit_format_percent, batteryInfo.level))
            InfoRow(stringResource(R.string.battery_status), batteryInfo.status)
            InfoRow(stringResource(R.string.battery_technology), batteryInfo.technology)
            InfoRow(stringResource(R.string.battery_temperature), batteryInfo.temperature)
        }

        // کارت دوم: سلامت و ظرفیت
        InfoCard(stringResource(R.string.battery_health_details)) {
            InfoRow(stringResource(R.string.battery_health), batteryInfo.health)
            if (batteryInfo.actualCapacity > 0) {
                InfoRow(stringResource(R.string.battery_capacity_design), stringResource(R.string.battery_unit_mah, batteryInfo.actualCapacity.roundToInt()))
            }
            if (batteryInfo.designCapacity > 0) {
                InfoRow(stringResource(R.string.battery_capacity_actual), stringResource(R.string.battery_unit_mah, batteryInfo.designCapacity))
            }
            if (batteryInfo.actualCapacity > 0 && batteryInfo.designCapacity > 0) {
                val healthPercentage = (batteryInfo.designCapacity.toDouble() / batteryInfo.actualCapacity * 100).coerceIn(0.0, 100.0)
                InfoRow(stringResource(R.string.battery_health_estimated), stringResource(R.string.unit_format_percent, healthPercentage.roundToInt()))
            }
        }

        // کارت سوم: شارژ و دشارژ
        InfoCard(stringResource(R.string.battery_charge_stats)) {
            InfoRow(stringResource(R.string.battery_charge_current), stringResource(R.string.battery_unit_ma, batteryInfo.chargeCurrent))
            InfoRow(stringResource(R.string.battery_voltage), batteryInfo.voltage)
            InfoRow(stringResource(R.string.battery_charge_power), stringResource(R.string.battery_unit_watt, batteryInfo.chargePower))
        }
    }
}