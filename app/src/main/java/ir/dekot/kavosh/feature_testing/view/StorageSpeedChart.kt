package ir.dekot.kavosh.feature_testing.view

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.view.InfoCard
import ir.dekot.kavosh.feature_testing.model.SpeedDataPoint
import kotlin.math.max

/**
 * نمودار زنده سرعت حافظه
 * نمایش سرعت خواندن و نوشتن به صورت real-time
 */
@Composable
fun StorageSpeedChart(
    writeSpeedHistory: List<SpeedDataPoint>,
    readSpeedHistory: List<SpeedDataPoint>,
    currentWriteSpeed: Double,
    currentReadSpeed: Double,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = stringResource(R.string.storage_speed_live_chart_title),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // نمایش سرعت‌های فعلی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpeedIndicator(
                    title = stringResource(R.string.write_speed),
                    speed = currentWriteSpeed,
                    icon = Icons.Default.Upload,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SpeedIndicator(
                    title = stringResource(R.string.read_speed),
                    speed = currentReadSpeed,
                    icon = Icons.Default.Download,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // نمودار سرعت نوشتن
            if (writeSpeedHistory.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.storage_speed_write_chart_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LiveSpeedGraph(
                    speedHistory = writeSpeedHistory,
                    color = MaterialTheme.colorScheme.primary, // نفس رنگ کارت نمایشگر سرعت نوشتن
                    label = stringResource(R.string.write_speed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // نمودار سرعت خواندن
            if (readSpeedHistory.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.storage_speed_read_chart_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LiveSpeedGraph(
                    speedHistory = readSpeedHistory,
                    color = MaterialTheme.colorScheme.secondary, // نفس رنگ کارت نمایشگر سرعت خواندن
                    label = stringResource(R.string.read_speed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

/**
 * نمایشگر سرعت فعلی
 */
@Composable
private fun SpeedIndicator(
    title: String,
    speed: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = if (speed > 0) "${"%.1f".format(speed)} MB/s" else "--",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * نمودار زنده سرعت (تک نمودار)
 */
@Composable
private fun LiveSpeedGraph(
    speedHistory: List<SpeedDataPoint>,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        if (speedHistory.isEmpty()) {
            // نمایش پیام خالی بودن داده
            drawContext.canvas.nativeCanvas.drawText(
                "در انتظار داده...",
                size.width / 2,
                size.height / 2,
                Paint().apply {
                    textSize = 16.dp.toPx()
                    setColor(gridColor.toArgb())
                    textAlign = Paint.Align.CENTER
                }
            )
            return@Canvas
        }

        // محاسبه حداکثر و حداقل سرعت برای مقیاس‌بندی
        val speeds = speedHistory.map { it.speed }.filter { it > 0.0 } // فیلتر کردن مقادیر صفر
        val maxSpeed = max(speeds.maxOrNull() ?: 1.0, 1.0)
        val minSpeed = 0.0 // شروع از صفر برای نمایش بهتر
        val speedRange = maxSpeed - minSpeed

        // رسم شبکه
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = padding + (i * chartHeight / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )
        }

        // رسم نمودار - حتی با یک نقطه داده
        if (speedHistory.isNotEmpty()) {
            // اعمال هموارسازی برای کاهش نوسانات شدید
            val smoothedHistory = smoothSpeedData(speedHistory)

            val path = Path()
            val gradientPath = Path()

            // اگر فقط یک نقطه داریم، یک خط افقی کوتاه رسم کن
            if (smoothedHistory.size == 1) {
                val point = smoothedHistory[0]
                val normalizedSpeed = if (speedRange > 0) (point.speed - minSpeed) / speedRange else 0.0
                val y = padding + chartHeight - (normalizedSpeed * chartHeight).toFloat()
                val startX = padding + chartWidth * 0.1f
                val endX = padding + chartWidth * 0.9f

                // رسم خط افقی صاف
                drawLine(
                    color = color,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            } else {
                // رسم نمودار صاف با چندین نقطه
                smoothedHistory.forEachIndexed { index, point ->
                    val x = padding + (index * chartWidth / (smoothedHistory.size - 1).coerceAtLeast(1))
                    val normalizedSpeed = if (speedRange > 0) (point.speed - minSpeed) / speedRange else 0.0
                    val y = padding + chartHeight - (normalizedSpeed * chartHeight).toFloat()

                    if (index == 0) {
                        path.moveTo(x, y)
                        gradientPath.moveTo(x, padding + chartHeight)
                        gradientPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        gradientPath.lineTo(x, y)
                    }
                }

                // بستن مسیر گرادیان
                gradientPath.lineTo(padding + chartWidth, padding + chartHeight)
                gradientPath.close()

                // رسم گرادیان پس‌زمینه
                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )

                // رسم خط اصلی صاف بدون نقاط
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }

    // نمایش حداکثر و حداقل سرعت
    if (speedHistory.isNotEmpty()) {
        val speeds = speedHistory.map { it.speed }
        val maxSpeed = speeds.maxOrNull() ?: 0.0
        val minSpeed = speeds.minOrNull() ?: 0.0

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "حداقل: ${"%.1f".format(minSpeed)} MB/s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "حداکثر: ${"%.1f".format(maxSpeed)} MB/s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * آیتم راهنمای نمودار
 */
@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .drawBehind {
                    drawCircle(color = color)
                }
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * هموارسازی داده‌های سرعت برای کاهش نوسانات شدید
 * استفاده از میانگین متحرک برای ایجاد نمودار صاف‌تر
 */
private fun smoothSpeedData(speedHistory: List<SpeedDataPoint>): List<SpeedDataPoint> {
    if (speedHistory.size <= 2) return speedHistory

    val smoothedData = mutableListOf<SpeedDataPoint>()
    val windowSize = 3 // اندازه پنجره برای میانگین متحرک

    speedHistory.forEachIndexed { index, point ->
        when (index) {
            0 -> {
                // نقطه اول: میانگین خودش و نقطه بعدی
                val avgSpeed = (point.speed + speedHistory[1].speed) / 2.0
                smoothedData.add(point.copy(speed = avgSpeed))
            }
            speedHistory.size - 1 -> {
                // نقطه آخر: میانگین خودش و نقطه قبلی
                val avgSpeed = (point.speed + speedHistory[index - 1].speed) / 2.0
                smoothedData.add(point.copy(speed = avgSpeed))
            }
            else -> {
                // نقاط میانی: میانگین متحرک
                val startIndex = maxOf(0, index - windowSize / 2)
                val endIndex = minOf(speedHistory.size - 1, index + windowSize / 2)
                val windowData = speedHistory.subList(startIndex, endIndex + 1)
                val avgSpeed = windowData.map { it.speed }.average()
                smoothedData.add(point.copy(speed = avgSpeed))
            }
        }
    }

    return smoothedData
}
