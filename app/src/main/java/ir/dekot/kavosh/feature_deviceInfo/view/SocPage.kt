package ir.dekot.kavosh.feature_deviceInfo.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.CpuInfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.GpuInfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.RamInfoCard
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SocPage(
    viewModel: DeviceInfoViewModel,
    onNavigateToStressTest: () -> Unit  ) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val liveCpuFrequencies by viewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by viewModel.liveGpuLoad.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CpuInfoCard(deviceInfo.cpu, liveCpuFrequencies)
        GpuInfoCard(deviceInfo.gpu, liveGpuLoad)
        RamInfoCard(deviceInfo.ram)
        Button(
            onClick = onNavigateToStressTest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("CPU Stress Test")
        }
    }
}