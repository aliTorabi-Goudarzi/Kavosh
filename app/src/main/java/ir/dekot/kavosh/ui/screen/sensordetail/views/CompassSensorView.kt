package ir.dekot.kavosh.ui.screen.sensordetail.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.domain.sensor.SensorState
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow
import ir.dekot.kavosh.ui.screen.sensordetail.components.AdvancedCompass
import ir.dekot.kavosh.ui.screen.sensordetail.components.AngleIndicator
import ir.dekot.kavosh.ui.screen.sensordetail.components.ArtificialHorizon

/**
 * کامپوزبل اصلی برای نمایش صفحه قطب‌نما.
 * این View از کامپوزبل‌های کوچک‌تر تشکیل شده است.
 * @param compassState وضعیت کامل قطب‌نما شامل زوایا و داده‌های خام.
 */
@Composable
fun CompassSensorView(compassState: SensorState.CompassState) {
    val bearing = Math.toDegrees(compassState.orientationAngles[0].toDouble()).toFloat()
    val animatedBearing by animateFloatAsState(targetValue = -bearing, label = "bearingAnim")
    val pitch = Math.toDegrees(compassState.orientationAngles[1].toDouble()).toFloat()
    val roll = Math.toDegrees(compassState.orientationAngles[2].toDouble()).toFloat()

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AdvancedCompass(rotationDegrees = animatedBearing)
        Spacer(modifier = Modifier.height(24.dp))
        ArtificialHorizon(pitch = pitch, roll = roll)
        Spacer(modifier = Modifier.height(24.dp))
        InfoCard(title = stringResource(R.string.orientation_angles)) {
            AngleIndicator(label = stringResource(R.string.rotation_pitch), angle = pitch)
            AngleIndicator(label = stringResource(R.string.rotation_roll), angle = roll)
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard(title = stringResource(R.string.raw_sensor_data)) {
            Text(stringResource(R.string.uncalibrated_accelerometer_title), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            InfoRow(label = "X", value = "%.2f".format(compassState.accelerometerData.getOrNull(0) ?: 0f))
            InfoRow(label = "Y", value = "%.2f".format(compassState.accelerometerData.getOrNull(1) ?: 0f))
            InfoRow(label = "Z", value = "%.2f".format(compassState.accelerometerData.getOrNull(2) ?: 0f))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.uncalibrated_magnetometer_title), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            InfoRow(label = "X", value = "%.2f".format(compassState.magnetometerData.getOrNull(0) ?: 0f))
            InfoRow(label = "Y", value = "%.2f".format(compassState.magnetometerData.getOrNull(1) ?: 0f))
            InfoRow(label = "Z", value = "%.2f".format(compassState.magnetometerData.getOrNull(2) ?: 0f))
        }
    }
}