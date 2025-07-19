package ir.dekot.kavosh.core.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ir.dekot.kavosh.core.ui.shared_components.AnimatedScreenTransition
import ir.dekot.kavosh.core.ui.screen.about.AboutScreen
import ir.dekot.kavosh.feature_dashboard.view.EditDashboardScreen

import ir.dekot.kavosh.feature_dashboard.view.MainScreen

import ir.dekot.kavosh.core.ui.screen.splash.SplashScreen
import ir.dekot.kavosh.feature_dashboard.viewModel.DashboardViewModel
import ir.dekot.kavosh.feature_deviceInfo.view.DetailScreen
import ir.dekot.kavosh.feature_deviceInfo.view.SensorDetailScreen
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceCacheViewModel

import ir.dekot.kavosh.feature_export_and_sharing.viewModel.ExportViewModel
import ir.dekot.kavosh.feature_export_and_sharing.viewModel.DiagnosticExportViewModel
import ir.dekot.kavosh.feature_settings.viewModel.SettingsViewModel
import ir.dekot.kavosh.ui.screen.diagnostic.ComparisonScreen
import ir.dekot.kavosh.ui.screen.diagnostic.HealthCheckScreen
import ir.dekot.kavosh.ui.screen.diagnostic.PerformanceScoreScreen
import ir.dekot.kavosh.feature_settings.view.SettingsScreen
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceScanViewModel
import ir.dekot.kavosh.feature_testing.view.DisplayTestScreen
import ir.dekot.kavosh.feature_testing.view.NetworkToolsScreen
import ir.dekot.kavosh.feature_testing.view.CpuStressTestScreen
import ir.dekot.kavosh.feature_testing.view.StorageTestScreen
import ir.dekot.kavosh.feature_testing.viewModel.StorageViewModel

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DeviceInspectorApp(
    deviceScanViewModel: DeviceScanViewModel,
    deviceInfoViewModel: DeviceInfoViewModel,
    deviceCacheViewModel: DeviceCacheViewModel,
    storageViewModel: StorageViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    exportViewModel: ExportViewModel,
    diagnosticExportViewModel: DiagnosticExportViewModel,
    navigationViewModel: NavigationViewModel,
    onStartScan: () -> Unit
) {
    val currentScreen by navigationViewModel.currentScreen.collectAsState()

    // ... (کد LaunchedEffect)

    // **اصلاح ۱: استفاده از انیمیشن انتقال صفحات**
    AnimatedScreenTransition(currentScreen = currentScreen) { screen ->
        when (screen) {
            is Screen.Splash -> SplashScreen(onStartScan = onStartScan, deviceScanViewModel = deviceScanViewModel)

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
                deviceInfoViewModel = deviceInfoViewModel,
                navigationViewModel = navigationViewModel,
                onBackClick = { navigationViewModel.navigateBack() },
                deviceCacheViewModel = deviceCacheViewModel,
                storageViewModel = storageViewModel
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
}