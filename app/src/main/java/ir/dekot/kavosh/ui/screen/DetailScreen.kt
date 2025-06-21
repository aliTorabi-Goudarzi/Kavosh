package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.ui.screen.infoCards.BatteryInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.CameraInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.CpuInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.DisplayInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.GpuInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.NetworkInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.RamInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.SensorInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.StorageInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.SystemInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.ThermalInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.util.report.ReportFormatter
import ir.dekot.kavosh.util.shareText

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DetailScreen(
    category: InfoCategory,
    viewModel: DeviceInfoViewModel,
    onBackClick: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val thermalDetails by viewModel.thermalDetails.collectAsState()
    val liveCpuFrequencies by viewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by viewModel.liveGpuLoad.collectAsState()

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                // استفاده مستقیم از خصوصیت title در enum
                title = { Text(category.title) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        val textToShare = ReportFormatter.formatInfoForSharing(category, deviceInfo, batteryInfo)
                        shareText(context, textToShare)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Info")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            CategoryDetailContent(
                category = category,
                deviceInfo = deviceInfo,
                batteryInfo = batteryInfo,
                thermalDetails = thermalDetails,
                liveCpuFrequencies = liveCpuFrequencies,
                liveGpuLoad = liveGpuLoad
            )
        }
    }
}

private fun LazyListScope.CategoryDetailContent(
    category: InfoCategory,
    deviceInfo: DeviceInfo,
    batteryInfo: BatteryInfo,
    thermalDetails: List<ThermalInfo>,
    liveCpuFrequencies: List<String>,
    liveGpuLoad: Int?
) {
    when (category) {
        InfoCategory.SOC -> {
            item { CpuInfoCard(deviceInfo.cpu, liveCpuFrequencies) }
            item { GpuInfoCard(deviceInfo.gpu, liveGpuLoad) }
            item { RamInfoCard(deviceInfo.ram) }
        }
        InfoCategory.DEVICE -> {
            item { DisplayInfoCard(deviceInfo.display) }
            item { StorageInfoCard(deviceInfo.storage) }
        }
        InfoCategory.SYSTEM -> {
            item { SystemInfoCard(deviceInfo.system) }
        }
        InfoCategory.BATTERY -> {
            item { BatteryInfoCard(batteryInfo) }
        }
        InfoCategory.SENSORS -> {
            items(deviceInfo.sensors, key = { it.name }) { sensor ->
                SensorInfoCard(info = sensor)
            }
        }
        InfoCategory.THERMAL -> {
            items(thermalDetails, key = { it.type }) { thermalInfo ->
                ThermalInfoCard(info = thermalInfo)
            }
        }
        InfoCategory.CAMERA -> {
            items(deviceInfo.cameras, key = { it.id }) { camera ->
                CameraInfoCard(info = camera)
            }
        }
        InfoCategory.NETWORK -> {
            item { NetworkInfoCard(deviceInfo.network) }
        }
    }
}