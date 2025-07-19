package ir.dekot.kavosh.core.ui.screen.splash

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceCacheViewModel
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceScanViewModel

// --- صفحه اسپلش (Splash) ---
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun SplashScreen(onStartScan: () -> Unit,
                 deviceScanViewModel: DeviceScanViewModel) {
    val isScanning by deviceScanViewModel.isScanning.collectAsState()
    val progress by deviceScanViewModel.scanProgress.collectAsState()
    val scanText by deviceScanViewModel.scanStatusText.collectAsState() // دریافت متن اسکن از ViewModel
    val animatedProgress =
        animateFloatAsState(targetValue = progress, label = "progress_anim").value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // تصویر پیش‌فرض (این تصویر را در res/drawable خود قرار دهید)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // یک تصویر دلخواه قرار دهید
            contentDescription = "Device Scan Logo",
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isScanning) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp),
                    color = Color.Green,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                // نمایش متن اسکن که به صورت پویا تغییر می‌کند
                Text(scanText, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartScan,
            enabled = !isScanning, // دکمه در حین اسکن غیرفعال می‌شود
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("اسکن دستگاه", fontSize = 16.sp)
        }
    }
}