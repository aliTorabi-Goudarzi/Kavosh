package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import ir.dekot.kavosh.ui.navigation.Screen
import ir.dekot.kavosh.ui.screen.about.AboutScreen
import ir.dekot.kavosh.ui.screen.dashboard.DashboardScreen
import ir.dekot.kavosh.ui.screen.dashboard.EditDashboardScreen
import ir.dekot.kavosh.ui.screen.detail.DetailScreen
import ir.dekot.kavosh.ui.screen.sensordetail.SensorDetailScreen
import ir.dekot.kavosh.ui.screen.settings.SettingsScreen
import ir.dekot.kavosh.ui.screen.splash.SplashScreen
import ir.dekot.kavosh.ui.viewmodel.DashboardViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    // دریافت هر سه ViewModel
    deviceInfoViewModel: DeviceInfoViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel = hiltViewModel(), // دریافت از Hilt
    onStartScan: () -> Unit
) {
    val currentScreen by deviceInfoViewModel.currentScreen.collectAsState()

    when (val screen = currentScreen) {
        is Screen.Splash -> SplashScreen(
            onStartScan = onStartScan,
            viewModel = deviceInfoViewModel
        )

        is Screen.Dashboard -> DashboardScreen(
            deviceInfoViewModel = deviceInfoViewModel,
            settingsViewModel = settingsViewModel,
            dashboardViewModel = dashboardViewModel, // <-- پاس دادن ViewModel جدید
            onCategoryClick = { category, _ ->
                deviceInfoViewModel.navigateToDetail(category)
            },
            onSettingsClick = { deviceInfoViewModel.navigateToSettings() },
            onEditDashboardClick = { deviceInfoViewModel.navigateToEditDashboard() }
        )

        is Screen.EditDashboard -> {
            BackHandler { deviceInfoViewModel.navigateBack() }
            EditDashboardScreen(
                viewModel = dashboardViewModel, // <-- استفاده از ViewModel جدید
                onBackClick = { deviceInfoViewModel.navigateBack() }
            )
        }

        // ... بقیه case ها بدون تغییر
        is Screen.Settings -> {
            BackHandler { deviceInfoViewModel.navigateBack() }
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToAbout = { deviceInfoViewModel.navigateToAbout() },
                onBackClick = { deviceInfoViewModel.navigateBack() }
            )
        }

        is Screen.Detail -> {
            BackHandler { deviceInfoViewModel.navigateBack() }
            DetailScreen(
                category = screen.category,
                viewModel = deviceInfoViewModel,
                onBackClick = { deviceInfoViewModel.navigateBack() }
            )
        }

        is Screen.About -> {
            BackHandler { deviceInfoViewModel.navigateBack() }
            AboutScreen(
                viewModel = settingsViewModel,
                onBackClick = { deviceInfoViewModel.navigateBack() }
            )
        }

        is Screen.SensorDetail -> {
            BackHandler { deviceInfoViewModel.navigateBack() }
            SensorDetailScreen(
                viewModel = deviceInfoViewModel,
                sensorType = screen.sensorType,
                onBackClick = { deviceInfoViewModel.navigateBack() }
            )
        }
    }
}