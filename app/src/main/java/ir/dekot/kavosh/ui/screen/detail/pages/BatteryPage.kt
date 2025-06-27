package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ir.dekot.kavosh.ui.screen.detail.infoCards.BatteryInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun BatteryPage(viewModel: DeviceInfoViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    BatteryInfoCard(batteryInfo)
}