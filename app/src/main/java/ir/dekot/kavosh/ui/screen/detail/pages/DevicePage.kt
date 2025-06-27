package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.detail.infoCards.DisplayInfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.StorageInfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.StorageSpeedTestCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.StorageViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DevicePage(deviceInfoViewModel: DeviceInfoViewModel, storageViewModel: StorageViewModel) {
    val deviceInfo by deviceInfoViewModel.deviceInfo.collectAsState()
    val isTesting by storageViewModel.isStorageTesting.collectAsState()
    val progress by storageViewModel.storageTestProgress.collectAsState()
    val writeSpeed by storageViewModel.writeSpeed.collectAsState()
    val readSpeed by storageViewModel.readSpeed.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DisplayInfoCard(deviceInfo.display)
        StorageInfoCard(deviceInfo.storage)
        StorageSpeedTestCard(
            isTesting = isTesting,
            progress = progress,
            writeSpeed = writeSpeed,
            readSpeed = readSpeed,
            onStartTest = { storageViewModel.startStorageSpeedTest() }
        )
    }
}