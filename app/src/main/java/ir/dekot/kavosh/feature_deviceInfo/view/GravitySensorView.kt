package ir.dekot.kavosh.feature_deviceInfo.view

import android.hardware.SensorManager
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
import ir.dekot.kavosh.feature_deviceInfo.model.SensorState
import ir.dekot.kavosh.feature_deviceInfo.view.components.AxisData

/**
 * کامپوزبل اختصاصی برای نمایش داده‌های سنسور گرانش (Gravity).
 * @param vector مقادیر محورهای X, Y, Z از نوع VectorValue.
 */
@Composable
fun GravitySensorView(vector: SensorState.VectorValue) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.gravity_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.accelerometer_unit),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        // maxValue برای گرانش، ثابت گرانش زمین است
        AxisData(label = "X", value = vector.x, maxValue = SensorManager.GRAVITY_EARTH)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Y", value = vector.y, maxValue = SensorManager.GRAVITY_EARTH)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Z", value = vector.z, maxValue = SensorManager.GRAVITY_EARTH)
    }
}