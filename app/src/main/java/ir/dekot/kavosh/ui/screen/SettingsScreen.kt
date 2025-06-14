package ir.dekot.kavosh.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.data.model.settings.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onBackClick: () -> Unit
) {
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
            Text("انتخاب تم برنامه", style = MaterialTheme.typography.titleLarge)

            // گزینه‌های انتخاب تم
            Theme.entries.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeSelected(theme) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (currentTheme == theme),
                        onClick = { onThemeSelected(theme) }
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
        }
    }
}

