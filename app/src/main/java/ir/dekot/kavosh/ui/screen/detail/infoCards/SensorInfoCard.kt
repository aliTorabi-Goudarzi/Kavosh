package ir.dekot.kavosh.ui.screen.detail.infoCards

import android.hardware.Sensor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

// یک لیست از سنسورهایی که قابلیت تست دارند
val testableSensors = listOf(
    Sensor.TYPE_LIGHT,
    Sensor.TYPE_ACCELEROMETER,
    Sensor.TYPE_MAGNETIC_FIELD
)

@Composable
fun SensorInfoCard(
    info: SensorInfo,
    onTestClick: (Int) -> Unit // یک Callback برای کلیک روی دکمه تست
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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

            // *** دکمه تست به صورت شرطی اضافه شد ***
            if (info.type in testableSensors) {
                Button(onClick = { onTestClick(info.type) }) {
                    Text(text = stringResource(R.string.sensor_test_button))
                }
            }
        }
    }
}