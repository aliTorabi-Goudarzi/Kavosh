package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.infoCards.BatteryInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.CpuInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.DisplayInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.GpuInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.RamInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.SensorInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.StorageInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.SystemInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.ThermalInfoCard
import ir.dekot.kavosh.ui.screen.infoCards.NetworkInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.ui.viewmodel.BatteryViewModel
import ir.dekot.kavosh.ui.viewmodel.SocViewModel

// --- صفحه جزئیات (Detail) ---
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    category: InfoCategory,
    deviceInfoViewModel: DeviceInfoViewModel, // برای اطلاعات استاتیک و ناوبری
    batteryViewModel: BatteryViewModel,     // برای اطلاعات باتری
    socViewModel: SocViewModel,             // برای اطلاعات SOC
    onBackClick: () -> Unit
) {
    val deviceInfo by deviceInfoViewModel.deviceInfo.collectAsState()
    val thermalDetails by deviceInfoViewModel.thermalDetails.collectAsState()
    val context = LocalContext.current

    // --- دریافت stateها از ViewModelled مربوطه ---
    val batteryInfo by batteryViewModel.batteryInfo.collectAsState()
    val liveCpuFrequencies by socViewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by socViewModel.liveGpuLoad.collectAsState()

    // مدیریت چرخه حیات ViewModelهای جدید
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
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    // حالا از state مربوط به BatteryViewModel استفاده می‌کند
                    item { BatteryInfoCard(batteryInfo) }
                }

                InfoCategory.SENSORS -> {
                    items(deviceInfo.sensors) { sensor -> SensorInfoCard(info = sensor) }
                }

                InfoCategory.THERMAL -> {
                    items(thermalDetails) { thermalInfo -> ThermalInfoCard(info = thermalInfo) }
                }

                InfoCategory.NETWORK -> {
                    item { NetworkInfoCard(deviceInfo.network) } // <-- نمایش کارت جدید
                }
            }
        }
    }
}