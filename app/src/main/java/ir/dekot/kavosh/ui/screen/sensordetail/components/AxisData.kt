package ir.dekot.kavosh.ui.screen.sensordetail.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * یک کامپوننت اشتراکی برای نمایش داده‌های یک محور (مثل X, Y, Z).
 */
@Composable
fun AxisData(label: String, value: Float, maxValue: Float) {
    val progress = (value / maxValue).coerceIn(-1f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "axis_progress_$label")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            Text(text = "%.2f".format(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (animatedProgress + 1) / 2 },
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
    }
}