package ir.dekot.kavosh.ui.screen.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * صفحه تنظیمات با طراحی مدرن و حرفه‌ای
 * شامل بخش‌های مختلف تنظیمات با گروه‌بندی منطقی
 */
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToAbout: () -> Unit,
    onEditDashboardClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // جمع‌آوری state های مختلف از ViewModel
    val currentTheme by viewModel.themeState.collectAsState()
    val isReorderingEnabled by viewModel.isReorderingEnabled.collectAsState()
    val isDynamicThemeEnabled by viewModel.isDynamicThemeEnabled.collectAsState()
    val currentLanguage by viewModel.language.collectAsState()

    // state های محلی برای کنترل نمایش بخش‌های مختلف
    var expandedSection by remember { mutableStateOf<SettingsSection?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // هدر صفحه
            item {
                Text(
                    text = stringResource(id = R.string.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            // بخش ظاهر
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_appearance),
                    description = stringResource(R.string.settings_appearance_desc),
                    icon = Icons.Default.Palette,
                    isExpanded = expandedSection == SettingsSection.APPEARANCE,
                    onToggleExpanded = {
                        expandedSection = if (expandedSection == SettingsSection.APPEARANCE) null else SettingsSection.APPEARANCE
                    }
                ) {
                    AppearanceSettings(
                        currentTheme = currentTheme,
                        currentLanguage = currentLanguage,
                        isDynamicThemeEnabled = isDynamicThemeEnabled,
                        onThemeSelected = viewModel::onThemeSelected,
                        onLanguageSelected = viewModel::onLanguageSelected,
                        onDynamicThemeToggled = viewModel::onDynamicThemeToggled
                    )
                }
            }

            // بخش داشبورد
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_dashboard),
                    description = stringResource(R.string.settings_dashboard_desc),
                    icon = Icons.Default.Dashboard,
                    isExpanded = expandedSection == SettingsSection.DASHBOARD,
                    onToggleExpanded = {
                        expandedSection = if (expandedSection == SettingsSection.DASHBOARD) null else SettingsSection.DASHBOARD
                    }
                ) {
                    DashboardSettings(
                        isReorderingEnabled = isReorderingEnabled,
                        onReorderingToggled = viewModel::onReorderingToggled,
                        onEditDashboardClick = onEditDashboardClick
                    )
                }
            }

            // بخش عملکرد
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_performance),
                    description = stringResource(R.string.settings_performance_desc),
                    icon = Icons.Default.Speed,
                    isExpanded = expandedSection == SettingsSection.PERFORMANCE,
                    onToggleExpanded = {
                        expandedSection = if (expandedSection == SettingsSection.PERFORMANCE) null else SettingsSection.PERFORMANCE
                    }
                ) {
                    PerformanceSettings(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }

            // بخش درباره
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_about),
                    description = stringResource(R.string.settings_about_desc),
                    icon = Icons.Default.Info,
                    isExpanded = false,
                    onToggleExpanded = { onNavigateToAbout() }
                ) {
                    // این بخش محتوای قابل گسترش ندارد
                }
            }
        }

        // Snackbar برای نمایش پیام‌ها
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * enum برای مشخص کردن بخش‌های مختلف تنظیمات
 */
private enum class SettingsSection {
    APPEARANCE, DASHBOARD, PERFORMANCE
}

/**
 * کامپوننت اصلی برای نمایش هر بخش از تنظیمات
 */
@Composable
private fun SettingsSection(
    title: String,
    description: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // هدر بخش
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

//                Icon(
//                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
            }

            // محتوای قابل گسترش
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * کامپوننت تنظیمات ظاهر شامل تم، زبان و رنگ‌ها
 */
@Composable
private fun AppearanceSettings(
    currentTheme: Theme,
    currentLanguage: String,
    isDynamicThemeEnabled: Boolean,
    onThemeSelected: (Theme) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onDynamicThemeToggled: (Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // بخش انتخاب زبان
        SettingsGroup(
            title = stringResource(R.string.language_selection)
        ) {
            LanguageSelector(
                currentLanguage = currentLanguage,
                onLanguageSelected = onLanguageSelected
            )
        }

        // بخش انتخاب تم
        SettingsGroup(
            title = stringResource(R.string.theme_selection)
        ) {
            ThemeSelector(
                currentTheme = currentTheme,
                onThemeSelected = onThemeSelected
            )

            // تم پویا (فقط برای Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggleItem(
                    title = stringResource(R.string.dynamic_theme),
                    description = null,
                    checked = isDynamicThemeEnabled,
                    onCheckedChange = onDynamicThemeToggled
                )
            }
        }
    }
}

