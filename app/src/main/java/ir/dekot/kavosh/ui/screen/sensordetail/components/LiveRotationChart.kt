package ir.dekot.kavosh.ui.screen.sensordetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.composables.InfoCard

/**
 * *** کامپوننت جدید برای رسم نمودار زنده ***
 */
/**
 * *** کامپوننت بازنویسی شده برای نمودار پیشرفته ***
 */
/**
 * *** کامپوننت بازنویسی شده برای نمودار پیشرفته ***
 */
@Composable
fun LiveRotationChart(history: List<FloatArray>) {
    val colors = listOf(Color.Red, Color.Green, Color(0xFF37A6FF))
    val labels = listOf("X", "Y", "Z")

    val axisColor = colorScheme.onSurface.copy(alpha = 0.5f)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = axisColor, fontSize = 10.sp)

    InfoCard(title = stringResource(R.string.live_chart_title)) {
        Column(Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)) {
                if (history.size < 2) return@Canvas

                val stepX = size.width / (history.size - 1).coerceAtLeast(1)

                // رسم محورها و شبکه نقطه‌چین
                val gridPath = Path()
                val yGridSteps = 4
                for (i in 0..yGridSteps) {
                    val y = i * (size.height / yGridSteps)
                    gridPath.moveTo(0f, y)
                    gridPath.lineTo(size.width, y)
                }
                drawPath(gridPath, color = axisColor, style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))

                drawLine(axisColor, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 2f)

                // رسم نمودارها
                for (axisIndex in 0..2) {
                    val path = Path()
                    val glassPath = Path()

                    history.forEachIndexed { index, data ->
                        val x = index * stepX
                        val y = (1 - ((data.getOrNull(axisIndex) ?: 0f) + 1) / 2) * size.height

                        if (index == 0) {
                            path.moveTo(x, y)
                            glassPath.moveTo(x, size.height)
                            glassPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            glassPath.lineTo(x, y)
                        }
                    }

                    glassPath.lineTo((history.size - 1) * stepX, size.height)
                    glassPath.close()

                    drawPath(glassPath, brush = Brush.verticalGradient(
                        colors = listOf(colors[axisIndex].copy(alpha = 0.3f), Color.Transparent)
                    ))

                    // رسم خط نئونی
                    drawPath(path, color = colors[axisIndex].copy(alpha = 0.4f), style = Stroke(width = 10f))
                    drawPath(path, color = colors[axisIndex], style = Stroke(width = 4f))

                    // *** بخش جدید: رسم لیبل متحرک و نئونی ***
                    val lastX = (history.size - 1) * stepX
                    val lastY = (1 - ((history.last().getOrNull(axisIndex) ?: 0f) + 1) / 2) * size.height

                    // رسم نقطه نئونی
                    drawCircle(colors[axisIndex].copy(alpha = 0.5f), radius = 20f, center = Offset(lastX, lastY))
                    drawCircle(colors[axisIndex], radius = 9f, center = Offset(lastX, lastY))

//                    // رسم متن نئونی
//                    val labelText = labels[axisIndex]
//                    val textLayoutResult = textMeasurer.measure(labelText, style = textStyle.copy(color = colors[axisIndex]))
//                    val textOffset = Offset(lastX - textLayoutResult.size.width/2 , lastY - 35.dp.toPx())

//                    drawText(textMeasurer, labelText, textOffset, style = textStyle.copy(color = colors[axisIndex].copy(alpha = 0.5f)))
//                    drawText(textMeasurer, labelText, textOffset, style = textStyle.copy(color = Color.White))
                }

                // رسم برچسب‌های محور Y
                drawText(textMeasurer, "1.0", Offset(5.dp.toPx(), -5.dp.toPx()), style = textStyle)
                drawText(textMeasurer, "0.0", Offset(5.dp.toPx(), center.y - 10.sp.toPx()), style = textStyle)
                drawText(textMeasurer, "-1.0", Offset(5.dp.toPx(), size.height - 20.sp.toPx()), style = textStyle)
            }
            // *** راهنمای نمودار با نقاط نئونی ***
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                labels.forEachIndexed { index, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // استفاده از Canvas برای رسم نقطه نئونی
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawNeonDot(center = this.center, color = colors[index], radius = 9f, glowRadius = 20f)
                        }
                        Text(text = label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }}}

/**
 * *** تابع کمکی جدید برای رسم یک نقطه نئونی ***
 */
fun DrawScope.drawNeonDot(center: Offset, color: Color, radius: Float = 6f, glowRadius: Float = 12f) {
    // رسم هاله درخشان
    drawCircle(color.copy(alpha = 0.5f), radius = glowRadius, center = center)
    // رسم نقطه اصلی
    drawCircle(color, radius = radius, center = center)
}