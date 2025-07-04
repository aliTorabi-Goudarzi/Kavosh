package ir.dekot.kavosh.ui.screen

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ir.dekot.kavosh.ui.navigation.Screen
import ir.dekot.kavosh.ui.screen.about.AboutScreen
import ir.dekot.kavosh.ui.screen.dashboard.EditDashboardScreen
import ir.dekot.kavosh.ui.screen.detail.DetailScreen
import ir.dekot.kavosh.ui.screen.displaytest.DisplayTestScreen
import ir.dekot.kavosh.ui.screen.main.MainScreen
import ir.dekot.kavosh.ui.screen.networktools.NetworkToolsScreen
import ir.dekot.kavosh.ui.screen.sensordetail.SensorDetailScreen
import ir.dekot.kavosh.ui.screen.settings.SettingsScreen
import ir.dekot.kavosh.ui.screen.splash.SplashScreen
import ir.dekot.kavosh.ui.screen.storagetest.StorageTestScreen
import ir.dekot.kavosh.ui.screen.stresstest.CpuStressTestScreen
import ir.dekot.kavosh.ui.screen.diagnostic.HealthCheckScreen
import ir.dekot.kavosh.ui.screen.diagnostic.PerformanceScoreScreen
import ir.dekot.kavosh.ui.screen.diagnostic.ComparisonScreen
import ir.dekot.kavosh.ui.viewmodel.DashboardViewModel
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportViewModel
import ir.dekot.kavosh.ui.viewmodel.DiagnosticExportViewModel
import ir.dekot.kavosh.ui.viewmodel.NavigationViewModel
import ir.dekot.kavosh.ui.viewmodel.SettingsViewModel

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    deviceInfoViewModel: DeviceInfoViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    exportViewModel: ExportViewModel,
    diagnosticExportViewModel: DiagnosticExportViewModel,
    navigationViewModel: NavigationViewModel,
    onStartScan: () -> Unit
) {
    val currentScreen by navigationViewModel.currentScreen.collectAsState()

    // ... (کد LaunchedEffect)

    // **اصلاح ۱: حذف BackHandler از اینجا و انتقال به داخل case ها**

    when (val screen = currentScreen) {
        is Screen.Splash -> SplashScreen(onStartScan = onStartScan, viewModel = deviceInfoViewModel)

        is Screen.Dashboard -> MainScreen(
            deviceInfoViewModel = deviceInfoViewModel,
            settingsViewModel = settingsViewModel,
            dashboardViewModel = dashboardViewModel,
            exportViewModel = exportViewModel,
            navigationViewModel = navigationViewModel,
            onCategoryClick = { category, _ -> navigationViewModel.navigateToDetail(category) },
            onNavigateToAbout = { navigationViewModel.navigateToAbout() },
            onEditDashboardClick = { navigationViewModel.navigateToEditDashboard() },
            onCpuStressTestClick = { navigationViewModel.navigateToCpuStressTest() },
            onStorageTestClick = { navigationViewModel.navigateToStorageTest() },
            onDisplayTestClick = { navigationViewModel.navigateToDisplayTest() },
            onNetworkToolsClick = { navigationViewModel.navigateToNetworkTools() },
            onHealthCheckClick = { navigationViewModel.navigateToHealthCheck() },
            onPerformanceScoreClick = { navigationViewModel.navigateToPerformanceScore() },
            onDeviceComparisonClick = { navigationViewModel.navigateToDeviceComparison() }
        )

        is Screen.Settings -> {
            BackHandler { navigationViewModel.navigateBack() }
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToAbout = { navigationViewModel.navigateToAbout() },
                onEditDashboardClick = { navigationViewModel.navigateToEditDashboard() },
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

        is Screen.StorageTest -> {
            BackHandler { navigationViewModel.navigateBack() }
            StorageTestScreen(
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        // صفحات ابزارهای تشخیصی جدید
        is Screen.HealthCheck -> {
            BackHandler { navigationViewModel.navigateBack() }
            HealthCheckScreen(
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        is Screen.PerformanceScore -> {
            BackHandler { navigationViewModel.navigateBack() }
            PerformanceScoreScreen(
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }

        is Screen.DeviceComparison -> {
            BackHandler { navigationViewModel.navigateBack() }
            ComparisonScreen(
                onBackClick = { navigationViewModel.navigateBack() }
            )
        }
    }
}