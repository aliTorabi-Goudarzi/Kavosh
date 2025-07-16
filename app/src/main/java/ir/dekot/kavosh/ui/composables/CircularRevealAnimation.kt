package ir.dekot.kavosh.ui.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import kotlin.math.sqrt

/**
 * انیمیشن دایره‌ای برای تغییر تم
 * مطابق با Material Design 3 و پشتیبانی از RTL/LTR
 */
@Composable
fun CircularRevealAnimation(
    targetState: Boolean,
    modifier: Modifier = Modifier,
    animationDurationMs: Int = 400,
    startFromCenter: Boolean = true,
    revealColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    val animationProgress = remember { Animatable(if (targetState) 1f else 0f) }
    
    LaunchedEffect(targetState) {
        animationProgress.animateTo(
            targetValue = if (targetState) 1f else 0f,
            animationSpec = tween(durationMillis = animationDurationMs)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                size = coordinates.size
            }
            .clipToBounds()
    ) {
        content()
        
        if (size != IntSize.Zero) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCircularReveal(
                    progress = animationProgress.value,
                    size = size,
                    startFromCenter = startFromCenter,
                    color = revealColor
                )
            }
        }
    }
}

/**
 * رسم انیمیشن دایره‌ای
 */
private fun DrawScope.drawCircularReveal(
    progress: Float,
    size: IntSize,
    startFromCenter: Boolean,
    color: Color
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    
    // محاسبه شعاع بر اساس قطر صفحه
    val maxRadius = sqrt(
        (centerX * centerX) + (centerY * centerY)
    )
    
    val currentRadius = maxRadius * progress
    
    val center = if (startFromCenter) {
        Offset(centerX, centerY)
    } else {
        // شروع از گوشه (برای RTL/LTR)
        Offset(0f, 0f)
    }
    
    if (progress > 0f) {
        drawCircle(
            color = color,
            radius = currentRadius,
            center = center
        )
    }
}

/**
 * انیمیشن دایره‌ای برای تغییر تم با شروع از نقطه خاص
 */
@Composable
fun CircularRevealFromPoint(
    targetState: Boolean,
    startPoint: Offset,
    modifier: Modifier = Modifier,
    animationDurationMs: Int = 400,
    revealColor: Color = Color.Black,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    val animationProgress = remember { Animatable(if (targetState) 1f else 0f) }
    
    LaunchedEffect(targetState) {
        animationProgress.animateTo(
            targetValue = if (targetState) 1f else 0f,
            animationSpec = tween(durationMillis = animationDurationMs)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                size = coordinates.size
            }
            .clipToBounds()
    ) {
        content()
        
        if (size != IntSize.Zero) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCircularRevealFromPoint(
                    progress = animationProgress.value,
                    size = size,
                    startPoint = startPoint,
                    color = revealColor
                )
            }
        }
    }
}

/**
 * رسم انیمیشن دایره‌ای از نقطه خاص
 */
private fun DrawScope.drawCircularRevealFromPoint(
    progress: Float,
    size: IntSize,
    startPoint: Offset,
    color: Color
) {
    // محاسبه حداکثر شعاع تا پوشش کامل صفحه
    val corners = listOf(
        Offset(0f, 0f),
        Offset(size.width.toFloat(), 0f),
        Offset(0f, size.height.toFloat()),
        Offset(size.width.toFloat(), size.height.toFloat())
    )
    
    val maxRadius = corners.maxOf { corner ->
        sqrt(
            (startPoint.x - corner.x) * (startPoint.x - corner.x) +
            (startPoint.y - corner.y) * (startPoint.y - corner.y)
        )
    }
    
    val currentRadius = maxRadius * progress
    
    if (progress > 0f) {
        drawCircle(
            color = color,
            radius = currentRadius,
            center = startPoint
        )
    }
}

/**
 * انیمیشن دایره‌ای ساده برای تغییر تم
 * مناسب برای استفاده در دیالوگ‌ها
 */
@Composable
fun SimpleCircularReveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CircularRevealAnimation(
        targetState = visible,
        modifier = modifier,
        animationDurationMs = AnimationConstants.NORMAL_ANIMATION_DURATION,
        startFromCenter = true,
        content = content
    )
}
