package ir.dekot.kavosh.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// پالت رنگی ثابت برای حالت تاریک (برای اندرویدهای قدیمی‌تر)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCCCCCC)
)

// **اصلاح ۱: تعریف پالت رنگی جدید برای تم AMOLED**
private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color.Black, // <-- پس‌زمینه کاملاً مشکی
    surface = Color.Black,    // <-- سطح کاملاً مشکی
    surfaceVariant = Color(0xFF1A1A1A), // کمی روشن‌تر برای تمایز جزئی
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCCCCCC)
)

// پالت رنگی ثابت برای حالت روشن (برای اندرویدهای قدیمی‌تر)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// ... (پالت‌های رنگی ثابت بدون تغییر) ...

@Composable
fun KavoshTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true, // پارامتر جدید برای کنترل تم پویا
    // **اصلاح ۲: پارامتر جدید برای دریافت نوع تم**
    theme: ir.dekot.kavosh.data.model.settings.Theme = ir.dekot.kavosh.data.model.settings.Theme.SYSTEM,
    content: @Composable () -> Unit
) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current

    val colorScheme = when {
        // **اصلاح ۳: منطق انتخاب پالت رنگی**
        theme == ir.dekot.kavosh.data.model.settings.Theme.AMOLED -> AmoledColorScheme
        dynamicColor && supportsDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && supportsDynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