/**
 * کامپوننت انتخاب زبان
 */
@Composable
private fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "fa" to stringResource(R.string.persian),
        "en" to stringResource(R.string.english)
    )

    Column {
        languages.forEach { (code, name) ->
            SettingsRadioItem(
                title = name,
                selected = currentLanguage == code,
                onClick = { onLanguageSelected(code) }
            )
        }
    }
}

/**
 * کامپوننت انتخاب تم
 */
@Composable
private fun ThemeSelector(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit
) {
    Column {
        Theme.entries.forEach { theme ->
            SettingsRadioItem(
                title = when (theme) {
                    Theme.SYSTEM -> stringResource(R.string.system_default)
                    Theme.LIGHT -> stringResource(R.string.light)
                    Theme.DARK -> stringResource(R.string.dark)
                    Theme.AMOLED -> stringResource(R.string.amoled)
                },
                selected = currentTheme == theme,
                onClick = { onThemeSelected(theme) }
            )
        }
    }
}

/**
 * کامپوننت تنظیمات داشبورد
 */
@Composable
private fun DashboardSettings(
    isReorderingEnabled: Boolean,
    onReorderingToggled: (Boolean) -> Unit,
    onEditDashboardClick: () -> Unit
) {
    SettingsGroup(
        title = stringResource(R.string.dashboard_customization)
    ) {
        SettingsToggleItem(
            title = stringResource(R.string.enable_item_reordering),
            description = null,
            checked = isReorderingEnabled,
            onCheckedChange = onReorderingToggled
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsClickableItem(
            title = stringResource(R.string.edit_dashboard_items),
            description = "نمایش یا مخفی کردن آیتم‌های داشبورد",
            icon = Icons.Default.Edit,
            onClick = onEditDashboardClick
        )
    }
}

/**
 * کامپوننت تنظیمات عملکرد
 */
@Composable
private fun PerformanceSettings(
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()

    // state های محلی برای کنترل دیالوگ‌ها
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    // خواندن string resource ها در سطح Composable
    val cacheCleared = stringResource(R.string.cache_cleared)
    val settingsReset = stringResource(R.string.settings_reset)

    SettingsGroup(
        title = stringResource(R.string.advanced_settings)
    ) {
        // مدیریت کش
        SettingsClickableItem(
            title = stringResource(R.string.clear_cache),
            description = stringResource(R.string.clear_cache_desc),
            icon = Icons.Default.Delete,
            onClick = {
                showClearCacheDialog = true
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // بازنشانی تنظیمات
        SettingsClickableItem(
            title = stringResource(R.string.reset_settings),
            description = stringResource(R.string.reset_settings_desc),
            icon = Icons.Default.RestartAlt,
            onClick = {
                showResetSettingsDialog = true
            }
        )
    }

    // دیالوگ تأیید پاک کردن کش
    if (showClearCacheDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.clear_cache),
            message = stringResource(R.string.confirm_clear_cache),
            onConfirm = {
                viewModel.clearCache()
                showClearCacheDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = cacheCleared
                    )
                }
            },
            onDismiss = {
                showClearCacheDialog = false
            }
        )
    }

    // دیالوگ تأیید بازنشانی تنظیمات
    if (showResetSettingsDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.reset_settings),
            message = stringResource(R.string.confirm_reset_settings),
            onConfirm = {
                viewModel.resetAllSettings()
                showResetSettingsDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = settingsReset
                    )
                }
            },
            onDismiss = {
                showResetSettingsDialog = false
            }
        )
    }
}

/**
 * کامپوننت گروه‌بندی تنظیمات
 */
@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * آیتم رادیو برای انتخاب گزینه‌ها
 */
@Composable
private fun SettingsRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * آیتم سوئیچ برای فعال/غیرفعال کردن گزینه‌ها
 */
@Composable
private fun SettingsToggleItem(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White
            )
        )
    }
}

/**
 * آیتم قابل کلیک برای اعمال مختلف
 */
@Composable
private fun SettingsClickableItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * دیالوگ تأیید برای عملیات‌های حساس
 */
@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(R.string.action_confirm),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}