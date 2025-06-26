package ir.dekot.kavosh.ui.screen.sensordetail

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import kotlinx.coroutines.delay
import kotlin.math.ln

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (sensorType) {
                Sensor.TYPE_LIGHT -> LightSensorContent(viewModel)
                Sensor.TYPE_ACCELEROMETER -> AccelerometerSensorContent(viewModel)
                // *** کیس جدید برای قطب‌نما ***
                Sensor.TYPE_MAGNETIC_FIELD -> CompassSensorContent(viewModel)
                Sensor.TYPE_GYROSCOPE -> GyroscopeSensorContent(viewModel)
                Sensor.TYPE_PROXIMITY -> ProximitySensorContent(viewModel)
                Sensor.TYPE_PRESSURE -> BarometerSensorContent(viewModel)
                Sensor.TYPE_GRAVITY -> GravitySensorContent(viewModel)
                Sensor.TYPE_LINEAR_ACCELERATION -> LinearAccelerationSensorContent(viewModel)
                Sensor.TYPE_STEP_COUNTER -> StepCounterSensorContent(viewModel)
                Sensor.TYPE_AMBIENT_TEMPERATURE -> AmbientTemperatureSensorContent(viewModel)
                Sensor.TYPE_RELATIVE_HUMIDITY -> RelativeHumiditySensorContent(viewModel)
                // *** کیس‌های جدید برای سنسورهای کالیبره نشده ***
                Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> UncalibratedSensorContent(
                    viewModel = viewModel,
                    title = stringResource(R.string.uncalibrated_accelerometer_title),
                    unit = stringResource(R.string.accelerometer_unit)
                )
                Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> UncalibratedSensorContent(
                    viewModel = viewModel,
                    title = stringResource(R.string.uncalibrated_gyroscope_title),
                    unit = stringResource(R.string.gyroscope_unit)
                )
                Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> UncalibratedSensorContent(
                    viewModel = viewModel,
                    title = stringResource(R.string.uncalibrated_magnetometer_title),
                    unit = stringResource(R.string.magnetometer_unit)
                )
                // *** کیس‌های جدید برای سنسورهای حرکتی ***
                // *** تغییر کلیدی: پاس دادن viewModel به توابع ***
                Sensor.TYPE_STEP_DETECTOR -> StepDetectorContent(viewModel)
                Sensor.TYPE_ROTATION_VECTOR -> RotationVectorSensorContent(viewModel)
                // *** کیس‌های جدید برای سنسورهای حرکتی ***
                Sensor.TYPE_SIGNIFICANT_MOTION -> TriggerSensorContent(viewModel = viewModel, titleRes = R.string.significant_motion_title)
                else -> Text(text = stringResource(R.string.sensor_no_live_view))
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun LightSensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val lightValue = liveData.firstOrNull() ?: 0f

    val lightLevelFactor = if (lightValue > 0) (ln(lightValue.coerceAtLeast(1f)) / ln(40000f)).coerceIn(0f, 1f) else 0f

    val animatedSize by animateFloatAsState(targetValue = 100 + (200 * lightLevelFactor), label = "sizeAnim")
    val animatedColor by animateColorAsState(targetValue = Color.Yellow.copy(alpha = lightLevelFactor), label = "colorAnim")

    Box(
        modifier = Modifier
            .size(animatedSize.dp)
            .clip(CircleShape)
            .background(animatedColor),
        contentAlignment = Alignment.Center
    ) {}
    Spacer(modifier = Modifier.height(32.dp))
    Text(
        text = stringResource(R.string.unit_format_lux, lightValue),
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = stringResource(R.string.illuminance),
        style = MaterialTheme.typography.titleLarge
    )
}

