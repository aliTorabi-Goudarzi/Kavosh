package ir.dekot.kavosh

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ir.dekot.kavosh.ui.screen.DeviceInspectorApp
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deviceInfoViewModel: DeviceInfoViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            val format = deviceInfoViewModel.pendingExportFormat
            if (format != null) {
                deviceInfoViewModel.performExport(it, format)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // راه‌اندازی جمع‌آوری درخواست خروجی
        lifecycleScope.launch {
            deviceInfoViewModel.exportRequest.collectLatest { format ->
                val fileName = "Kavosh_Report_${System.currentTimeMillis()}.${format.extension}"
                createFileLauncher.launch(fileName)
            }
        }

        // --- تغییر کلیدی برای رفع باگ ---
        // بارگذاری داده‌ها فقط زمانی شروع می‌شود که اکتیویتی آماده باشد
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deviceInfoViewModel.loadDataForNonFirstLaunch(this@MainActivity)
            }
        }

        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DeviceInspectorApp(
                    deviceInfoViewModel = deviceInfoViewModel,
                    onStartScan = { deviceInfoViewModel.startScan(this) }
                )
            }
        }
    }
}