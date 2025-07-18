package ir.dekot.kavosh.core.ui.shared_components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.core.navigation.Screen

/**
 * کامپوننت انیمیشن انتقال صفحات با پشتیبانی از RTL/LTR
 * انیمیشن‌های افقی لغزنده مطابق با جهت زبان
 */
@Composable
fun AnimatedScreenTransition(
    currentScreen: Screen,
    modifier: Modifier = Modifier,
    content: @Composable (Screen) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    
    AnimatedContent(
        targetState = currentScreen,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            getScreenTransitionSpec(
                initialScreen = initialState,
                targetScreen = targetState,
                isRtl = isRtl
            )
        },
        label = "screenTransition"
    ) { screen ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(screen)
        }
    }
}

/**
 * تعیین نوع انیمیشن بر اساس صفحات مبدا و مقصد
 */
private fun AnimatedContentTransitionScope<Screen>.getScreenTransitionSpec(
    initialScreen: Screen,
    targetScreen: Screen,
    isRtl: Boolean
): ContentTransform {
    val isForward = isForwardNavigation(initialScreen, targetScreen)
    
    return if (isForward) {
        // انیمیشن رفتن به جلو
        slideInHorizontally(
            animationSpec = tween(AnimationConstants.PAGE_TRANSITION_DURATION),
            initialOffsetX = { fullWidth -> 
                if (isRtl) -fullWidth else fullWidth 
            }
        ) togetherWith slideOutHorizontally(
            animationSpec = tween(AnimationConstants.PAGE_TRANSITION_DURATION),
            targetOffsetX = { fullWidth -> 
                if (isRtl) fullWidth else -fullWidth 
            }
        )
    } else {
        // انیمیشن برگشت
        slideInHorizontally(
            animationSpec = tween(AnimationConstants.PAGE_TRANSITION_DURATION),
            initialOffsetX = { fullWidth -> 
                if (isRtl) fullWidth else -fullWidth 
            }
        ) togetherWith slideOutHorizontally(
            animationSpec = tween(AnimationConstants.PAGE_TRANSITION_DURATION),
            targetOffsetX = { fullWidth -> 
                if (isRtl) -fullWidth else fullWidth 
            }
        )
    }
}

/**
 * تشخیص جهت ناوبری (رفتن به جلو یا برگشت)
 * بر اساس سلسله مراتب صفحات
 */
private fun isForwardNavigation(from: Screen, to: Screen): Boolean {
    val screenHierarchy = mapOf(
        Screen.Splash::class to 0,
        Screen.Dashboard::class to 1,
        Screen.Settings::class to 2,
        Screen.Detail::class to 2,
        Screen.SensorDetail::class to 3,
        Screen.EditDashboard::class to 2,
        Screen.About::class to 3,
        Screen.CpuStressTest::class to 2,
        Screen.NetworkTools::class to 2,
        Screen.DisplayTest::class to 2,
        Screen.StorageTest::class to 2,
        Screen.HealthCheck::class to 2,
        Screen.PerformanceScore::class to 2,
        Screen.DeviceComparison::class to 2
    )
    
    val fromLevel = screenHierarchy[from::class] ?: 1
    val toLevel = screenHierarchy[to::class] ?: 1
    
    return toLevel > fromLevel
}

/**
 * انیمیشن ظاهر شدن Bottom Sheet
 * با فیزیک فنری طبیعی
 */
@Composable
fun AnimatedBottomSheet(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = AnimationConstants.BOTTOM_SHEET_DURATION,
                easing = EaseOutCubic
            ),
            initialOffsetY = { it }
        ) + fadeIn(
            animationSpec = tween(AnimationConstants.BOTTOM_SHEET_DURATION)
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = AnimationConstants.BOTTOM_SHEET_DURATION,
                easing = EaseInCubic
            ),
            targetOffsetY = { it }
        ) + fadeOut(
            animationSpec = tween(AnimationConstants.BOTTOM_SHEET_DURATION)
        )
    ) {
        content()
    }
}

/**
 * انیمیشن گسترش/جمع شدن عناصر
 * مناسب برای منوها و لیست‌ها
 */
@Composable
fun AnimatedExpandableContent(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = spring(),
        ) + fadeIn(
            animationSpec = AnimationSpecs.normalTween
        ),
        exit = shrinkVertically(
            animationSpec = spring()
        ) + fadeOut(
            animationSpec = AnimationSpecs.normalTween
        )
    ) {
        content()
    }
}

/**
 * انیمیشن اسکلتون برای بارگذاری محتوا
 * با افکت درخشش
 */
@Composable
fun SkeletonLoadingAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "skeletonTransition"
    )
    
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = AnimationConstants.SKELETON_SHIMMER_DURATION,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = shimmerAlpha
                ),
                RoundedCornerShape(8.dp)
            )
    )
}
