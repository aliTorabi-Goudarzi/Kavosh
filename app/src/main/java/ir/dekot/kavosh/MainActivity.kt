package ir.dekot.kavosh

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.ui.screen.DeviceInspectorApp
import ir.dekot.kavosh.ui.theme.KavoshTheme
import ir.dekot.kavosh.ui.viewmodel.BatteryViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModelFactory
import ir.dekot.kavosh.ui.viewmodel.SocViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = DeviceInfoRepository(this)
        val viewModelFactory = DeviceInfoViewModelFactory(repository)

        // ساخت هر سه ViewModel
        val deviceInfoViewModel: DeviceInfoViewModel =
            ViewModelProvider(this, viewModelFactory)[DeviceInfoViewModel::class.java]
        val batteryViewModel: BatteryViewModel =
            ViewModelProvider(this, viewModelFactory)[BatteryViewModel::class.java]
        val socViewModel: SocViewModel =
            ViewModelProvider(this, viewModelFactory)[SocViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            KavoshTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // پاس دادن هر سه ViewModel به اپلیکیشن
                    DeviceInspectorApp(deviceInfoViewModel, batteryViewModel, socViewModel)
                }
            }
        }
    }
}


