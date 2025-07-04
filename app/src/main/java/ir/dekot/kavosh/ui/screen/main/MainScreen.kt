package ir.dekot.kavosh.ui.screen.main

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ir.dekot.kavosh.ui.composables.FloatingBottomNavigation
import ir.dekot.kavosh.ui.navigation.BottomNavItem
import ir.dekot.kavosh.ui.screen.dashboard.DashboardContent
import ir.dekot.kavosh.ui.screen.settings.SettingsScreen
import ir.dekot.kavosh.ui.screen.share.ShareScreen
import ir.dekot.kavosh.ui.screen.tests.TestsScreen
import ir.dekot.kavosh.ui.viewmodel.*

/**
 * صفحه اصلی جدید با Bottom Navigation
 * شامل چهار تب: اطلاعات، تست‌ها، تنظیمات، اشتراک‌گذاری
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MainScreen(
    deviceInfoViewModel: DeviceInfoViewModel,
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    exportViewModel: ExportViewModel,
    navigationViewModel: NavigationViewModel,
    onCategoryClick: (InfoCategory, Context) -> Unit,
    onNavigateToAbout: () -> Unit,
    onEditDashboardClick: () -> Unit,
    onCpuStressTestClick: () -> Unit,
    onStorageTestClick: () -> Unit,
    onDisplayTestClick: () -> Unit,
    onNetworkToolsClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomNavItem.INFO) }
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            FloatingBottomNavigation(
                selectedItem = selectedTab,
                onItemSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                BottomNavItem.INFO -> {
                    DashboardContent(
                        settingsViewModel = settingsViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onCategoryClick = onCategoryClick
                    )
                }
                
                BottomNavItem.TESTS -> {
                    TestsScreen(
                        onCpuStressTestClick = onCpuStressTestClick,
                        onStorageTestClick = onStorageTestClick,
                        onDisplayTestClick = onDisplayTestClick,
                        onNetworkToolsClick = onNetworkToolsClick
                    )
                }
                
                BottomNavItem.SETTINGS -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToAbout = onNavigateToAbout,
                        onEditDashboardClick = onEditDashboardClick,
                        onBackClick = { /* در MainScreen دکمه back نداریم */ }
                    )
                }
                
                BottomNavItem.SHARE -> {
                    ShareScreen(
                        exportViewModel = exportViewModel
                    )
                }
            }
        }
    }
}
