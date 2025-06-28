package ir.dekot.kavosh.ui.screen.detail.pages

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
import ir.dekot.kavosh.ui.screen.detail.infoCards.NetworkInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun NetworkPage(
    viewModel: DeviceInfoViewModel,
    onNavigateToTools: () -> Unit // <-- پارامتر برای ناوبری
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NetworkInfoCard(
            info = deviceInfo.network,
            downloadSpeed = downloadSpeed,
            uploadSpeed = uploadSpeed
        )
        Button(
            onClick = onNavigateToTools, // <-- استفاده از رویداد کلیک
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Advanced Network Tools")
        }
    }
}