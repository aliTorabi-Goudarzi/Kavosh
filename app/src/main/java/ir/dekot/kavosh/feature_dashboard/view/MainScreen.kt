package ir.dekot.kavosh.feature_dashboard.view

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
import ir.dekot.kavosh.core.navigation.NavigationViewModel
import ir.dekot.kavosh.feature_dashboard.viewModel.DashboardViewModel
import ir.dekot.kavosh.feature_export_and_sharing.viewModel.ExportViewModel
import ir.dekot.kavosh.feature_settings.viewModel.SettingsViewModel
import ir.dekot.kavosh.core.navigation.FloatingBottomNavigation
import ir.dekot.kavosh.core.navigation.BottomNavItem
import ir.dekot.kavosh.feature_settings.view.SettingsScreen
import ir.dekot.kavosh.feature_export_and_sharing.view.ShareScreen
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel
import ir.dekot.kavosh.feature_testing.view.TestsScreen

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
