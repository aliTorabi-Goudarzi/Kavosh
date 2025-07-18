package ir.dekot.kavosh.feature_customeTheme.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ir.dekot.kavosh.feature_customeTheme.ColorTheme
import ir.dekot.kavosh.feature_customeTheme.Theme

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

/**
 * تابع کمکی برای ایجاد ColorScheme با رنگ‌های سفارشی
 */
private fun createCustomColorScheme(
    colorTheme: ColorTheme,
    isDark: Boolean
): ColorScheme {
    val primaryColor = colorTheme.primaryColor
    val secondaryColor = colorTheme.primaryColor
//        colorTheme.secondaryColor ?: Color(0xFF03DAC6)

    return if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2C2C2C),
            onPrimary = if (isLightColor(primaryColor)) Color.Black else Color.White,
            onSecondary = if (isLightColor(secondaryColor)) Color.Black else Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFFCCCCCC)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            onPrimary = if (isLightColor(primaryColor)) Color.Black else Color.White,
            onSecondary = if (isLightColor(secondaryColor)) Color.Black else Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
    }
}

/**
 * تابع کمکی برای ایجاد ColorScheme AMOLED با رنگ‌های سفارشی
 */
private fun createAmoledColorScheme(colorTheme: ColorTheme): ColorScheme {
    val primaryColor = colorTheme.primaryColor
    val secondaryColor = colorTheme.primaryColor
//        colorTheme.secondaryColor ?: Color(0xFF03DAC6)

    return darkColorScheme(
        primary = primaryColor,
        secondary = secondaryColor,
        background = Color.Black,
        surface = Color.Black,
        surfaceVariant = Color(0xFF1A1A1A),
        onPrimary = if (isLightColor(primaryColor)) Color.Black else Color.White,
        onSecondary = if (isLightColor(secondaryColor)) Color.Black else Color.White,
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color(0xFFCCCCCC)
    )
}

/**
 * تابع کمکی برای تشخیص رنگ‌های روشن
 */
private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.5
}

@Composable
fun KavoshTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    theme: Theme = Theme.SYSTEM,
    colorTheme: ColorTheme? = null, // پارامتر جدید برای تم رنگی
    content: @Composable () -> Unit
) {
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current

    val colorScheme = when {
        // اگر تم رنگی سفارشی انتخاب شده باشد
        colorTheme != null -> {
            when (theme) {
                Theme.AMOLED -> createAmoledColorScheme(colorTheme)
                else -> createCustomColorScheme(colorTheme, darkTheme)
            }
        }
        // منطق قبلی برای تم‌های پیش‌فرض
        theme == Theme.AMOLED -> AmoledColorScheme
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