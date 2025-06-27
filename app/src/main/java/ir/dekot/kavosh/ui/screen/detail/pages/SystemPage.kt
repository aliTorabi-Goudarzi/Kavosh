package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ir.dekot.kavosh.ui.screen.detail.infoCards.SystemInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SystemPage(viewModel: DeviceInfoViewModel) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    SystemInfoCard(deviceInfo.system)
}