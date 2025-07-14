package ir.dekot.kavosh.ui.screen.main

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
    onNetworkToolsClick: () -> Unit,
    onHealthCheckClick: () -> Unit,
    onPerformanceScoreClick: () -> Unit,
    onDeviceComparisonClick: () -> Unit
) {
    // **اصلاح: استفاده از NavigationViewModel برای مدیریت بخش فعلی**
    val selectedTab by navigationViewModel.currentBottomNavSection.collectAsState()
    val context = LocalContext.current

    // **اصلاح: مدیریت دکمه بازگشت در سطح بالای هر بخش**
    // اگر کاربر در INFO tab باشد، دکمه back باید برنامه را ببندد
    // اگر در تب‌های دیگر باشد، باید به INFO tab برگردد
    BackHandler(enabled = selectedTab != BottomNavItem.INFO) {
        navigationViewModel.setBottomNavSection(BottomNavItem.INFO)
    }

    Scaffold(
        bottomBar = {
            FloatingBottomNavigation(
                selectedItem = selectedTab,
                onItemSelected = { navigationViewModel.setBottomNavSection(it) }
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
                        onNetworkToolsClick = onNetworkToolsClick,
                        onHealthCheckClick = onHealthCheckClick,
                        onPerformanceScoreClick = onPerformanceScoreClick,
                        onDeviceComparisonClick = onDeviceComparisonClick
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
