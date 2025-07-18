package ir.dekot.kavosh.core.ui.shared_components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * کامپوننت انتخابگر رنگ با چرخ رنگی
 */
@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }

    // تبدیل رنگ انتخاب شده به HSV
    LaunchedEffect(selectedColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(selectedColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // چرخ رنگی
        ColorWheel(
            hue = hue,
            saturation = saturation,
            onColorChange = { newHue, newSaturation ->
                hue = newHue
                saturation = newSaturation
                val color = Color.hsv(hue, saturation, brightness)
                onColorSelected(color)
            },
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // نوار روشنایی
        BrightnessSlider(
            brightness = brightness,
            hue = hue,
            saturation = saturation,
            onBrightnessChange = { newBrightness ->
                brightness = newBrightness
                val color = Color.hsv(hue, saturation, brightness)
                onColorSelected(color)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // نمایش رنگ انتخاب شده
        ColorPreview(
            color = selectedColor,
            modifier = Modifier.size(60.dp)
        )
    }
}

/**
 * چرخ رنگی برای انتخاب Hue و Saturation
 */
@Composable
private fun ColorWheel(
    hue: Float,
    saturation: Float,
    onColorChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    handleColorWheelTouch(offset,
                        Size(size.width.toFloat(), size.height.toFloat()), onColorChange)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        handleColorWheelTouch(offset,
                            Size(size.width.toFloat(), size.height.toFloat()), onColorChange)
                    },
                    onDrag = { _, offset ->
                        handleColorWheelTouch(offset,
                            Size(size.width.toFloat(), size.height.toFloat()), onColorChange)
                    }
                )
            }
    ) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // رسم چرخ رنگی
        drawColorWheel(center, radius)
        
        // رسم نشانگر موقعیت فعلی
        val angle = Math.toRadians(hue.toDouble())
        val distance = saturation * radius * 0.8f
        val markerX = center.x + (cos(angle) * distance).toFloat()
        val markerY = center.y + (sin(angle) * distance).toFloat()
        
        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = Offset(markerX, markerY)
        )
        drawCircle(
            color = Color.Black,
            radius = 6.dp.toPx(),
            center = Offset(markerX, markerY)
        )
    }
}

/**
 * تابع کمکی برای پردازش لمس روی چرخ رنگی
 */
private fun handleColorWheelTouch(
    offset: Offset,
    size: Size,
    onColorChange: (Float, Float) -> Unit
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = size.minDimension / 2

    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val distance = sqrt(dx * dx + dy * dy)

    if (distance <= radius) {
        val angle = atan2(dy, dx)
        val hue = (Math.toDegrees(angle.toDouble()).toFloat() + 360) % 360
        val saturation = (distance / (radius * 0.8f)).coerceIn(0f, 1f)

        onColorChange(hue, saturation)
    }
}

/**
 * رسم چرخ رنگی
 */
private fun DrawScope.drawColorWheel(center: Offset, radius: Float) {
    // رسم چرخ رنگی ساده
    val steps = 60 // کاهش تعداد مراحل برای بهبود عملکرد

    for (i in 0 until steps) {
        val startAngle = i * 360f / steps
        val sweepAngle = 360f / steps + 1f // کمی overlap برای پوشش کامل
        val color = Color.hsv(startAngle, 1f, 1f)

        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }

    // رسم دایره سفید در مرکز برای saturation کم
    drawCircle(
        color = Color.White,
        radius = radius * 0.15f,
        center = center
    )
}

/**
 * نوار کنترل روشنایی
 */
@Composable
private fun BrightnessSlider(
    brightness: Float,
    hue: Float,
    saturation: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = brightness,
        onValueChange = onBrightnessChange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color.hsv(hue, saturation, brightness),
            activeTrackColor = Color.hsv(hue, saturation, 1f),
            inactiveTrackColor = Color.hsv(hue, saturation, 0.3f)
        )
    )
}

/**
 * پیش‌نمایش رنگ انتخاب شده
 */
@Composable
private fun ColorPreview(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
    )
}

/**
 * کامپوننت انتخابگر رنگ ساده با رنگ‌های از پیش تعریف شده
 */
@Composable
fun SimpleColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val predefinedColors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFF44336), // Red
        Color(0xFF9C27B0), // Purple
        Color(0xFFFF9800), // Orange
        Color(0xFF009688), // Teal
        Color(0xFF3F51B5), // Indigo
        Color(0xFFE91E63), // Pink
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF795548), // Brown
        Color(0xFF9E9E9E), // Grey
        Color(0xFF000000), // Black
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(predefinedColors.size) { index ->
            val color = predefinedColors[index]
            val isSelected = color == selectedColor
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onColorSelected(color) }
            )
        }
    }
}

/**
 * تابع کمکی برای ایجاد رنگ HSV
 */
private fun Color.Companion.hsv(hue: Float, saturation: Float, value: Float): Color {
    val rgb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    return Color(rgb)
}
