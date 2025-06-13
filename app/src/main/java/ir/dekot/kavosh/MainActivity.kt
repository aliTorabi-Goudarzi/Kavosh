package ir.dekot.kavosh

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.ui.screen.DeviceInspectorApp
import ir.dekot.kavosh.ui.theme.KavoshTheme
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ساختن وابستگی‌ها (در پروژه‌های بزرگ‌تر از Hilt/Dagger استفاده می‌شود)
        val repository = DeviceInfoRepository(this)
        val viewModelFactory = DeviceInfoViewModelFactory(repository)
        val viewModel: DeviceInfoViewModel =
            ViewModelProvider(this, viewModelFactory)[DeviceInfoViewModel::class.java]

        // توجه: دیگر نیازی به فراخوانی startBatteryUpdates در اینجا نیست.
        // این کار در صفحه جزئیات باتری انجام خواهد شد.

        enableEdgeToEdge()
        setContent {
            KavoshTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DeviceInspectorApp(viewModel)
                }
            }
        }
    }
}


