package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.infoCards.*
import ir.dekot.kavosh.ui.viewmodel.BatteryViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.ui.viewmodel.SocViewModel
import ir.dekot.kavosh.util.InfoFormatter
import ir.dekot.kavosh.util.shareText

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DetailScreen(
    category: InfoCategory,
    deviceInfoViewModel: DeviceInfoViewModel,
    batteryViewModel: BatteryViewModel,
    socViewModel: SocViewModel,
    onBackClick: () -> Unit
) {
    val deviceInfo by deviceInfoViewModel.deviceInfo.collectAsState()
    val batteryInfo by batteryViewModel.batteryInfo.collectAsState()
    val thermalDetails by deviceInfoViewModel.thermalDetails.collectAsState()
    val liveCpuFrequencies by socViewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by socViewModel.liveGpuLoad.collectAsState()

    val context = LocalContext.current

    // کدهای مربوط به وضعیت و افکت انیمیشن به طور کامل حذف شدند

    DisposableEffect(key1 = category) {
        when (category) {
            InfoCategory.BATTERY -> batteryViewModel.registerBatteryReceiver(context)
            InfoCategory.SOC -> socViewModel.startSocPolling()
            else -> {}
        }
        onDispose {
            when (category) {
                InfoCategory.BATTERY -> batteryViewModel.unregisterBatteryReceiver(context)
                InfoCategory.SOC -> socViewModel.stopSocPolling()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getCategoryTitle(category)) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = {
                        val textToShare = InfoFormatter.formatInfoForSharing(category, deviceInfo, batteryInfo)
                        shareText(context, textToShare)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Info")
                    }
                }
            )
        }
    ) { paddingValues ->
        // LazyColumn حالا بدون هیچ‌گونه انیمیشن والد نمایش داده می‌شود
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
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
    }
}