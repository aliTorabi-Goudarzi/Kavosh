package ir.dekot.kavosh.ui.screen.settings

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.ui.viewmodel.SettingsViewModel // <-- ایمپورت ViewModel جدید

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel, // <-- استفاده از ViewModel جدید
    onNavigateToAbout: () -> Unit, // <-- تابع جدید برای ناوبری
    onBackClick: () -> Unit
) {
    val currentTheme by viewModel.themeState.collectAsState()
    val isReorderingEnabled by viewModel.isReorderingEnabled.collectAsState()
    val isDynamicThemeEnabled by viewModel.isDynamicThemeEnabled.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {

            // بخش تنظیمات زبان
            Text(stringResource(R.string.language_settings), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.choose_language), style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onLanguageSelected("fa") }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (currentLanguage == "fa"),
                    onClick = { viewModel.onLanguageSelected("fa") }
                )
                Text(
                    text = stringResource(R.string.persian),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onLanguageSelected("en") }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (currentLanguage == "en"),
                    onClick = { viewModel.onLanguageSelected("en") }
                )
                Text(
                    text = stringResource(R.string.english),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // بخش تنظیمات تم
            Text(stringResource(R.string.choose_app_theme), style = MaterialTheme.typography.titleLarge)
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
                            Theme.SYSTEM -> stringResource(R.string.system_default)
                            Theme.LIGHT -> stringResource(R.string.light)
                            Theme.DARK -> stringResource(R.string.dark)
                        },
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onDynamicThemeToggled(!isDynamicThemeEnabled) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dynamic_theme),
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

            // بخش تنظیمات داشبورد
            Text(stringResource(R.string.dashboard_settings), style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onReorderingToggled(!isReorderingEnabled) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.enable_item_reordering),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )
                Switch(
                    checked = isReorderingEnabled,
                    onCheckedChange = { viewModel.onReorderingToggled(it) }
                )
            }

            // بخش "درباره"
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAbout() } // <-- استفاده از تابع جدید
                    .padding(vertical = 12.dp)
            )
        }
    }
}