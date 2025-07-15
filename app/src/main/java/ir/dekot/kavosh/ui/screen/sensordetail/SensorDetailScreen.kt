package ir.dekot.kavosh.ui.screen.sensordetail

import android.hardware.Sensor
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.domain.sensor.SensorState
import ir.dekot.kavosh.ui.screen.sensordetail.views.AccelerometerView
import ir.dekot.kavosh.ui.screen.sensordetail.views.AmbientTemperatureSensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.BarometerSensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.CompassSensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.GravitySensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.GyroscopeView
import ir.dekot.kavosh.ui.screen.sensordetail.views.LightSensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.LinearAccelerationSensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.ProximitySensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.RelativeHumiditySensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.RotationVectorSensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.StepCounterView
import ir.dekot.kavosh.ui.screen.sensordetail.views.StepDetectorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.TriggerSensorView
import ir.dekot.kavosh.ui.screen.sensordetail.views.UncalibratedSensorView
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDetailScreen(
    viewModel: DeviceInfoViewModel,
    sensorType: Int,
    onBackClick: () -> Unit
) {
    val sensorName = viewModel.deviceInfo.collectAsState().value.sensors.find { it.type == sensorType }?.name
        ?: stringResource(R.string.unknown_sensor)

    val sensorState by viewModel.sensorState.collectAsState()

    LaunchedEffect(sensorType) {
        viewModel.registerSensorListener(sensorType)
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.unregisterSensorListener()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sensorName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            val currentState = sensorState

            // ** مدیریت هوشمند حالت لودینگ **
            // فقط زمانی لودینگ نشان بده که هنوز هیچ داده‌ای دریافت نشده
            when (currentState) {
                is SensorState.Loading -> {
                    CircularProgressIndicator()
                }

                is SensorState.NotAvailable -> {
                    Text(text = currentState.message)
                }

                else -> {
                    // زمانی که داده دریافت شد یا وضعیت اولیه "منتظر" است، UI را نمایش بده
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (sensorType) {
                            Sensor.TYPE_LIGHT -> if (currentState is SensorState.SingleValue) LightSensorView(currentState.value)
                            Sensor.TYPE_ACCELEROMETER -> if (currentState is SensorState.VectorValue) AccelerometerView(currentState)
                            Sensor.TYPE_MAGNETIC_FIELD -> if (currentState is SensorState.CompassState) CompassSensorView(currentState)
                            Sensor.TYPE_GYROSCOPE -> if (currentState is SensorState.VectorValue) GyroscopeView(currentState)
                            Sensor.TYPE_PROXIMITY -> if (currentState is SensorState.SingleValue) ProximitySensorView(currentState.value)
                            Sensor.TYPE_PRESSURE -> if (currentState is SensorState.SingleValue) BarometerSensorView(currentState.value)
                            Sensor.TYPE_GRAVITY -> if (currentState is SensorState.VectorValue) GravitySensorView(currentState)
                            Sensor.TYPE_LINEAR_ACCELERATION -> if (currentState is SensorState.VectorValue) LinearAccelerationSensorView(currentState)
                            Sensor.TYPE_AMBIENT_TEMPERATURE -> if (currentState is SensorState.SingleValue) AmbientTemperatureSensorView(currentState.value)
                            Sensor.TYPE_RELATIVE_HUMIDITY -> if (currentState is SensorState.SingleValue) RelativeHumiditySensorView(currentState.value)
                            Sensor.TYPE_ROTATION_VECTOR -> if (currentState is SensorState.RotationVectorState) RotationVectorSensorView(currentState)

                            Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> if (currentState is SensorState.UncalibratedVectorValue) UncalibratedSensorView(R.string.uncalibrated_accelerometer_title, R.string.accelerometer_unit, currentState)
                            Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> if (currentState is SensorState.UncalibratedVectorValue) UncalibratedSensorView(R.string.uncalibrated_gyroscope_title, R.string.gyroscope_unit, currentState)
                            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> if (currentState is SensorState.UncalibratedVectorValue) UncalibratedSensorView(R.string.uncalibrated_magnetometer_title, R.string.magnetometer_unit, currentState)

                            // ** مدیریت صحیح سنسورهای خاص **
                            Sensor.TYPE_STEP_COUNTER -> {
                                val steps = if (currentState is SensorState.SingleValue) currentState.value else 0f
                                StepCounterView(steps)
                            }

                            Sensor.TYPE_STEP_DETECTOR -> {
                                val count = if (currentState is SensorState.TriggerEvent) currentState.eventCount else 0
                                StepDetectorView(count)
                            }

                            Sensor.TYPE_SIGNIFICANT_MOTION -> {
                                val count = if (currentState is SensorState.TriggerEvent) currentState.eventCount else 0
                                TriggerSensorView(titleRes = R.string.significant_motion_title, eventCount = count, icon = Icons.AutoMirrored.Filled.DirectionsRun)
                            }

                            else -> {
                                Text(text = stringResource(R.string.sensor_no_live_view))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}