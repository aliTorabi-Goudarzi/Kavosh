package ir.dekot.kavosh.ui.screen.dashboard

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

@Composable
fun DashboardScreen(onCategoryClick: (InfoCategory, Context) -> Unit) {
    val context = LocalContext.current
    val categories = listOf(
        DashboardItem(InfoCategory.SOC, "پردازنده", Icons.Default.Memory),
        DashboardItem(InfoCategory.DEVICE, "دستگاه", Icons.Default.PhoneAndroid),
        DashboardItem(InfoCategory.SYSTEM, "سیستم", Icons.Default.Android),
        DashboardItem(InfoCategory.BATTERY, "باتری", Icons.Default.BatteryFull),
        DashboardItem(InfoCategory.SENSORS, "سنسورها", Icons.Default.Sensors),
        DashboardItem(InfoCategory.THERMAL, "دما", Icons.Default.Thermostat),
        DashboardItem(InfoCategory.NETWORK, "شبکه", Icons.Default.NetworkWifi)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { item ->
            DashboardTile(item = item, onClick = { onCategoryClick(item.category, context) })
        }
    }
}