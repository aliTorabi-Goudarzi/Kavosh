package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.ui.screen.dashboard.DashboardScreen
import ir.dekot.kavosh.ui.screen.splash.SplashScreen
import ir.dekot.kavosh.ui.viewmodel.BatteryViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.Screen
import ir.dekot.kavosh.ui.viewmodel.SocViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    deviceInfoViewModel: DeviceInfoViewModel,
    batteryViewModel: BatteryViewModel,
    socViewModel: SocViewModel,
    onStartScan: () -> Unit
) {
    val currentScreen by deviceInfoViewModel.currentScreen.collectAsState()
    val currentTheme by deviceInfoViewModel.themeState.collectAsState()

    val useDarkTheme = when (currentTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }

    // KavoshTheme هنوز برای اعمال تم لازم است، اما Crossfade داخلی آن حذف می‌شود
    ir.dekot.kavosh.ui.theme.KavoshTheme(darkTheme = useDarkTheme) {
        // Crossfade به طور کامل حذف شد و با یک when ساده جایگزین شد
        when (val screen = currentScreen) { // از screen در when استفاده می‌کنیم
            is Screen.Splash -> SplashScreen(
                onStartScan = onStartScan,
                viewModel = deviceInfoViewModel
            )

            is Screen.Dashboard -> DashboardScreen(
                onCategoryClick = { category, context ->
                    deviceInfoViewModel.navigateToDetail(category)
                },
                onSettingsClick = { deviceInfoViewModel.navigateToSettings() }
            )

            is Screen.Settings -> {
                BackHandler { deviceInfoViewModel.navigateBack() }
                SettingsScreen(
                    currentTheme = currentTheme,
                    onThemeSelected = { theme -> deviceInfoViewModel.onThemeSelected(theme) },
                    onBackClick = { deviceInfoViewModel.navigateBack() }
                )
            }

            is Screen.Detail -> {
                BackHandler { deviceInfoViewModel.navigateBack() }
                DetailScreen(
                    category = screen.category,
                    deviceInfoViewModel = deviceInfoViewModel,
                    batteryViewModel = batteryViewModel,
                    socViewModel = socViewModel,
                    onBackClick = { deviceInfoViewModel.navigateBack() }
                )
            }
        }
    }
}