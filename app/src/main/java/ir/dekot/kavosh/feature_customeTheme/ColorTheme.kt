package ir.dekot.kavosh.feature_customeTheme

import androidx.compose.ui.graphics.Color
import ir.dekot.kavosh.R

/**
 * مدل برای تم‌های رنگی مختلف
 * هر تم شامل رنگ اصلی و رنگ ثانویه است
 */
data class ColorTheme(
    val id: String,
    val nameResId: Int,
    val primaryColor: Color,
    val secondaryColor: Color? = null,
    val isCustom: Boolean = false
)

/**
 * enum برای تم‌های رنگی از پیش تعریف شده
 */
enum class PredefinedColorTheme(
    val id: String,
    val nameResId: Int,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    BLUE(
        id = "blue",
        nameResId = R.string.color_theme_blue,
        primaryColor = Color(0xFF2196F3),
        secondaryColor = Color(0xFF03DAC6)
    ),
    GREEN(
        id = "green", 
        nameResId = R.string.color_theme_green,
        primaryColor = Color(0xFF4CAF50),
        secondaryColor = Color(0xFF8BC34A)
    ),
    RED(
        id = "red",
        nameResId = R.string.color_theme_red,
        primaryColor = Color(0xFFF44336),
        secondaryColor = Color(0xFFFF5722)
    ),
    PURPLE(
        id = "purple",
        nameResId = R.string.color_theme_purple,
        primaryColor = Color(0xFF9C27B0),
        secondaryColor = Color(0xFF673AB7)
    ),
    ORANGE(
        id = "orange",
        nameResId = R.string.color_theme_orange,
        primaryColor = Color(0xFFFF9800),
        secondaryColor = Color(0xFFFF5722)
    ),
    TEAL(
        id = "teal",
        nameResId = R.string.color_theme_teal,
        primaryColor = Color(0xFF009688),
        secondaryColor = Color(0xFF00BCD4)
    ),
    INDIGO(
        id = "indigo",
        nameResId = R.string.color_theme_indigo,
        primaryColor = Color(0xFF3F51B5),
        secondaryColor = Color(0xFF2196F3)
    ),
    PINK(
        id = "pink",
        nameResId = R.string.color_theme_pink,
        primaryColor = Color(0xFFE91E63),
        secondaryColor = Color(0xFF9C27B0)
    );

    fun toColorTheme(): ColorTheme {
        return ColorTheme(
            id = id,
            nameResId = nameResId,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            isCustom = false
        )
    }

    companion object {
        fun fromId(id: String): PredefinedColorTheme? {
            return entries.find { it.id == id }
        }
        
        fun getDefault(): PredefinedColorTheme = BLUE
    }
}

/**
 * کلاس برای مدیریت تم‌های رنگی سفارشی
 */
data class CustomColorTheme(
    val primaryColor: Color,
    val secondaryColor: Color? = null
) {
    fun toColorTheme(): ColorTheme {
        return ColorTheme(
            id = "custom",
            nameResId = R.string.color_theme_custom,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            isCustom = true
        )
    }
}