/**
 * *** کامپوننت جدید و اختصاصی برای نمایش داده‌های شتاب‌سنج ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AccelerometerSensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val xValue = liveData.getOrNull(0) ?: 0f
    val yValue = liveData.getOrNull(1) ?: 0f
    val zValue = liveData.getOrNull(2) ?: 0f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.accelerometer_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.accelerometer_unit),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // نمایش داده‌های هر محور به همراه نوار پیشرفت
        AxisData(label = "X", value = xValue, maxValue = SensorManager.GRAVITY_EARTH * 2)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Y", value = yValue, maxValue = SensorManager.GRAVITY_EARTH * 2)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Z", value = zValue, maxValue = SensorManager.GRAVITY_EARTH * 2)
    }
}

@Composable
private fun AxisData(label: String, value: Float, maxValue: Float) {
    // نرمال‌سازی مقدار بین -1 و 1 برای نمایش در نوار پیشرفت
    val progress = (value / maxValue).coerceIn(-1f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "axis_progress_$label")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            Text(text = "%.2f".format(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))

        // نوار پیشرفتی که از وسط پر یا خالی می‌شود
        LinearProgressIndicator(
            progress = { (animatedProgress + 1) / 2 }, // تبدیل بازه -1..1 به 0..1
            modifier = Modifier.fillMaxWidth().height(12.dp)
        )
    }
}

/**
 * *** کامپوننت جدید و اختصاصی برای نمایش قطب‌نما ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CompassSensorContent(viewModel: DeviceInfoViewModel) {
    val bearing by viewModel.compassBearing.collectAsState()
    val animatedBearing by animateFloatAsState(targetValue = bearing, label = "bearingAnim")

    val bearingText = getBearingText(bearing)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%.1f° %s".format(bearing, bearingText),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.compass_bearing),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        // طراحی گرافیکی قطب‌نما
        Compass(bearing = animatedBearing)
    }
}

@Composable
fun Compass(bearing: Float, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier.size(300.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // رسم دایره بیرونی
            drawCircle(color = onSurfaceColor, radius = radius, center = center, style = Stroke(width = 4f))

            // رسم حروف جهت‌ها
            // این بخش برای سادگی حذف شده، اما قابل اضافه کردن است

            // چرخش کل Canvas برای جهت‌گیری سوزن
            rotate(degrees = -bearing, pivot = center) {
                // رسم سوزن قرمز (شمال)
                val northPath = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x - 20, center.y)
                    lineTo(center.x + 20, center.y)
                    close()
                }
                drawPath(path = northPath, color = Color.Red)

                // رسم سوزن خاکستری (جنوب)
                val southPath = Path().apply {
                    moveTo(center.x, center.y + radius)
                    lineTo(center.x - 20, center.y)
                    lineTo(center.x + 20, center.y)
                    close()
                }
                drawPath(path = southPath, color = Color.Gray)
            }
        }
    }
}

// تابع کمکی برای تبدیل درجه به جهت متنی
@Composable
private fun getBearingText(bearing: Float): String {
    return when (bearing) {
        in 337.5..360.0, in 0.0..22.5 -> stringResource(R.string.compass_n)
        in 22.5..67.5 -> stringResource(R.string.compass_ne)
        in 67.5..112.5 -> stringResource(R.string.compass_e)
        in 112.5..157.5 -> stringResource(R.string.compass_se)
        in 157.5..202.5 -> stringResource(R.string.compass_s)
        in 202.5..247.5 -> stringResource(R.string.compass_sw)
        in 247.5..292.5 -> stringResource(R.string.compass_w)
        in 292.5..337.5 -> stringResource(R.string.compass_nw)
        else -> ""
    }
}

/**
 * *** کامپوننت جدید و اختصاصی برای نمایش داده‌های ژیروسکوپ ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun GyroscopeSensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val xValue = liveData.getOrNull(0) ?: 0f
    val yValue = liveData.getOrNull(1) ?: 0f
    val zValue = liveData.getOrNull(2) ?: 0f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.gyroscope_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.gyroscope_unit),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        // استفاده مجدد از کامپوننت AxisData برای نمایش داده‌ها
        AxisData(label = "X", value = xValue, maxValue = 10f) // محدوده معمول ژیروسکوپ
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Y", value = yValue, maxValue = 10f)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Z", value = zValue, maxValue = 10f)
    }
}

/**
 * *** کامپوننت جدید و اختصاصی برای نمایش داده‌های سنسور مجاورت ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ProximitySensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    // مقدار این سنسور معمولاً فاصله به سانتی‌متر است. مقدار 0 به معنی بسیار نزدیک است.
    val distance = liveData.firstOrNull() ?: 5f
    val isNear = distance < 5f // اکثر سنسورها یک آستانه حدود 5 سانتی‌متر دارند

    val iconSize by animateDpAsState(targetValue = if (isNear) 150.dp else 100.dp, label = "iconSizeAnim")
    val text = if (isNear) stringResource(R.string.proximity_near) else stringResource(R.string.proximity_far)
    val icon = if (isNear) Icons.Default.PhoneInTalk else Icons.Default.PhoneIphone

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.unit_format_cm, distance),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/**
 * *** کامپوننت جدید و اختصاصی برای نمایش داده‌های فشارسنج ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun BarometerSensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    // مقدار این سنسور فشار هوا به هکتوپاسکال (hPa) یا میلی‌بار (mbar) است
    val pressureValue = liveData.firstOrNull() ?: 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_barometer), // یک آیکون مناسب نیاز داریم
            contentDescription = stringResource(R.string.barometer_title),
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.unit_format_hpa, pressureValue),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.barometer_title),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

/**
 * کامپوننت برای سنسور گرانش
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun GravitySensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val xValue = liveData.getOrNull(0) ?: 0f
    val yValue = liveData.getOrNull(1) ?: 0f
    val zValue = liveData.getOrNull(2) ?: 0f

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.gravity_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = stringResource(R.string.accelerometer_unit), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        AxisData(label = "X", value = xValue, maxValue = SensorManager.GRAVITY_EARTH)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Y", value = yValue, maxValue = SensorManager.GRAVITY_EARTH)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Z", value = zValue, maxValue = SensorManager.GRAVITY_EARTH)
    }
}

/**
 * کامپوننت برای سنسور شتاب خطی
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun LinearAccelerationSensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val xValue = liveData.getOrNull(0) ?: 0f
    val yValue = liveData.getOrNull(1) ?: 0f
    val zValue = liveData.getOrNull(2) ?: 0f

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.linear_acceleration_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = stringResource(R.string.accelerometer_unit), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        AxisData(label = "X", value = xValue, maxValue = 10f)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Y", value = yValue, maxValue = 10f)
        Spacer(modifier = Modifier.height(16.dp))
        AxisData(label = "Z", value = zValue, maxValue = 10f)
    }
}

/**
 * کامپوننت برای سنسور گام‌شمار
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun StepCounterSensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val steps = liveData.firstOrNull()?.toInt() ?: 0

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = stringResource(R.string.step_counter_title), modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = steps.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text(text = stringResource(R.string.step_counter_title), style = MaterialTheme.typography.titleLarge)
    }
}

/**
 * کامپوننت برای سنسور دمای محیط
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AmbientTemperatureSensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val tempValue = liveData.firstOrNull() ?: 0f

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.Thermostat, contentDescription = stringResource(R.string.ambient_temperature_title), modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(R.string.unit_format_celsius, tempValue), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text(text = stringResource(R.string.ambient_temperature_title), style = MaterialTheme.typography.titleLarge)
    }
}

/**
 * کامپوننت برای سنسور رطوبت نسبی
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun RelativeHumiditySensorContent(viewModel: DeviceInfoViewModel) {
    val liveData by viewModel.liveSensorData.collectAsState()
    val humidityValue = liveData.firstOrNull() ?: 0f

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = stringResource(R.string.relative_humidity_title), modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stringResource(R.string.unit_format_percent_relative, humidityValue), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text(text = stringResource(R.string.relative_humidity_title), style = MaterialTheme.typography.titleLarge)
    }
}

/**
 * *** کامپوننت جدید و عمومی برای نمایش داده‌های سنسورهای کالیبره نشده ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun UncalibratedSensorContent(viewModel: DeviceInfoViewModel, title: String, unit: String) {
    val liveData by viewModel.liveSensorData.collectAsState()
    // داده‌های خام
    val xValue = liveData.getOrNull(0) ?: 0f
    val yValue = liveData.getOrNull(1) ?: 0f
    val zValue = liveData.getOrNull(2) ?: 0f
    // خطای تخمینی (Bias)
    val xBias = liveData.getOrNull(3) ?: 0f
    val yBias = liveData.getOrNull(4) ?: 0f
    val zBias = liveData.getOrNull(5) ?: 0f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = unit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // بخش داده‌های خام
        Text(text = stringResource(R.string.uncalibrated_raw_data), style = MaterialTheme.typography.titleMedium)
        AxisData(label = "X", value = xValue, maxValue = 20f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Y", value = yValue, maxValue = 20f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Z", value = zValue, maxValue = 20f)

        Spacer(modifier = Modifier.height(24.dp))

        // بخش خطای تخمینی
        Text(text = stringResource(R.string.uncalibrated_bias), style = MaterialTheme.typography.titleMedium)
        AxisData(label = "X Bias", value = xBias, maxValue = 5f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Y Bias", value = yBias, maxValue = 5f)
        Spacer(modifier = Modifier.height(8.dp))
        AxisData(label = "Z Bias", value = zBias, maxValue = 5f)
    }
}

/**
 * *** کامپوننت جدید برای نمایش رویدادهای سنسور تشخیص گام ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun StepDetectorContent(viewModel: DeviceInfoViewModel) { // *** پارامتر جدید ***
    var stepEvents by remember { mutableIntStateOf(0) }
    val liveData by viewModel.liveSensorData.collectAsState()

    // این کد تضمین می‌کند که هر رویداد جدید فقط یک بار شمارش شود
    LaunchedEffect(liveData) {
        if (liveData.isNotEmpty()) {
            stepEvents++
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = stringResource(R.string.step_detector_title), modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = stepEvents.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text(text = stringResource(R.string.step_detector_title), style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.step_detector_description), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top=8.dp))
    }
}

/**
 * *** کامپوننت عمومی جدید برای نمایش رویدادهای سنسورهای تریگر ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TriggerSensorContent(
    viewModel: DeviceInfoViewModel,
    @StringRes titleRes: Int,
    icon: ImageVector = Icons.Default.CheckCircle // آیکون پیش‌فرض
) {
    var eventDetected by remember { mutableStateOf(false) }
    val liveData by viewModel.liveSensorData.collectAsState()

    LaunchedEffect(liveData) {
        if (liveData.isNotEmpty()) {
            eventDetected = true
            delay(2000) // پیام برای ۲ ثانیه نمایش داده می‌شود
            eventDetected = false
        }
    }

    val textToShow = if (eventDetected) stringResource(R.string.trigger_sensor_detected) else stringResource(R.string.trigger_sensor_waiting)
    val color by animateColorAsState(targetValue = if (eventDetected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, label = "triggerColorAnim")
    val iconToShow = if (eventDetected) Icons.Default.CheckCircle else icon

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = iconToShow, contentDescription = null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = textToShow, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * *** کامپوننت بازنویسی شده برای نمایش مکعب سه‌بعدی ***
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun RotationVectorSensorContent(viewModel: DeviceInfoViewModel) {
    val rotationVector by viewModel.rotationVectorData.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.rotation_vector_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        // نمایش مکعب سه‌بعدی
        RotatingCube(rotationVector = rotationVector)
    }
}

@Composable
fun RotatingCube(rotationVector: FloatArray) {
    val cubeColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.size(300.dp)) {
        // ماتریس چرخش را از وکتور چرخش به دست می‌آوریم
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        // تعریف رئوس یک مکعب واحد
        val points = arrayOf(
            floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f),
            floatArrayOf(1f, 1f, -1f), floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(-1f, -1f, 1f), floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)
        )

        val projectedPoints = Array(8) { Offset.Zero }

        // چرخش و تصویر کردن رئوس روی صفحه دو بعدی
        points.forEachIndexed { i, point ->
            val rotated = multiplyMatrix(rotationMatrix, point)
            projectedPoints[i] = project(rotated, size.width, size.height)
        }

        // رسم خطوط مکعب
        drawCubeEdge(projectedPoints[0], projectedPoints[1], cubeColor)
        drawCubeEdge(projectedPoints[1], projectedPoints[2], cubeColor)
        drawCubeEdge(projectedPoints[2], projectedPoints[3], cubeColor)
        drawCubeEdge(projectedPoints[3], projectedPoints[0], cubeColor)

        drawCubeEdge(projectedPoints[4], projectedPoints[5], cubeColor)
        drawCubeEdge(projectedPoints[5], projectedPoints[6], cubeColor)
        drawCubeEdge(projectedPoints[6], projectedPoints[7], cubeColor)
        drawCubeEdge(projectedPoints[7], projectedPoints[4], cubeColor)

        drawCubeEdge(projectedPoints[0], projectedPoints[4], cubeColor)
        drawCubeEdge(projectedPoints[1], projectedPoints[5], cubeColor)
        drawCubeEdge(projectedPoints[2], projectedPoints[6], cubeColor)
        drawCubeEdge(projectedPoints[3], projectedPoints[7], cubeColor)
    }
}

// تابع کمکی برای ضرب ماتریس در وکتور
private fun multiplyMatrix(matrix: FloatArray, vector: FloatArray): FloatArray {
    val result = FloatArray(3)
    result[0] = matrix[0] * vector[0] + matrix[1] * vector[1] + matrix[2] * vector[2]
    result[1] = matrix[3] * vector[0] + matrix[4] * vector[1] + matrix[5] * vector[2]
    result[2] = matrix[6] * vector[0] + matrix[7] * vector[1] + matrix[8] * vector[2]
    return result
}

// تابع کمکی برای تصویر کردن نقطه سه‌بعدی روی صفحه دو بعدی
private fun project(point: FloatArray, width: Float, height: Float): Offset {
    val scale = 100f
    val x = point[0] * scale + width / 2
    val y = point[1] * scale + height / 2
    return Offset(x, y)
}

// تابع کمکی برای رسم یک یال مکعب
private fun DrawScope.drawCubeEdge(start: Offset, end: Offset, color: Color) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 8f
    )
}