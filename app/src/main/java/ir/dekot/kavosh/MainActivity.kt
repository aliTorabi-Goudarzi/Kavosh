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
import ir.dekot.kavosh.ui.viewmodel.BatteryViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.SocViewModel

/**
 * MainActivity حالا یک نقطه ورود Hilt است.
 * @AndroidEntryPoint به Hilt اجازه می‌دهد تا وابستگی‌ها را به این Activity تزریق کند.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deviceInfoViewModel: DeviceInfoViewModel by viewModels()
    private val batteryViewModel: BatteryViewModel by viewModels()
    private val socViewModel: SocViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // در اینجا، پس از ساخت ViewModel، داده‌ها را برای اجراهای بعدی بارگذاری می‌کنیم
        // ViewModel در init خود دیگر این کار را انجام نمی‌دهد.
        deviceInfoViewModel.loadDataForNonFirstLaunch(this)

        enableEdgeToEdge()
        setContent {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // در اینجا یک لامبدا برای startScan می‌سازیم که Activity را پاس دهد
                    DeviceInspectorApp(
                        deviceInfoViewModel = deviceInfoViewModel,
                        batteryViewModel = batteryViewModel,
                        socViewModel = socViewModel,
                        // به این ترتیب، UI از وجود Activity بی‌خبر می‌ماند
                        onStartScan = { deviceInfoViewModel.startScan(this) }
                    )
                }
            }
        }
    }



