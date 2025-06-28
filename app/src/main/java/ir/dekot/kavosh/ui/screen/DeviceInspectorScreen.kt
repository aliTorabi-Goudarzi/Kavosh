package ir.dekot.kavosh.ui.screen

import android.annotation.SuppressLint
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
import ir.dekot.kavosh.ui.screen.displaytest.DisplayTestScreen
import ir.dekot.kavosh.ui.screen.networktools.NetworkToolsScreen
import ir.dekot.kavosh.ui.screen.sensordetail.SensorDetailScreen
import ir.dekot.kavosh.ui.screen.settings.SettingsScreen
import ir.dekot.kavosh.ui.screen.splash.SplashScreen
import ir.dekot.kavosh.ui.screen.stresstest.CpuStressTestScreen
import ir.dekot.kavosh.ui.viewmodel.*

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    deviceInfoViewModel: DeviceInfoViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    exportViewModel: ExportViewModel,
    navigationViewModel: NavigationViewModel,
    onStartScan: () -> Unit
) {
    val currentScreen by navigationViewModel.currentScreen.collectAsState()

    // ... (کد LaunchedEffect)

    // **اصلاح ۱: حذف BackHandler از اینجا و انتقال به داخل case ها**

    when (val screen = currentScreen) {
        is Screen.Splash -> SplashScreen(onStartScan = onStartScan, viewModel = deviceInfoViewModel)

        is Screen.Dashboard -> DashboardScreen(
            settingsViewModel = settingsViewModel,
            dashboardViewModel = dashboardViewModel,
            exportViewModel = exportViewModel,
            onCategoryClick = { category, _ -> navigationViewModel.navigateToDetail(category) },
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
                navigationViewModel = navigationViewModel,
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        is Screen.EditDashboard -> {
            BackHandler {
                dashboardViewModel.loadDashboardItems()
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

        is Screen.CpuStressTest -> {
            BackHandler { navigationViewModel.navigateBack() }
            CpuStressTestScreen(onBackClick = { navigationViewModel.navigateBack() })
        }

        is Screen.SensorDetail -> {
            BackHandler { navigationViewModel.navigateBack() }
            SensorDetailScreen(
                viewModel = deviceInfoViewModel,
                sensorType = screen.sensorType,
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        // **اصلاح ۲: جایگزینی TODO() با پیاده‌سازی صحیح**
        is Screen.NetworkTools -> {
            BackHandler { navigationViewModel.navigateBack() }
            NetworkToolsScreen(onBackClick = { navigationViewModel.navigateBack() })
        }
        is Screen.DisplayTest -> {
            BackHandler { navigationViewModel.navigateBack() }
            DisplayTestScreen(onBackClick = { navigationViewModel.navigateBack() })
        }
    }
}