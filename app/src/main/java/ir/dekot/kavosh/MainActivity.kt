package ir.dekot.kavosh

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ir.dekot.kavosh.ui.screen.DeviceInspectorApp
import ir.dekot.kavosh.ui.theme.KavoshTheme
import ir.dekot.kavosh.ui.viewmodel.BatteryViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.SocViewModel

/**
 * MainActivity حالا یک نقطه ورود Hilt است.
 * @AndroidEntryPoint به Hilt اجازه می‌دهد تا وابستگی‌ها را به این Activity تزریق کند.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // دریافت ViewModelها به صورت خودکار توسط Hilt با استفاده از by viewModels()
    // دیگر نیازی به Factory یا ViewModelProvider دستی نیست.
    private val deviceInfoViewModel: DeviceInfoViewModel by viewModels()
    private val batteryViewModel: BatteryViewModel by viewModels()
    private val socViewModel: SocViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            KavoshTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // پاس دادن ViewModelهای دریافت شده از Hilt به اپلیکیشن
                    DeviceInspectorApp(deviceInfoViewModel, batteryViewModel, socViewModel)
                }
            }
        }
    }
}


