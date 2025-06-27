package ir.dekot.kavosh.ui.screen.sensordetail.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.domain.sensor.SensorState
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow
import ir.dekot.kavosh.ui.screen.sensordetail.components.LiveRotationChart
import ir.dekot.kavosh.ui.screen.sensordetail.components.RotatingCube

/**
 * کامپوزبل اختصاصی برای نمایش داده‌های سنسور وکتور چرخش (Rotation Vector).
 * @param rotationState وضعیت کامل سنسور شامل تاریخچه مقادیر برای نمودار.
 */
@Composable
fun RotationVectorSensorView(rotationState: SensorState.RotationVectorState) {
    val lastRotationVector = rotationState.history.lastOrNull() ?: FloatArray(4)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.rotation_vector_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        RotatingCube(rotationVector = lastRotationVector)
        Spacer(modifier = Modifier.height(32.dp))

        InfoCard(title = stringResource(R.string.live_values_title)) {
            val x = lastRotationVector.getOrNull(0) ?: 0f
            val y = lastRotationVector.getOrNull(1) ?: 0f
            val z = lastRotationVector.getOrNull(2) ?: 0f
            val w = lastRotationVector.getOrNull(3) ?: 0f // Scalar/Cosine component
            InfoRow(label = "X · sin(θ/2)", value = "%.3f".format(x))
            InfoRow(label = "Y · sin(θ/2)", value = "%.3f".format(y))
            InfoRow(label = "Z · sin(θ/2)", value = "%.3f".format(z))
            InfoRow(label = "cos(θ/2)", value = "%.3f".format(w))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LiveRotationChart(history = rotationState.history)
    }
}