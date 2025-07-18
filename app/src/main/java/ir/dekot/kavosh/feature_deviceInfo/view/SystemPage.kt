package ir.dekot.kavosh.feature_deviceInfo.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.SystemInfoCard
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SystemPage(viewModel: DeviceInfoViewModel) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    SystemInfoCard(deviceInfo.system)
}