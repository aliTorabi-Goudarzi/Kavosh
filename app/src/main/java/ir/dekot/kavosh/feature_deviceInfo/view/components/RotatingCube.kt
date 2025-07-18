package ir.dekot.kavosh.feature_deviceInfo.view.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

/**
 * کامپوزبل برای نمایش یک مکعب سه‌بعدی چرخان با افکت نئونی و شیشه‌ای.
 */
@Composable
fun RotatingCube(rotationVector: FloatArray) {
    val neonColor = MaterialTheme.colorScheme.primary
    val glassColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

    Canvas(modifier = Modifier.size(250.dp)) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        val vertices = arrayOf(
            floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f),
            floatArrayOf(1f, 1f, -1f), floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(-1f, -1f, 1f), floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)
        )

        val projectedVertices = Array(8) { Offset.Zero }
        val transformedZ = FloatArray(8)

        vertices.forEachIndexed { i, vertex ->
            val rotated = multiplyMatrix(rotationMatrix, vertex)
            transformedZ[i] = rotated[2]
            projectedVertices[i] = project(rotated, size.width, size.height, scale = 150f)
        }

        val faces = listOf(
            listOf(0, 1, 2, 3), listOf(4, 5, 6, 7), listOf(0, 4, 7, 3),
            listOf(1, 5, 6, 2), listOf(3, 2, 6, 7), listOf(0, 1, 5, 4)
        )

        val sortedFaces = faces.map { face ->
            val avgZ = face.sumOf { transformedZ[it].toDouble() } / face.size
            Pair(face, avgZ)
        }.sortedBy { it.second }

        sortedFaces.forEach { (face, _) ->
            val path = Path().apply {
                moveTo(projectedVertices[face[0]].x, projectedVertices[face[0]].y)
                lineTo(projectedVertices[face[1]].x, projectedVertices[face[1]].y)
                lineTo(projectedVertices[face[2]].x, projectedVertices[face[2]].y)
                lineTo(projectedVertices[face[3]].x, projectedVertices[face[3]].y)
                close()
            }
            drawPath(path, color = glassColor)
        }

        for (i in 0 until 4) {
            drawNeonEdge(projectedVertices[i], projectedVertices[(i + 1) % 4], neonColor)
            drawNeonEdge(projectedVertices[i + 4], projectedVertices[((i + 1) % 4) + 4], neonColor)
            drawNeonEdge(projectedVertices[i], projectedVertices[i + 4], neonColor)
        }
    }
}

// --- توابع کمکی ---

private fun multiplyMatrix(matrix: FloatArray, vector: FloatArray): FloatArray {
    val result = FloatArray(3)
    result[0] = matrix[0] * vector[0] + matrix[1] * vector[1] + matrix[2] * vector[2]
    result[1] = matrix[3] * vector[0] + matrix[4] * vector[1] + matrix[5] * vector[2]
    result[2] = matrix[6] * vector[0] + matrix[7] * vector[1] + matrix[8] * vector[2]
    return result
}

private fun project(point: FloatArray, width: Float, height: Float, @Suppress("SameParameterValue") scale: Float): Offset {
    val x = point[0] * scale + width / 2
    val y = point[1] * scale + height / 2
    return Offset(x, y)
}

private fun DrawScope.drawNeonEdge(start: Offset, end: Offset, color: Color) {
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 6f
        maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
    }

    drawIntoCanvas {
        paint.color = color.copy(alpha = 0.5f).toArgb()
        it.nativeCanvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }

    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 4f
    )
}