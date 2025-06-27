package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.detail.infoCards.CpuInfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.GpuInfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.RamInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SocPage(viewModel: DeviceInfoViewModel) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val liveCpuFrequencies by viewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by viewModel.liveGpuLoad.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CpuInfoCard(deviceInfo.cpu, liveCpuFrequencies)
        GpuInfoCard(deviceInfo.gpu, liveGpuLoad)
        RamInfoCard(deviceInfo.ram)
    }
}