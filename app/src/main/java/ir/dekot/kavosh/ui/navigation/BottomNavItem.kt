package ir.dekot.kavosh.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * enum برای آیتم‌های Navigation Bar پایین
 * شامل چهار بخش اصلی برنامه
 */
enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val titleResId: Int,
    val descriptionResId: Int
) {
    /**
     * بخش اطلاعات - نمایش تمام کارت‌های اطلاعات دستگاه
     */
    INFO(
        route = "info",
        icon = Icons.Default.Home,
        titleResId = ir.dekot.kavosh.R.string.nav_info,
        descriptionResId = ir.dekot.kavosh.R.string.nav_info_desc
    ),
    
    /**
     * بخش تست‌ها - ابزارهای تست سخت‌افزار
     */
    TESTS(
        route = "tests",
        icon = Icons.Default.Build,
        titleResId = ir.dekot.kavosh.R.string.nav_tests,
        descriptionResId = ir.dekot.kavosh.R.string.nav_tests_desc
    ),
    
    /**
     * بخش تنظیمات - تنظیمات برنامه
     */
    SETTINGS(
        route = "settings",
        icon = Icons.Default.Settings,
        titleResId = ir.dekot.kavosh.R.string.nav_settings,
        descriptionResId = ir.dekot.kavosh.R.string.nav_settings_desc
    ),
    
    /**
     * بخش اشتراک‌گذاری - خروجی و اشتراک‌گذاری اطلاعات
     */
    SHARE(
        route = "share",
        icon = Icons.Default.Share,
        titleResId = ir.dekot.kavosh.R.string.nav_share,
        descriptionResId = ir.dekot.kavosh.R.string.nav_share_desc
    )
}
