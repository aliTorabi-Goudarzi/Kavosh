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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ir.dekot.kavosh.core.navigation.DeviceInspectorApp
import ir.dekot.kavosh.core.navigation.NavigationViewModel
import ir.dekot.kavosh.core.splash.SplashScreenManager
import ir.dekot.kavosh.feature_customeTheme.Theme
import ir.dekot.kavosh.feature_customeTheme.theme.KavoshTheme
import ir.dekot.kavosh.feature_dashboard.viewModel.DashboardViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel
import ir.dekot.kavosh.feature_export_and_sharing.viewModel.DiagnosticExportViewModel
import ir.dekot.kavosh.feature_export_and_sharing.viewModel.ExportViewModel
import ir.dekot.kavosh.feature_settings.viewModel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val deviceInfoViewModel: DeviceInfoViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val exportViewModel: ExportViewModel by viewModels() // <-- اضافه کردن ViewModel جدید
    private val diagnosticExportViewModel: DiagnosticExportViewModel by viewModels() // <-- اضافه کردن ViewModel جدید
    private val navigationViewModel: NavigationViewModel by viewModels() // <-- اضافه کردن ViewModel جدید

    // SplashScreenManager as regular class instance
    private lateinit var splashScreenManager: SplashScreenManager


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
    private val createDiagnosticFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            diagnosticExportViewModel.performExport(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun attachBaseContext(newBase: Context) {
        // از متد استاتیک ViewModel جدید استفاده می‌کنیم
        val lang = SettingsViewModel.getSavedLanguage(newBase)
        @Suppress("DEPRECATION") val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        // نصب صفحه اسپلش قبل از super.onCreate()
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // ایجاد SplashScreenManager پس از super.onCreate()
        // Create SplashScreenManager after super.onCreate()
        splashScreenManager = SplashScreenManager.create(this, settingsRepository)

        // پیکربندی صفحه اسپلش با بارگذاری داده‌ها
        // Configure splash screen with data loading
        splashScreenManager.configureSplashScreen(
            splashScreen = splashScreen,
            activity = this,
            deviceInfoViewModel = deviceInfoViewModel,
            dashboardViewModel = dashboardViewModel,
            onDataLoadingComplete = {
                // داده‌ها آماده است، صفحه اسپلش می‌تواند بسته شود
                // Data is ready, splash screen can be dismissed
            }
        )

        // به رویدادهای ViewModel جدید گوش می‌دهیم
        lifecycleScope.launch {
            exportViewModel.exportRequest.collectLatest { format ->
                val fileName = "Kavosh_Report_${System.currentTimeMillis()}.${format.extension}"
                createFileLauncher.launch(fileName)
            }
        }

        // به رویدادهای DiagnosticExportViewModel گوش می‌دهیم
        lifecycleScope.launch {
            diagnosticExportViewModel.filePickerRequest.collectLatest { format ->
                val fileName = "Kavosh_Diagnostic_${System.currentTimeMillis()}.${format.extension}"
                createDiagnosticFileLauncher.launch(fileName)
            }
        }

        lifecycleScope.launch {
            // به رویداد تغییر زبان از ViewModel جدید گوش می‌دهیم
            settingsViewModel.languageChangeRequest.collectLatest {
                recreate()
            }
        }

        // حذف شده: بارگذاری داده‌ها حالا در صفحه Loading انجام می‌شود
        // lifecycleScope.launch {
        //     lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        //         deviceInfoViewModel.loadDataForNonFirstLaunch(this@MainActivity)
        //     }
        // }

        enableEdgeToEdge()
        setContent {
            // زبان و تم را از ViewModel جدید می‌خوانیم
            val language by settingsViewModel.language.collectAsState()
            val currentTheme by settingsViewModel.themeState.collectAsState() // <-- خواندن تم
            val dynamicColor by settingsViewModel.isDynamicThemeEnabled.collectAsState()
            val currentColorTheme by settingsViewModel.currentColorTheme.collectAsState() // <-- خواندن تم رنگی


            val useDarkTheme = when (currentTheme) {
                Theme.SYSTEM -> isSystemInDarkTheme()
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.AMOLED -> true // تم AMOLED هم تم تاریک محسوب می‌شود
            }

            val layoutDirection = if (language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                // تم را اینجا بر اساس داده‌های SettingsViewModel تنظیم می‌کنیم
                KavoshTheme(
                    darkTheme = useDarkTheme,
                    dynamicColor = dynamicColor,
                    theme = currentTheme,
                    colorTheme = currentColorTheme // <-- پاس دادن تم رنگی
                ) {
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
                            diagnosticExportViewModel = diagnosticExportViewModel, // <-- پاس دادن ViewModel جدید
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

    override fun onDestroy() {
        super.onDestroy()
        // پاک کردن منابع SplashScreenManager
        // Clean up SplashScreenManager resources
        if (::splashScreenManager.isInitialized) {
            splashScreenManager.cleanup()
        }
    }
}