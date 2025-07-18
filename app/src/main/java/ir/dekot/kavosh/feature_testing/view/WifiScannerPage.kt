package ir.dekot.kavosh.feature_testing.view

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ir.dekot.kavosh.R
import ir.dekot.kavosh.core.util.isLocationEnabled
import ir.dekot.kavosh.feature_deviceInfo.model.WifiScanResult
import ir.dekot.kavosh.feature_testing.viewModel.NetworkToolsViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifiScannerPage(viewModel: NetworkToolsViewModel) {
    val scanResults by viewModel.wifiScanResults.collectAsState()
    val isScanning by viewModel.isScanningWifi.collectAsState()
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    // **اصلاح ۱: بررسی وضعیت Location دستگاه**
    var locationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            // **اصلاح ۲: مدیریت حالت‌های مختلف UI**
            !locationPermissionState.status.isGranted -> {
                PermissionRequiredView {
                    locationPermissionState.launchPermissionRequest()
                }
            }
            !locationEnabled -> {
                LocationDisabledView {
                    // باز کردن تنظیمات Location دستگاه
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    // ما نمی‌توانیم مستقیماً بفهمیم کاربر Location را روشن کرده یا نه،
                    // پس فقط صفحه تنظیمات را باز می‌کنیم. کاربر پس از بازگشت باید دوباره اسکن کند.
                }
            }
            isScanning -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            scanResults.isEmpty() -> {
                EmptyResultsView()
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(scanResults, key = { it.bssid }) { result ->
                        WifiNetworkItem(result = result)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                // به‌روزرسانی وضعیت Location قبل از هر اسکن
                locationEnabled = isLocationEnabled(context)
                if (locationPermissionState.status.isGranted && locationEnabled) {
                    viewModel.startWifiScan()
                } else if (!locationPermissionState.status.isGranted) {
                    locationPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_scan))
            }
        }
    }
}

// **کامپوزبل‌های جدید برای نمایش وضعیت‌های مختلف**

@Composable
private fun PermissionRequiredView(onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.wifi_permission_required_desc),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onGrantPermission) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

@Composable
private fun LocationDisabledView(onEnableLocation: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = stringResource(R.string.location_off),
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.location_service_disabled),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.turn_on_location_for_wifi),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onEnableLocation) {
            Text(stringResource(R.string.open_location_settings))
        }
    }
}

@Composable
private fun EmptyResultsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.no_wifi_networks_found),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.press_refresh_to_scan),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

// کامپوزبل WifiNetworkItem بدون تغییر باقی می‌ماند
@Composable
private fun WifiNetworkItem(result: WifiScanResult) {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (result.level > -70) Icons.Default.SignalWifi4Bar else Icons.Default.WifiOff,
                contentDescription = stringResource(R.string.signal_strength),
                tint = if (result.level > -70) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = result.ssid, fontWeight = FontWeight.Bold)
                Text(text = result.bssid, style = MaterialTheme.typography.bodySmall)
                Text(text = result.capabilities, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = stringResource(R.string.wifi_dbm_format, result.level), fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.wifi_mhz_format, result.frequency), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}