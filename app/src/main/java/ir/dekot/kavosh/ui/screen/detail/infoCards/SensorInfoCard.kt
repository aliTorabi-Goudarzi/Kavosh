package ir.dekot.kavosh.ui.screen.detail.infoCards

import android.hardware.Sensor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.SensorInfo

// *** سنسور مجاورت به لیست اضافه شد ***
// *** لیست نهایی سنسورهای قابل تست ***
val testableSensors = listOf(
    Sensor.TYPE_LIGHT,
    Sensor.TYPE_ACCELEROMETER,
    Sensor.TYPE_MAGNETIC_FIELD,
    Sensor.TYPE_GYROSCOPE,
    Sensor.TYPE_PROXIMITY,
    Sensor.TYPE_PRESSURE,
    Sensor.TYPE_GRAVITY,
    Sensor.TYPE_LINEAR_ACCELERATION,
    Sensor.TYPE_STEP_COUNTER,
    Sensor.TYPE_AMBIENT_TEMPERATURE,
    Sensor.TYPE_RELATIVE_HUMIDITY,
    Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
    Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
    Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
    Sensor.TYPE_STEP_DETECTOR,
    // سنسورهای جدید
    Sensor.TYPE_SIGNIFICANT_MOTION,
    // سنسور جدید
    Sensor.TYPE_ROTATION_VECTOR
)
@Composable
fun SensorInfoCard(
    info: SensorInfo,
    onClick: () -> Unit // کلیک روی کل کارت
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ستون برای نام و سازنده سنسور
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.sensor_vendor, info.vendor),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // آیکون نشان‌دهنده قابلیت تست
            if (info.type in testableSensors) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = "قابل تست",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}