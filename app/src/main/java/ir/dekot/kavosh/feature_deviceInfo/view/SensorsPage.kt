package ir.dekot.kavosh.feature_deviceInfo.view

import android.hardware.Sensor
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R

import ir.dekot.kavosh.core.ui.shared_components.EmptyStateMessage
import ir.dekot.kavosh.core.navigation.NavigationViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.SensorInfo
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.SensorInfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.testableSensors
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsPage(deviceInfoViewModel: DeviceInfoViewModel, navigationViewModel: NavigationViewModel) {
    val deviceInfo by deviceInfoViewModel.deviceInfo.collectAsState()
    var selectedSensor by remember { mutableStateOf<SensorInfo?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    if (deviceInfo.sensors.isEmpty()) {
        EmptyStateMessage(stringResource(R.string.no_sensors_found))
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            deviceInfo.sensors.forEach { sensor ->
                SensorInfoCard(
                    info = sensor,
                    onClick = {
                        selectedSensor = sensor
                    }
                )
            }
        }
    }

    // BottomSheet برای نمایش جزئیات سنسور
    selectedSensor?.let { sensor ->
        ModalBottomSheet(
            onDismissRequest = { selectedSensor = null },
            sheetState = bottomSheetState
        ) {
            SensorDetailBottomSheet(
                sensor = sensor,
                onTestClick = { sensorType ->
                    selectedSensor = null
                    navigationViewModel.navigateToSensorDetail(sensorType)
                },
                onDismiss = { selectedSensor = null }
            )
        }
    }
}

/**
 * BottomSheet برای نمایش جزئیات سنسور
 */
@Composable
private fun SensorDetailBottomSheet(
    sensor: SensorInfo,
    onTestClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp,bottom = 24.dp)
    ) {
        // هدر
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.sensor_details_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

//            IconButton(onClick = onDismiss) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = stringResource(R.string.back)
//                )
//            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // اطلاعات سنسور
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // نام سنسور
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sensor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // سازنده
                Row {
                    Text(
                        text = stringResource(R.string.sensor_vendor, ""),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sensor.vendor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // نوع سنسور
                Row {
                    Text(
                        text = "نوع: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = getSensorTypeName(sensor.type),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // وضعیت تست
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قابلیت تست: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (sensor.type in testableSensors) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.sensor_testable),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.sensor_testable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = stringResource(R.string.sensor_not_testable),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.sensor_not_testable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // دکمه تست (فقط اگر سنسور قابل تست باشد)
        if (sensor.type in testableSensors) {
            Button(
                onClick = { onTestClick(sensor.type) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.sensor_start_test))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * دریافت نام فارسی نوع سنسور
 */
private fun getSensorTypeName(type: Int): String {
    return when (type) {
        Sensor.TYPE_ACCELEROMETER -> "شتاب‌سنج"
        Sensor.TYPE_MAGNETIC_FIELD -> "مغناطیس‌سنج"
        Sensor.TYPE_GYROSCOPE -> "ژیروسکوپ"
        Sensor.TYPE_LIGHT -> "نور"
        Sensor.TYPE_PRESSURE -> "فشار"
        Sensor.TYPE_PROXIMITY -> "مجاورت"
        Sensor.TYPE_GRAVITY -> "گرانش"
        Sensor.TYPE_LINEAR_ACCELERATION -> "شتاب خطی"
        Sensor.TYPE_ROTATION_VECTOR -> "بردار چرخش"
        Sensor.TYPE_RELATIVE_HUMIDITY -> "رطوبت نسبی"
        Sensor.TYPE_AMBIENT_TEMPERATURE -> "دمای محیط"
        Sensor.TYPE_STEP_COUNTER -> "شمارنده قدم"
        Sensor.TYPE_STEP_DETECTOR -> "تشخیص قدم"
        Sensor.TYPE_SIGNIFICANT_MOTION -> "حرکت قابل توجه"
        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> "شتاب‌سنج کالیبره نشده"
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "ژیروسکوپ کالیبره نشده"
        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "مغناطیس‌سنج کالیبره نشده"
        else -> "نامشخص (${type})"
    }
}