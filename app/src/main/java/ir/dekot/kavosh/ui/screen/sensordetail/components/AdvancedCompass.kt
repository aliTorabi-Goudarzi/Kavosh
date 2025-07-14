package ir.dekot.kavosh.ui.screen.sensordetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.dekot.kavosh.R

@Composable
fun AdvancedCompass(rotationDegrees: Float) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = colorScheme.primary
    val onSurfaceColor = colorScheme.onSurface
    val textStyle = TextStyle(color = onSurfaceColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)

    // Get compass direction strings
    val northText = stringResource(R.string.compass_north)
    val eastText = stringResource(R.string.compass_east)
    val southText = stringResource(R.string.compass_south)
    val westText = stringResource(R.string.compass_west)

    Box(modifier = Modifier.padding(16.dp)) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val radius = size.minDimension / 2
            val center = this.center

            rotate(degrees = rotationDegrees, pivot = center) {
                for (i in 0 until 360 step 15) {
                    val lineLength = if (i % 45 == 0) 25f else 15f
                    rotate(degrees = i.toFloat(), pivot = center) {
                        drawLine(color = onSurfaceColor, start = Offset(center.x, 0f), end = Offset(center.x, lineLength), strokeWidth = 3f)
                    }
                }
                drawText(textMeasurer, northText, Offset(center.x - textMeasurer.measure(northText).size.width / 2, 35f), style = textStyle.copy(color = primaryColor))
                drawText(textMeasurer, eastText, Offset(size.width - 45f, center.y - 15), style = textStyle)
                drawText(textMeasurer, southText, Offset(center.x - textMeasurer.measure(southText).size.width / 2, size.height - 55f), style = textStyle)
                drawText(textMeasurer, westText, Offset(35f, center.y - 15), style = textStyle)
            }

            val needlePath = Path().apply {
                moveTo(center.x, 0f)
                lineTo(center.x - 20f, 50f)
                lineTo(center.x + 20f, 50f)
                close()
            }
            drawPath(path = needlePath, color = primaryColor)
            drawCircle(color = primaryColor, radius = 10f, center = center)
        }
    }
}