package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import ir.dekot.kavosh.ui.viewmodel.ExportViewModel
import ir.dekot.kavosh.ui.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    deviceInfoViewModel: DeviceInfoViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel, // این پارامتر از MainActivity میاد
    exportViewModel: ExportViewModel,       // این پارامتر از MainActivity میاد
    onStartScan: () -> Unit
) {
    val currentScreen by deviceInfoViewModel.currentScreen.collectAsState()

    when (val screen = currentScreen) {
        is Screen.Splash -> SplashScreen(
            onStartScan = onStartScan,
            viewModel = deviceInfoViewModel
        )

        is Screen.Dashboard -> DashboardScreen(
            // **اینجا نقطه کلیدی اصلاح است**
            // ما دیگر deviceInfoViewModel را به داشبورد پاس نمی‌دهیم
            settingsViewModel = settingsViewModel,
            dashboardViewModel = dashboardViewModel,
            exportViewModel = exportViewModel,
            // توابع ناوبری هنوز از viewModel اصلی استفاده می‌کنند
            onCategoryClick = { category, _ ->
                deviceInfoViewModel.navigateToDetail(category)
            },
            onSettingsClick = { deviceInfoViewModel.navigateToSettings() },
            onEditDashboardClick = { deviceInfoViewModel.navigateToEditDashboard() }
        )

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

        is Screen.EditDashboard -> {
            BackHandler { deviceInfoViewModel.navigateBack() }
            EditDashboardScreen(
                viewModel = dashboardViewModel,
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