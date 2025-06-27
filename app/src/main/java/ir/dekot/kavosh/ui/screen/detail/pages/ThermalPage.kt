package ir.dekot.kavosh.ui.screen.detail.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.detail.infoCards.ThermalInfoCard
import ir.dekot.kavosh.ui.screen.shared.EmptyStateMessage
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ThermalPage(viewModel: DeviceInfoViewModel) {
    val thermalDetails by viewModel.thermalDetails.collectAsState()

    if (thermalDetails.isEmpty()) {
        EmptyStateMessage("Thermal information is not available for this device.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            thermalDetails.forEach { thermalInfo ->
                ThermalInfoCard(info = thermalInfo)
            }
        }
    }
}