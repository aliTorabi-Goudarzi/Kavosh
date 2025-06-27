package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ir.dekot.kavosh.ui.viewmodel.*

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    deviceInfoViewModel: DeviceInfoViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    exportViewModel: ExportViewModel,
    navigationViewModel: NavigationViewModel, // <-- پارامتر جدید
    onStartScan: () -> Unit
) {
    // **اصلاح کلیدی: خواندن وضعیت ناوبری از ViewModel جدید**
    val currentScreen by navigationViewModel.currentScreen.collectAsState()

    // این افکت، مدیریت polling را بر اساس صفحه فعلی انجام می‌دهد
    LaunchedEffect(currentScreen) {
        when (val screen = currentScreen) {
            is Screen.Detail -> deviceInfoViewModel.startPollingForCategory(screen.category)
            is Screen.SensorDetail -> deviceInfoViewModel.registerSensorListener(screen.sensorType)
            else -> {
                deviceInfoViewModel.stopAllPolling()
                deviceInfoViewModel.unregisterSensorListener()
            }
        }
    }

    when (val screen = currentScreen) {
        is Screen.Splash -> SplashScreen(
            onStartScan = onStartScan,
            viewModel = deviceInfoViewModel
        )

        is Screen.Dashboard -> DashboardScreen(
            settingsViewModel = settingsViewModel,
            dashboardViewModel = dashboardViewModel,
            exportViewModel = exportViewModel,
            onCategoryClick = { category, _ ->
                navigationViewModel.navigateToDetail(category)
            },
            onSettingsClick = { navigationViewModel.navigateToSettings() },
            onEditDashboardClick = { navigationViewModel.navigateToEditDashboard() }
        )

        is Screen.Settings -> {
            BackHandler { navigationViewModel.navigateBack() }
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToAbout = { navigationViewModel.navigateToAbout() },
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        is Screen.Detail -> {
            BackHandler { navigationViewModel.navigateBack() }
            DetailScreen(
                category = screen.category,
                viewModel = deviceInfoViewModel,
                navigationViewModel = navigationViewModel, // <-- پاس دادن ViewModel ناوبری
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        is Screen.EditDashboard -> {
            BackHandler {
                dashboardViewModel.loadDashboardItems() // بازخوانی آیتم‌ها قبل از بازگشت
                navigationViewModel.navigateBack()
            }
            EditDashboardScreen(
                viewModel = dashboardViewModel,
                onBackClick = {
                    dashboardViewModel.loadDashboardItems()
                    navigationViewModel.navigateBack()
                }
            )
        }

        is Screen.About -> {
            BackHandler { navigationViewModel.navigateBack() }
            AboutScreen(
                viewModel = settingsViewModel,
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        is Screen.SensorDetail -> {
            BackHandler { navigationViewModel.navigateBack() }
            SensorDetailScreen(
                viewModel = deviceInfoViewModel,
                sensorType = screen.sensorType,
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }
    }
}