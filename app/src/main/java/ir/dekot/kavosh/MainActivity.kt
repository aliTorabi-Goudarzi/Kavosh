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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deviceInfoViewModel: DeviceInfoViewModel by viewModels()
    // ViewModel جدید را هم از طریق Hilt دریافت می‌کنیم
    private val settingsViewModel: SettingsViewModel by viewModels()

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

        lifecycleScope.launch {
            // به رویداد تغییر زبان از ViewModel جدید گوش می‌دهیم
            settingsViewModel.languageChangeRequest.collectLatest {
                recreate()
            }
        }

        lifecycleScope.launch {
            deviceInfoViewModel.exportRequest.collectLatest { format ->
                val fileName = "Kavosh_Report_${System.currentTimeMillis()}.${format.extension}"
                createFileLauncher.launch(fileName)
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
                            onStartScan = { deviceInfoViewModel.startScan(this) }
                        )
                    }
                }
            }
        }
    }
}