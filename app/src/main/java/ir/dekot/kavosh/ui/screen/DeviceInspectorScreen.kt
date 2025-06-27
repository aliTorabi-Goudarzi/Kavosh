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
    navigationViewModel: NavigationViewModel,
    onStartScan: () -> Unit
) {
    val currentScreen by navigationViewModel.currentScreen.collectAsState()

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

    // **اصلاح کلیدی در منطق BackHandler ها**
    when (val screen = currentScreen) {
        is Screen.Splash -> { /* دکمه بازگشت در اسپلش غیرفعال است */ }

        is Screen.Dashboard -> { /* در داشبورد، بازگشت به معنی خروج از برنامه است */ }

        is Screen.Detail, is Screen.Settings, is Screen.About, is Screen.SensorDetail -> {
            // برای تمام این صفحات، رفتار بازگشت یکسان است
            BackHandler { navigationViewModel.navigateBack() }
        }

        is Screen.EditDashboard -> {
            // رفتار خاص برای صفحه ویرایش داشبورد
            BackHandler {
                dashboardViewModel.loadDashboardItems() // آیتم‌ها را قبل از بازگشت رفرش کن
                navigationViewModel.navigateBack()
            }
        }
    }

    // بدنه اصلی نمایش صفحات (بدون تغییر زیاد)
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

        is Screen.Settings -> SettingsScreen(
            viewModel = settingsViewModel,
            onNavigateToAbout = { navigationViewModel.navigateToAbout() },
            onBackClick = { navigationViewModel.navigateBack() }
        )

        is Screen.Detail -> DetailScreen(
            category = screen.category,
            viewModel = deviceInfoViewModel,
            navigationViewModel = navigationViewModel,
            onBackClick = { navigationViewModel.navigateBack() }
        )

        is Screen.EditDashboard -> EditDashboardScreen(
            viewModel = dashboardViewModel,
            onBackClick = {
                dashboardViewModel.loadDashboardItems()
                navigationViewModel.navigateBack()
            }
        )

        is Screen.About -> AboutScreen(
            viewModel = settingsViewModel,
            onBackClick = { navigationViewModel.navigateBack() }
        )

        is Screen.SensorDetail -> SensorDetailScreen(
            viewModel = deviceInfoViewModel,
            sensorType = screen.sensorType,
            onBackClick = { navigationViewModel.navigateBack() }
        )
    }
}