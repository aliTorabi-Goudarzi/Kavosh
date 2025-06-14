package ir.dekot.kavosh.ui.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Crossfade(targetState = currentScreen, label = "screen_fade") { screen ->
        when (screen) {
            is Screen.Splash -> SplashScreen(
                onStartScan = onStartScan,
                viewModel = deviceInfoViewModel
            )

            is Screen.Dashboard -> DashboardScreen(onCategoryClick = { category, context ->
                deviceInfoViewModel.navigateToDetail(category)
            })

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