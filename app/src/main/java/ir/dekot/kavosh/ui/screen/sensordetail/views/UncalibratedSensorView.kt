package ir.dekot.kavosh.ui.screen.sensordetail.views

import androidx.annotation.StringRes
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
import ir.dekot.kavosh.ui.screen.sensordetail.components.AxisData

/**
 * کامپوزبل عمومی برای نمایش داده‌های سنسورهای کالیبره نشده.
 * @param titleRes شناسه منبع رشته برای عنوان سنسور.
 * @param unitRes شناسه منبع رشته برای واحد اندازه‌گیری.
 * @param vector مقادیر خام و بایاس سنسور.
 */
@Composable
fun UncalibratedSensorView(
    @StringRes titleRes: Int,
    @StringRes unitRes: Int,
    vector: SensorState.UncalibratedVectorValue
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(unitRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // بخش داده‌های خام
        Text(text = stringResource(R.string.uncalibrated_raw_data), style = MaterialTheme.typography.titleMedium)
        AxisData(label = "X", value = vector.x, maxValue = 20f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Y", value = vector.y, maxValue = 20f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Z", value = vector.z, maxValue = 20f)

        Spacer(modifier = Modifier.height(24.dp))

        // بخش خطای تخمینی (Bias)
        Text(text = stringResource(R.string.uncalibrated_bias), style = MaterialTheme.typography.titleMedium)
        AxisData(label = "X Bias", value = vector.xBias, maxValue = 5f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Y Bias", value = vector.yBias, maxValue = 5f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Z Bias", value = vector.zBias, maxValue = 5f)
    }
}