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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.DisplayInfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.StorageInfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.StorageSpeedTestCard
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel
import ir.dekot.kavosh.feature_testing.viewModel.StorageViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DevicePage(deviceInfoViewModel: DeviceInfoViewModel, storageViewModel: StorageViewModel, onNavigateToDisplayTest: () -> Unit // <-- تابع جدید
) {
    val deviceInfo by deviceInfoViewModel.deviceInfo.collectAsState()
    val isTesting by storageViewModel.isStorageTesting.collectAsState()
    val progress by storageViewModel.storageTestProgress.collectAsState()
    val writeSpeed by storageViewModel.writeSpeed.collectAsState()
    val readSpeed by storageViewModel.readSpeed.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DisplayInfoCard(deviceInfo.display)
        Button(
            onClick = onNavigateToDisplayTest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.display_test_title))
        }
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