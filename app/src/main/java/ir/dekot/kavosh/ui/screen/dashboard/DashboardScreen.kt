package ir.dekot.kavosh.ui.screen.dashboard

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import androidx.compose.material.icons.filled.PhotoCamera // <-- ایمپورت آیکون جدید

// امضای تابع را برای دریافت یک لامبدا برای ناوبری به تنظیمات تغییر می‌دهیم
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCategoryClick: (InfoCategory, Context) -> Unit,
    onSettingsClick: () -> Unit // <-- لامبدای جدید
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("کاوش") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val categories = listOf(
            DashboardItem(InfoCategory.SOC, "پردازنده", Icons.Default.Memory),
            DashboardItem(InfoCategory.DEVICE, "دستگاه", Icons.Default.PhoneAndroid),
            DashboardItem(InfoCategory.SYSTEM, "سیستم", Icons.Default.Android),
            DashboardItem(InfoCategory.BATTERY, "باتری", Icons.Default.BatteryFull),
            DashboardItem(InfoCategory.SENSORS, "سنسورها", Icons.Default.Sensors),
            DashboardItem(InfoCategory.THERMAL, "دما", Icons.Default.Thermostat),
            DashboardItem(InfoCategory.NETWORK, "شبکه", Icons.Default.NetworkWifi),
            DashboardItem(InfoCategory.CAMERA, "دوربین", Icons.Default.PhotoCamera) // <-- آیتم جدید
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(paddingValues) // <-- استفاده از paddingValues
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { item ->
                DashboardTile(item = item, onClick = { onCategoryClick(item.category, context) })
            }
        }
    }
}