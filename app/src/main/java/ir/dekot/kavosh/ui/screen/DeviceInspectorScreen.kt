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
import ir.dekot.kavosh.ui.screen.dashboard.EditDashboardScreen
import ir.dekot.kavosh.ui.screen.detail.DetailScreen
import ir.dekot.kavosh.ui.screen.settings.SettingsScreen
import ir.dekot.kavosh.ui.screen.splash.SplashScreen
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.Screen

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    deviceInfoViewModel: DeviceInfoViewModel,
    onStartScan: () -> Unit
) {
    val currentScreen by deviceInfoViewModel.currentScreen.collectAsState()
    val currentTheme by deviceInfoViewModel.themeState.collectAsState()
    val dynamicColor by deviceInfoViewModel.isDynamicThemeEnabled.collectAsState()


    val useDarkTheme = when (currentTheme) {
        Theme.SYSTEM -> isSystemInDarkTheme()
        Theme.LIGHT -> false
        Theme.DARK -> true
    }

    ir.dekot.kavosh.ui.theme.KavoshTheme(darkTheme = useDarkTheme,dynamicColor = dynamicColor) {
        when (val screen = currentScreen) {
            is Screen.Splash -> SplashScreen(
                onStartScan = onStartScan,
                viewModel = deviceInfoViewModel
            )

            is Screen.Dashboard -> DashboardScreen(
                deviceInfoViewModel = deviceInfoViewModel,
                onCategoryClick = { category, _ ->
                    deviceInfoViewModel.navigateToDetail(category)
                },
                onSettingsClick = { deviceInfoViewModel.navigateToSettings() },
                onEditDashboardClick = { deviceInfoViewModel.navigateToEditDashboard() }
            )

            is Screen.Settings -> {
                BackHandler { deviceInfoViewModel.navigateBack() }
                SettingsScreen(
                    viewModel = deviceInfoViewModel,
                    onBackClick = { deviceInfoViewModel.navigateBack() }
                )
            }

            is Screen.Detail -> {
                BackHandler { deviceInfoViewModel.navigateBack() }
                // فراخوانی DetailScreen با پارامترهای جدید و ساده‌تر
                DetailScreen(
                    category = screen.category,
                    viewModel = deviceInfoViewModel, // فقط یک ViewModel پاس داده می‌شود
                    onBackClick = { deviceInfoViewModel.navigateBack() }
                )
            }

            is Screen.EditDashboard -> {
                BackHandler { deviceInfoViewModel.navigateBack() }
                EditDashboardScreen(
                    viewModel = deviceInfoViewModel,
                    onBackClick = { deviceInfoViewModel.navigateBack() }
                )
            }
        }
    }
}