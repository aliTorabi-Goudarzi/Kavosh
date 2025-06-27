package ir.dekot.kavosh.ui.screen.sensordetail.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import kotlin.math.ln

/**
 * کامپوزبل اختصاصی برای نمایش داده‌های سنسور نور.
 * این View حالا فقط مقدار عددی نور را به عنوان ورودی می‌گیرد.
 * @param lightValue مقدار روشنایی لحظه‌ای بر حسب lux.
 */
@Composable
fun LightSensorView(lightValue: Float) {
    val lightLevelFactor = if (lightValue > 0) (ln(lightValue.coerceAtLeast(1f)) / ln(40000f)).coerceIn(0f, 1f) else 0f

    val animatedSize by animateFloatAsState(targetValue = 100 + (200 * lightLevelFactor), label = "sizeAnim")
    val animatedColor by animateColorAsState(targetValue = Color.Yellow.copy(alpha = lightLevelFactor), label = "colorAnim")

    Box(
        modifier = Modifier
            .size(animatedSize.dp)
            .clip(CircleShape)
            .background(animatedColor),
        contentAlignment = Alignment.Center
    ) {}
    Spacer(modifier = Modifier.height(32.dp))
    Text(
        text = stringResource(R.string.unit_format_lux, lightValue),
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = stringResource(R.string.illuminance),
        style = MaterialTheme.typography.titleLarge
    )
}