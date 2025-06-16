package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DeviceInfoViewModel, // دریافت کل ViewModel برای دسترسی آسان
    onBackClick: () -> Unit
) {
    val currentTheme by viewModel.themeState.collectAsState()
    val isReorderingEnabled by viewModel.isReorderingEnabled.collectAsState()
    val isDynamicThemeEnabled by viewModel.isDynamicThemeEnabled.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            // بخش تنظیمات تم
            Text("انتخاب تم برنامه", style = MaterialTheme.typography.titleLarge)
            Theme.entries.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onThemeSelected(theme) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (currentTheme == theme),
                        onClick = { viewModel.onThemeSelected(theme) }
                    )
                    Text(
                        text = when (theme) {
                            Theme.SYSTEM -> "پیش‌فرض سیستم"
                            Theme.LIGHT -> "روشن"
                            Theme.DARK -> "تاریک"
                        },
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // --- سوییچ جدید برای تم پویا (فقط در اندروید ۱۲+) ---
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onDynamicThemeToggled(!isDynamicThemeEnabled) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تم پویا (بر اساس تصویر زمینه)",
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                    )
                    Switch(
                        checked = isDynamicThemeEnabled,
                        onCheckedChange = { viewModel.onDynamicThemeToggled(it) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- بخش جدید برای تنظیمات داشبورد ---
            Text("تنظیمات داشبورد", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onReorderingToggled(!isReorderingEnabled) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فعال‌سازی جابجایی آیتم‌ها",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )
                Switch(
                    checked = isReorderingEnabled,
                    onCheckedChange = { viewModel.onReorderingToggled(it) }
                )
            }
        }
    }
}