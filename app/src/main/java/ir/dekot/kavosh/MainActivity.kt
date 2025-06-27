package ir.dekot.kavosh

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.ui.screen.DeviceInspectorApp
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.SettingsViewModel // <-- ایمپورت جدید
import ir.dekot.kavosh.ui.theme.KavoshTheme // <-- ایمپورت اصلاح شده
import ir.dekot.kavosh.ui.viewmodel.DashboardViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportViewModel
import ir.dekot.kavosh.ui.viewmodel.NavigationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deviceInfoViewModel: DeviceInfoViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val exportViewModel: ExportViewModel by viewModels() // <-- اضافه کردن ViewModel جدید
    private val navigationViewModel: NavigationViewModel by viewModels() // <-- اضافه کردن ViewModel جدید


    @RequiresApi(Build.VERSION_CODES.R)
    private val createFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            val format = exportViewModel.pendingExportFormat
            if (format != null) {
                // اطلاعات دستگاه را از ViewModel اصلی می‌خوانیم و به ViewModel خروجی پاس می‌دهیم
                val currentDeviceInfo = deviceInfoViewModel.deviceInfo.value
                exportViewModel.performExport(it, format, currentDeviceInfo)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun attachBaseContext(newBase: Context) {
        // از متد استاتیک ViewModel جدید استفاده می‌کنیم
        val lang = SettingsViewModel.getSavedLanguage(newBase)
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // به رویدادهای ViewModel جدید گوش می‌دهیم
        lifecycleScope.launch {
            exportViewModel.exportRequest.collectLatest { format ->
                val fileName = "Kavosh_Report_${System.currentTimeMillis()}.${format.extension}"
                createFileLauncher.launch(fileName)
            }
        }

        lifecycleScope.launch {
            // به رویداد تغییر زبان از ViewModel جدید گوش می‌دهیم
            settingsViewModel.languageChangeRequest.collectLatest {
                recreate()
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                deviceInfoViewModel.loadDataForNonFirstLaunch(this@MainActivity)
            }
        }

        enableEdgeToEdge()
        setContent {
            // زبان و تم را از ViewModel جدید می‌خوانیم
            val language by settingsViewModel.language.collectAsState()
            val currentTheme by settingsViewModel.themeState.collectAsState()
            val dynamicColor by settingsViewModel.isDynamicThemeEnabled.collectAsState()

            val useDarkTheme = when (currentTheme) {
                Theme.SYSTEM -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
            }

            val layoutDirection = if (language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                // تم را اینجا بر اساس داده‌های SettingsViewModel تنظیم می‌کنیم
                KavoshTheme(darkTheme = useDarkTheme, dynamicColor = dynamicColor) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DeviceInspectorApp(
                            // هر دو ViewModel را به تابع اصلی پاس می‌دهیم
                            deviceInfoViewModel = deviceInfoViewModel,
                            settingsViewModel = settingsViewModel,
                            dashboardViewModel = dashboardViewModel, // <-- پاس دادن ViewModel جدید
                            exportViewModel = exportViewModel, // <-- پاس دادن ViewModel جدید
                            navigationViewModel = navigationViewModel, // <-- پاس دادن ViewModel جدید
                            onStartScan = {
                                deviceInfoViewModel.startScan(this) {
                                    // بعد از اتمام اسکن، به ViewModel ناوبری اطلاع می‌دهیم
                                    navigationViewModel.onScanCompleted()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}