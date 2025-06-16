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
    // دریافت وضعیت تم پویا از ViewModel
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
                onEditDashboardClick = { deviceInfoViewModel.navigateToEditDashboard() } // <-- ناوبری به ویرایش
            )

            is Screen.Settings -> {
                BackHandler { deviceInfoViewModel.navigateBack() }
                // پاس دادن کل ViewModel به صفحه تنظیمات
                SettingsScreen(
                    viewModel = deviceInfoViewModel,
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

            // مدیریت نمایش صفحه جدید ویرایش داشبورد
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