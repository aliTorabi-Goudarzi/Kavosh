package ir.dekot.kavosh.ui.composables // <-- تغییر در این خط

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * این فایل شامل کامپوزبل‌های اشتراکی و ثوابت انیمیشن است که در صفحات مختلف استفاده می‌شوند.
 */

/**
 * ثوابت انیمیشن مطابق با Material Design 3
 * برای حفظ یکنواختی در تمام اپلیکیشن
 */
object AnimationConstants {
    // مدت زمان انیمیشن‌ها
    const val FAST_ANIMATION_DURATION = 150
    const val NORMAL_ANIMATION_DURATION = 300
    const val SLOW_ANIMATION_DURATION = 500
    const val EXTRA_SLOW_ANIMATION_DURATION = 800

    // انیمیشن‌های صفحه
    const val PAGE_TRANSITION_DURATION = 400
    const val PAGE_TRANSITION_DELAY = 50

    // انیمیشن‌های دکمه
    const val BUTTON_PRESS_DURATION = 100
    const val BUTTON_RELEASE_DURATION = 200
    const val BUTTON_SCALE_PRESSED = 0.95f
    const val BUTTON_SCALE_NORMAL = 1.0f

    // انیمیشن‌های کارت
    const val CARD_HOVER_DURATION = 200
    const val CARD_ELEVATION_NORMAL = 4f
    const val CARD_ELEVATION_PRESSED = 8f

    // انیمیشن‌های بارگذاری
    const val LOADING_PULSE_DURATION = 1200
    const val LOADING_WAVE_DURATION = 2000
    const val SKELETON_SHIMMER_DURATION = 1500

    // انیمیشن‌های Bottom Sheet
    const val BOTTOM_SHEET_DURATION = 350
    const val BOTTOM_SHEET_SPRING_DAMPING = 0.8f
    const val BOTTOM_SHEET_SPRING_STIFFNESS = 400f
}

/**
 * مشخصات انیمیشن پیش‌تعریف شده
 */
object AnimationSpecs {
    // انیمیشن‌های عمومی
    val fastTween = tween<Float>(AnimationConstants.FAST_ANIMATION_DURATION, easing = FastOutSlowInEasing)
    val normalTween = tween<Float>(AnimationConstants.NORMAL_ANIMATION_DURATION, easing = EaseInOutCubic)
    val slowTween = tween<Float>(AnimationConstants.SLOW_ANIMATION_DURATION, easing = EaseInOut)

    // انیمیشن‌های دکمه
    val buttonPressSpec = tween<Float>(AnimationConstants.BUTTON_PRESS_DURATION, easing = EaseInOutCubic)
    val buttonReleaseSpec = tween<Float>(AnimationConstants.BUTTON_RELEASE_DURATION, easing = EaseOutBack)

    // انیمیشن‌های صفحه
    val pageTransitionSpec = tween<Float>(AnimationConstants.PAGE_TRANSITION_DURATION, easing = EaseInOutCubic)

    // انیمیشن‌های فنری
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val bottomSheetSpring = spring<Float>(
        dampingRatio = AnimationConstants.BOTTOM_SHEET_SPRING_DAMPING,
        stiffness = AnimationConstants.BOTTOM_SHEET_SPRING_STIFFNESS
    )
}

/**
 * یک تابع کمکی برای نمایش عنوان‌های داخل کارت‌های اطلاعاتی.
 */
@Composable
fun SectionTitleInCard(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

/**
 * یک کامپوننت برای نمایش یک پیام متنی در مرکز صفحه، مناسب برای وضعیت‌های خالی.
 */
@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * دکمه با انیمیشن فشردن حرفه‌ای
 * بدون هیچ‌گونه افکت بصری کلیک (ریپل یا گلو)
 */
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (enabled) AnimationConstants.BUTTON_SCALE_NORMAL else AnimationConstants.BUTTON_SCALE_PRESSED,
        animationSpec = AnimationSpecs.buttonReleaseSpec,
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // حذف کامل افکت‌های بصری
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * کارت با انیمیشن تعامل
 * بدون هیچ‌گونه افکت بصری کلیک (ریپل یا گلو)
 */
@Composable
fun AnimatedCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1.0f else 0.98f,
        animationSpec = AnimationSpecs.normalTween,
        label = "cardScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1.0f else 0.7f,
        animationSpec = AnimationSpecs.normalTween,
        label = "cardAlpha"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null, // حذف کامل افکت‌های بصری
                        enabled = enabled
                    ) { onClick() }
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * انیمیشن بارگذاری پیشرفته با افکت نبض
 * مناسب برای تست‌های تشخیصی
 */
@Composable
fun ProfessionalLoadingIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingTransition")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationConstants.LOADING_PULSE_DURATION, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationConstants.LOADING_PULSE_DURATION, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // دایره پس‌زمینه
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .alpha(alpha * 0.3f)
                .clip(CircleShape)
                .background(color)
        )

        // دایره اصلی
        Box(
            modifier = Modifier
                .size(size * 0.6f)
                .scale(scale * 0.8f)
                .alpha(alpha)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * انیمیشن بارگذاری موجی
 * مناسب برای نمایش پیشرفت تست‌ها
 */
@Composable
fun WaveLoadingIndicator(
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(AnimationConstants.LOADING_WAVE_DURATION, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = AnimationSpecs.slowTween,
        label = "waveProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // نوار پیشرفت پس‌زمینه
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 4.dp)
        )

        // نوار پیشرفت انیمیت شده
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(vertical = 4.dp)
                .graphicsLayer {
                    alpha = 0.8f + (waveOffset * 0.2f)
                }
        )
    }
}