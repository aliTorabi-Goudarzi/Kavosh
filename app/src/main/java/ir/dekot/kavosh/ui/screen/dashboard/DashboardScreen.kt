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
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.dashboard.DashboardItem
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

// --- صفحه داشبورد (Dashboard) ---
@Composable
fun DashboardScreen(onCategoryClick: (InfoCategory, Context) -> Unit) { // ورودی تابع را تغییر دهید
    val context = LocalContext.current // context را اینجا بگیرید
    val categories = listOf(
        DashboardItem(InfoCategory.SOC, "پردازنده", Icons.Default.Memory),
        DashboardItem(InfoCategory.DEVICE, "دستگاه", Icons.Default.PhoneAndroid),
        DashboardItem(InfoCategory.SYSTEM, "سیستم", Icons.Default.Android),
        DashboardItem(InfoCategory.BATTERY, "باتری", Icons.Default.BatteryFull),
        DashboardItem(InfoCategory.SENSORS, "سنسورها", Icons.Default.Sensors),
        DashboardItem(
            InfoCategory.THERMAL,
            "دما",
            Icons.Default.Thermostat
        ) // <-- این خط را اضافه کنید
    )



    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            // این مادیفایر به طور هوشمند به اندازه ارتفاع استاتوس بار، از بالا فاصله ایجاد می‌کند
            .windowInsetsPadding(WindowInsets.statusBars)
            // این مادیفایر هم برای ایجاد فاصله در اطراف کل گرید است
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { item ->
            // context را اینجا پاس دهید
            DashboardTile(item = item, onClick = { onCategoryClick(item.category, context) })
        }
    }
}