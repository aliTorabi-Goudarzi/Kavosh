package ir.dekot.kavosh.ui.screen

// این فایل شامل تمام کامپوزبل‌هایی است که برای ساختن UI استفاده می‌شوند.
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.ui.screen.dashboard.DashboardScreen
import ir.dekot.kavosh.ui.screen.splash.SplashScreen
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.Screen
import ir.dekot.kavosh.ui.viewmodel.BatteryViewModel
import ir.dekot.kavosh.ui.viewmodel.SocViewModel

// کامپوزبل اصلی که نقش ناوبر (Navigator) را دارد
@RequiresApi(Build.VERSION_CODES.R)
// کامپوزبل اصلی که نقش ناوبر (Navigator) را دارد
@Composable
fun DeviceInspectorApp(
    deviceInfoViewModel: DeviceInfoViewModel,
    batteryViewModel: BatteryViewModel, // پارامتر جدید
    socViewModel: SocViewModel, // پارامتر جدید
    onStartScan: () -> Unit // <-- پارامتر جدید برای لامبدا

) {
    val currentScreen by deviceInfoViewModel.currentScreen.collectAsState()

    Crossfade(targetState = currentScreen, label = "screen_fade") { screen ->
        when (screen) {
            is Screen.Splash -> SplashScreen(
                // لامبدا را مستقیماً به صفحه اسپلش پاس می‌دهیم
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
                    batteryViewModel = batteryViewModel, // پاس دادن به صفحه جزئیات
                    socViewModel = socViewModel,           // پاس دادن به صفحه جزئیات
                    onBackClick = { deviceInfoViewModel.navigateBack() }
                )
            }
        }
    }
}
// یک تابع کمکی برای عنوان‌های داخل کارت
@Composable
internal fun SectionTitleInCard(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}
