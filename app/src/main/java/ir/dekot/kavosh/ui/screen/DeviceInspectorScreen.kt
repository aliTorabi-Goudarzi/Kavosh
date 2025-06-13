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

// کامپوزبل اصلی که نقش ناوبر (Navigator) را دارد
@RequiresApi(Build.VERSION_CODES.R)
// کامپوزبل اصلی که نقش ناوبر (Navigator) را دارد
@Composable
fun DeviceInspectorApp(viewModel: DeviceInfoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // با استفاده از Crossroad، انیمیشن نرمی بین صفحات خواهیم داشت
    Crossfade(targetState = currentScreen, label = "screen_fade") { screen ->
        when (screen) {
            is Screen.Splash -> SplashScreen(
                onStartScan = { viewModel.startScan() },
                viewModel = viewModel
            )

            is Screen.Dashboard -> DashboardScreen(onCategoryClick = { category, context ->
                viewModel.navigateToDetail(category, context) // فراخوانی جدید
            })

            is Screen.Detail -> {
                // مدیریت دکمه بازگشت سخت‌افزاری
                BackHandler { viewModel.navigateBack() }
                DetailScreen(
                    category = screen.category,
                    viewModel = viewModel,
                    onBackClick = { viewModel.navigateBack() }
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
