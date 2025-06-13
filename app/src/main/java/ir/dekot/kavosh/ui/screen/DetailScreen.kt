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
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

// --- صفحه جزئیات (Detail) ---
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    category: InfoCategory,
    viewModel: DeviceInfoViewModel,
    onBackClick: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val liveCpuFrequencies by viewModel.liveCpuFrequencies.collectAsState() // <-- این خط را اضافه کنید
    val liveGpuLoad by viewModel.liveGpuLoad.collectAsState()           // <-- این خط را هم اضافه کنید
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val thermalDetails by viewModel.thermalDetails.collectAsState() // <-- این خط را اضافه کنید
    val context = LocalContext.current

    // رجیستر کردن گیرنده باتری فقط زمانی که وارد صفحه باتری می‌شویم
    DisposableEffect(key1 = category) {
        // این بلوک کد زمانی اجرا می‌شود که کاربر وارد صفحه جزئیات می‌شود
        when (category) {
            InfoCategory.BATTERY -> viewModel.registerBatteryReceiver(context)
            InfoCategory.SOC -> viewModel.startSocPolling() // <-- شروع آپدیت لحظه‌ای برای SOC
            else -> { /* برای بقیه دسته‌بندی‌ها کاری انجام نده */
            }
        }

        // onDispose زمانی اجرا می‌شود که کاربر از این صفحه خارج می‌شود (مثلا دکمه بازگشت را می‌زند)
        onDispose {
            when (category) {
                InfoCategory.BATTERY -> viewModel.unregisterBatteryReceiver(context)
                InfoCategory.SOC -> viewModel.stopSocPolling() // <-- توقف آپدیت لحظه‌ای برای SOC
                else -> { /* کاری انجام نده */
                }
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
                    item {
                        CpuInfoCard(
                            deviceInfo.cpu,
                            liveCpuFrequencies
                        )
                    } // state جدید را به عنوان پارامتر دوم پاس می‌دهیم
                    item {
                        GpuInfoCard(
                            deviceInfo.gpu,
                            liveGpuLoad
                        )
                    } // لود زنده را به عنوان پارامتر دوم پاس می‌دهیم
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
                    items(deviceInfo.sensors) { sensor ->
                        SensorInfoCard(info = sensor)
                    }
                }

                InfoCategory.THERMAL -> {
                    // از لیست ترکیبی جدید استفاده می‌کنیم
                    items(thermalDetails) { thermalInfo ->
                        ThermalInfoCard(info = thermalInfo)
                    }
                }
            }
        }
    }
}