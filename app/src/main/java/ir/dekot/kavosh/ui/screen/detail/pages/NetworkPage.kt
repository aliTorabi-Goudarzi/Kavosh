package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ir.dekot.kavosh.ui.screen.detail.infoCards.NetworkInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun NetworkPage(viewModel: DeviceInfoViewModel) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()

    NetworkInfoCard(
        info = deviceInfo.network,
        downloadSpeed = downloadSpeed,
        uploadSpeed = uploadSpeed
    )
}