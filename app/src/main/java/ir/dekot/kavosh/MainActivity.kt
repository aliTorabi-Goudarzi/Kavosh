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
import ir.dekot.kavosh.ui.screen.DeviceInspectorApp
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

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
    override fun attachBaseContext(newBase: Context) {
        // این متد زبان را قبل از ساخت Activity تنظیم می‌کند
        val lang = DeviceInfoViewModel.getSavedLanguage(newBase)
        val locale = Locale(lang)
        Locale.setDefault(locale) // تنظیم زبان پیش‌فرض برای کل برنامه
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // مشاهده رویداد تغییر زبان از ViewModel
        lifecycleScope.launch {
            deviceInfoViewModel.languageChangeRequest.collectLatest {
                // Activity را از نو بساز تا زبان جدید اعمال شود
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
            // زبان فعلی را مستقیماً از ViewModel می‌خوانیم
            val language by deviceInfoViewModel.language.collectAsState()
            // تعیین چیدمان بر اساس زبان
            val layoutDirection = if (language == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
}