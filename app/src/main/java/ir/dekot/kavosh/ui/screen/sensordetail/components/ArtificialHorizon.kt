package ir.dekot.kavosh.ui.screen.sensordetail.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * *** کامپوننت بازنویسی شده و نهایی برای نمایش افق مصنوعی ***
 */
/**
 * *** کامپوننت بازنویسی شده افق مصنوعی با استایل شیشه‌ای ***
 */

@Composable
fun ArtificialHorizon(pitch: Float, roll: Float) {
    val animatedPitch by animateFloatAsState(targetValue = pitch, label = "pitchAnim")
    val animatedRoll by animateFloatAsState(targetValue = -roll, label = "rollAnim")

    val skyColor = Color(0xFF42A5F5)
    val groundColor = Color(0xFF6D4C41)
    val horizonLineColor = Color.White
    val markingsColor = Color.White.copy(alpha = 0.8f)
    val planeColor = colorScheme.primary
    // *** متغیر فراموش شده در اینجا تعریف شد ***
    val onSurfaceColor = colorScheme.onSurface

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 12.sp.value
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
        }
    }

    Box(
        modifier = Modifier
            .size(250.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = this.center

            clipPath(Path().apply { addOval(Rect(center, radius)) }) {
                rotate(degrees = animatedRoll, pivot = center) {
                    val pitchTranslation = animatedPitch * (radius / 45f)

                    drawRect(color = skyColor, size = size.copy(height = center.y + pitchTranslation + radius), topLeft = Offset(0f, -radius))
                    drawRect(color = groundColor, topLeft = Offset(0f, center.y + pitchTranslation))
                    drawLine(color = horizonLineColor, start = Offset(0f, center.y + pitchTranslation), end = Offset(size.width, center.y + pitchTranslation), strokeWidth = 5f)

                    for (i in -90..90 step 10) {
                        if (i == 0) continue
                        val yPos = center.y + pitchTranslation - (i * (radius / 45f))
                        val lineLength = if (i % 30 == 0) 100.dp.toPx() else 50.dp.toPx()
                        drawLine(color = markingsColor, start = Offset(center.x - lineLength / 2, yPos), end = Offset(center.x + lineLength / 2, yPos), strokeWidth = 2f)

                        if (i % 30 == 0) {
                            drawContext.canvas.nativeCanvas.drawText(abs(i).toString(), center.x - lineLength/2 - 20.dp.toPx() , yPos + 5.dp.toPx(), textPaint)
                            drawContext.canvas.nativeCanvas.drawText(abs(i).toString(), center.x + lineLength/2 + 20.dp.toPx() , yPos + 5.dp.toPx(), textPaint)
                        }
                    }
                }
            }

            // *** استفاده از متغیر صحیح شده ***
            drawCircle(color = onSurfaceColor, radius = radius, style = Stroke(width = 8f), center = center)

            val planeWingWidth = 100.dp.toPx()
            val planeBodyWidth = 40.dp.toPx()
            val planeStrokeWidth = 8f

            drawLine(planeColor, start = Offset(center.x - planeWingWidth / 2, center.y), end = Offset(center.x + planeWingWidth / 2, center.y), strokeWidth = planeStrokeWidth)
            drawLine(planeColor, start = Offset(center.x - planeWingWidth / 2, center.y), end = Offset(center.x - planeWingWidth / 2, center.y - 10f), strokeWidth = planeStrokeWidth / 2)
            drawLine(planeColor, start = Offset(center.x + planeWingWidth / 2, center.y), end = Offset(center.x + planeWingWidth / 2, center.y - 10f), strokeWidth = planeStrokeWidth / 2)
            drawLine(planeColor, start = Offset(center.x - planeBodyWidth / 2, center.y + 15f), end = Offset(center.x + planeBodyWidth / 2, center.y + 15f), strokeWidth = planeStrokeWidth)
        }
    }
}