package ir.dekot.kavosh.ui.screen.detail.pages

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.composables.EmptyStateMessage
import ir.dekot.kavosh.ui.screen.detail.infoCards.SimInfoCard
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import kotlinx.coroutines.flow.filter

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SimPage(viewModel: DeviceInfoViewModel) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val permissionState = rememberPermissionState(Manifest.permission.READ_PHONE_STATE)

    // **اصلاح کلیدی: استفاده از LaunchedEffect برای واکنش به تغییر مجوز**
    LaunchedEffect(permissionState) {
        // یک جریان (Flow) از وضعیت مجوز ایجاد می‌کنیم
        snapshotFlow { permissionState.status }
            // فیلتر می‌کنیم تا فقط زمانی که مجوز "اعطا شده" است، ادامه دهیم
            .filter { it.isGranted }
            .collect {
                // اطلاعات سیم‌کارت را دوباره واکشی کن
                viewModel.fetchSimInfo()
            }
    }

    if (permissionState.status.isGranted) {
        if (deviceInfo.simCards.isEmpty()) {
            EmptyStateMessage("No active SIM cards found.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                deviceInfo.simCards.forEach { simInfo ->
                    SimInfoCard(info = simInfo)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.permission_required_sim), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}