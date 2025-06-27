package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.detail.infoCards.SensorInfoCard
import ir.dekot.kavosh.ui.composables.EmptyStateMessage
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.NavigationViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SensorsPage(deviceInfoViewModel: DeviceInfoViewModel, navigationViewModel: NavigationViewModel) {
    val deviceInfo by deviceInfoViewModel.deviceInfo.collectAsState()

    if (deviceInfo.sensors.isEmpty()) {
        EmptyStateMessage("No sensors found on this device.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            deviceInfo.sensors.forEach { sensor ->
                SensorInfoCard(
                    info = sensor,
                    onTestClick = { sensorType ->
                        navigationViewModel.navigateToSensorDetail(sensorType)
                    }
                )
            }
        }
    }
}