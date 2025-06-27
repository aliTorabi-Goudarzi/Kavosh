package ir.dekot.kavosh.ui.screen.sensordetail

import android.graphics.Paint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoRow
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
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
        // *** تغییر کلیدی: افزودن قابلیت اسکرول به ستون اصلی ***
       Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp) // padding عمودی به آیتم‌ها داده می‌شود
                .verticalScroll(rememberScrollState()), // قابلیت اسکرول اضافه شد
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // چینش از بالا
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

            // اضافه کردن یک فاصله در پایین
            Spacer(modifier = Modifier.height(16.dp))
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
            color = colorScheme.onSurfaceVariant
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



@Composable
fun Compass(bearing: Float, modifier: Modifier = Modifier) {
    val primaryColor = colorScheme.primary
    val onSurfaceColor = colorScheme.onSurface

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
            color = colorScheme.onSurfaceVariant
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
            tint = colorScheme.primary
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
            color = colorScheme.onSurfaceVariant
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
            tint = colorScheme.primary
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
        Text(text = stringResource(R.string.accelerometer_unit), style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
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
        Text(text = stringResource(R.string.accelerometer_unit), style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
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
        Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = stringResource(R.string.step_counter_title), modifier = Modifier.size(120.dp), tint = colorScheme.primary)
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
        Icon(imageVector = Icons.Default.Thermostat, contentDescription = stringResource(R.string.ambient_temperature_title), modifier = Modifier.size(120.dp), tint = colorScheme.primary)
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
        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = stringResource(R.string.relative_humidity_title), modifier = Modifier.size(120.dp), tint = colorScheme.primary)
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
        Text(text = unit, style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)

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
        Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = stringResource(R.string.step_detector_title), modifier = Modifier.size(120.dp), tint = colorScheme.primary)
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
    val color by animateColorAsState(targetValue = if (eventDetected) colorScheme.primaryContainer else colorScheme.surface, label = "triggerColorAnim")
    val iconToShow = if (eventDetected) Icons.Default.CheckCircle else icon

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = iconToShow, contentDescription = null, modifier = Modifier.size(120.dp), tint = colorScheme.primary)
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
    val rotationHistory by viewModel.rotationVectorHistory.collectAsState()
    val lastRotationVector = rotationHistory.lastOrNull() ?: FloatArray(4)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.rotation_vector_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // مکعب بازطراحی شده
        RotatingCube(rotationVector = lastRotationVector)

        Spacer(modifier = Modifier.height(32.dp))

        // کارت نمایش مقادیر عددی زنده
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

        // نمودار بازطراحی شده
        LiveRotationChart(history = rotationHistory)
    }
}

@Composable
fun AdvancedCompass(rotationDegrees: Float) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = colorScheme.primary
    val onSurfaceColor = colorScheme.onSurface
    val textStyle = TextStyle(color = onSurfaceColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)

    Box(modifier = Modifier.padding(16.dp)) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val radius = size.minDimension / 2
            val center = this.center

            rotate(degrees = rotationDegrees, pivot = center) {
                for (i in 0 until 360 step 15) {
                    val lineLength = if (i % 45 == 0) 25f else 15f
                    rotate(degrees = i.toFloat(), pivot = center) {
                        drawLine(color = onSurfaceColor, start = Offset(center.x, 0f), end = Offset(center.x, lineLength), strokeWidth = 3f)
                    }
                }
                drawText(textMeasurer, "N", Offset(center.x - textMeasurer.measure("N").size.width / 2, 35f), style = textStyle.copy(color = primaryColor))
                drawText(textMeasurer, "E", Offset(size.width - 45f, center.y - 15), style = textStyle)
                drawText(textMeasurer, "S", Offset(center.x - textMeasurer.measure("S").size.width / 2, size.height - 55f), style = textStyle)
                drawText(textMeasurer, "W", Offset(35f, center.y - 15), style = textStyle)
            }

            val needlePath = Path().apply {
                moveTo(center.x, 0f)
                lineTo(center.x - 20f, 50f)
                lineTo(center.x + 20f, 50f)
                close()
            }
            drawPath(path = needlePath, color = primaryColor)
            drawCircle(color = primaryColor, radius = 10f, center = center)
        }
    }
}

@Composable
private fun AngleIndicator(label: String, angle: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge)
        Text(
            text = "%.1f°".format(angle),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
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

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CompassSensorContent(viewModel: DeviceInfoViewModel) {
    val orientationAngles by viewModel.orientationAngles.collectAsState()
    val accelerometerData by viewModel.accelerometerData.collectAsState()
    val magnetometerData by viewModel.magnetometerData.collectAsState()

    val bearing = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
    val animatedBearing by animateFloatAsState(targetValue = -bearing, label = "bearingAnim")
    val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
    val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AdvancedCompass(rotationDegrees = animatedBearing)
        Spacer(modifier = Modifier.height(24.dp))
        // *** کامپوننت جدید افق مصنوعی اضافه شد ***
        ArtificialHorizon(pitch = pitch, roll = roll)
        Spacer(modifier = Modifier.height(24.dp))
        InfoCard(title = stringResource(R.string.orientation_angles)) {
            AngleIndicator(label = stringResource(R.string.rotation_pitch), angle = Math.toDegrees(orientationAngles[1].toDouble()).toFloat())
            AngleIndicator(label = stringResource(R.string.rotation_roll), angle = Math.toDegrees(orientationAngles[2].toDouble()).toFloat())
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoCard(title = stringResource(R.string.raw_sensor_data)) {
            Text(stringResource(R.string.uncalibrated_accelerometer_title), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            InfoRow(label = "X", value = "%.2f".format(accelerometerData.getOrNull(0) ?: 0f))
            InfoRow(label = "Y", value = "%.2f".format(accelerometerData.getOrNull(1) ?: 0f))
            InfoRow(label = "Z", value = "%.2f".format(accelerometerData.getOrNull(2) ?: 0f))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.uncalibrated_magnetometer_title), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            InfoRow(label = "X", value = "%.2f".format(magnetometerData.getOrNull(0) ?: 0f))
            InfoRow(label = "Y", value = "%.2f".format(magnetometerData.getOrNull(1) ?: 0f))
            InfoRow(label = "Z", value = "%.2f".format(magnetometerData.getOrNull(2) ?: 0f))
        }
    }
}


/**
 * *** کامپوننت بازنویسی شده و نهایی برای نمایش افق مصنوعی ***
 */
/**
 * *** کامپوننت بازنویسی شده افق مصنوعی با استایل شیشه‌ای ***
 */

@Composable
fun ArtificialHorizon(pitch: Float, roll: Float) {
    val animatedPitch by animateFloatAsState(targetValue = pitch, label = "pitchAnim")
    val animatedRoll by animateFloatAsState(targetValue = -roll, label = "rollAnim")

    val skyColor = Color(0xFF42A5F5)
    val groundColor = Color(0xFF6D4C41)
    val horizonLineColor = Color.White
    val markingsColor = Color.White.copy(alpha = 0.8f)
    val planeColor = colorScheme.primary
    // *** متغیر فراموش شده در اینجا تعریف شد ***
    val onSurfaceColor = colorScheme.onSurface

    val textPaint = remember {
        Paint().apply {
            isAntiAlias = true
            textSize = 12.sp.value
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
        }
    }

    Box(
        modifier = Modifier
            .size(250.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = this.center

            clipPath(Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, radius)) }) {
                rotate(degrees = animatedRoll, pivot = center) {
                    val pitchTranslation = animatedPitch * (radius / 45f)

                    drawRect(color = skyColor, size = size.copy(height = center.y + pitchTranslation + radius), topLeft = Offset(0f, -radius))
                    drawRect(color = groundColor, topLeft = Offset(0f, center.y + pitchTranslation))
                    drawLine(color = horizonLineColor, start = Offset(0f, center.y + pitchTranslation), end = Offset(size.width, center.y + pitchTranslation), strokeWidth = 5f)

                    for (i in -90..90 step 10) {
                        if (i == 0) continue
                        val yPos = center.y + pitchTranslation - (i * (radius / 45f))
                        val lineLength = if (i % 30 == 0) 100.dp.toPx() else 50.dp.toPx()
                        drawLine(color = markingsColor, start = Offset(center.x - lineLength / 2, yPos), end = Offset(center.x + lineLength / 2, yPos), strokeWidth = 2f)

                        if (i % 30 == 0) {
                            drawContext.canvas.nativeCanvas.drawText(abs(i).toString(), center.x - lineLength/2 - 20.dp.toPx() , yPos + 5.dp.toPx(), textPaint)
                            drawContext.canvas.nativeCanvas.drawText(abs(i).toString(), center.x + lineLength/2 + 20.dp.toPx() , yPos + 5.dp.toPx(), textPaint)
                        }
                    }
                }
            }

            // *** استفاده از متغیر صحیح شده ***
            drawCircle(color = onSurfaceColor, radius = radius, style = Stroke(width = 8f), center = center)

            val planeWingWidth = 100.dp.toPx()
            val planeBodyWidth = 40.dp.toPx()
            val planeStrokeWidth = 8f

            drawLine(planeColor, start = Offset(center.x - planeWingWidth / 2, center.y), end = Offset(center.x + planeWingWidth / 2, center.y), strokeWidth = planeStrokeWidth)
            drawLine(planeColor, start = Offset(center.x - planeWingWidth / 2, center.y), end = Offset(center.x - planeWingWidth / 2, center.y - 10f), strokeWidth = planeStrokeWidth / 2)
            drawLine(planeColor, start = Offset(center.x + planeWingWidth / 2, center.y), end = Offset(center.x + planeWingWidth / 2, center.y - 10f), strokeWidth = planeStrokeWidth / 2)
            drawLine(planeColor, start = Offset(center.x - planeBodyWidth / 2, center.y + 15f), end = Offset(center.x + planeBodyWidth / 2, center.y + 15f), strokeWidth = planeStrokeWidth)
        }
    }
}

/**
 * *** کامپوننت بازنویسی شده برای نمایش مکعب سه‌بعدی نئونی و شیشه‌ای ***
 */
/**
 * *** کامپوننت بازنویسی شده برای مکعب سه‌بعدی نئونی و شیشه‌ای بزرگتر ***
 */
/**
 * *** کامپوننت بازنویسی شده برای مکعب سه‌بعدی نئونی و شیشه‌ای بزرگتر ***
 */
@Composable
fun RotatingCube(rotationVector: FloatArray) {
    val neonColor = colorScheme.primary
    val glassColor = colorScheme.primary.copy(alpha = 0.15f)

    Canvas(modifier = Modifier.size(250.dp)) { // *** افزایش اندازه Canvas ***
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
            projectedVertices[i] = project(rotated, size.width, size.height, scale = 150f)//برای بزرگ کردن سایز مکعب
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


// تابع کمکی برای تصویر کردن با مقیاس قابل تنظیم
private fun project(point: FloatArray, width: Float, height: Float, scale: Float): Offset {
    val x = point[0] * scale + width / 2
    val y = point[1] * scale + height / 2
    return Offset(x, y)
}




// تابع کمکی جدید برای رسم خطوط نئونی
private fun DrawScope.drawNeonEdge(start: Offset, end: Offset, color: Color) {
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 6f
        maskFilter = android.graphics.BlurMaskFilter(15f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    // هاله‌ی درخشان
    drawIntoCanvas {
        paint.color = color.copy(alpha = 0.5f).toArgb()
        it.nativeCanvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }

    // خط اصلی
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 4f
    )
}

/**
 * *** کامپوننت جدید برای رسم نمودار زنده ***
 */
/**
 * *** کامپوننت بازنویسی شده برای نمودار پیشرفته ***
 */
/**
 * *** کامپوننت بازنویسی شده برای نمودار پیشرفته ***
 */
@Composable
fun LiveRotationChart(history: List<FloatArray>) {
    val colors = listOf(Color.Red, Color.Green, Color(0xFF37A6FF))
    val labels = listOf("X", "Y", "Z")

    val axisColor = colorScheme.onSurface.copy(alpha = 0.5f)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = axisColor, fontSize = 10.sp)

    InfoCard(title = stringResource(R.string.live_chart_title)) {
        Column(Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)) {
                if (history.size < 2) return@Canvas

                val stepX = size.width / (history.size - 1).coerceAtLeast(1)

                // رسم محورها و شبکه نقطه‌چین
                val gridPath = Path()
                val yGridSteps = 4
                for (i in 0..yGridSteps) {
                    val y = i * (size.height / yGridSteps)
                    gridPath.moveTo(0f, y)
                    gridPath.lineTo(size.width, y)
                }
                drawPath(gridPath, color = axisColor, style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))

                drawLine(axisColor, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 2f)

                // رسم نمودارها
                for (axisIndex in 0..2) {
                    val path = Path()
                    val glassPath = Path()

                    history.forEachIndexed { index, data ->
                        val x = index * stepX
                        val y = (1 - ((data.getOrNull(axisIndex) ?: 0f) + 1) / 2) * size.height

                        if (index == 0) {
                            path.moveTo(x, y)
                            glassPath.moveTo(x, size.height)
                            glassPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            glassPath.lineTo(x, y)
                        }
                    }

                    glassPath.lineTo((history.size - 1) * stepX, size.height)
                    glassPath.close()

                    drawPath(glassPath, brush = Brush.verticalGradient(
                        colors = listOf(colors[axisIndex].copy(alpha = 0.3f), Color.Transparent)
                    ))

                    // رسم خط نئونی
                    drawPath(path, color = colors[axisIndex].copy(alpha = 0.4f), style = Stroke(width = 10f))
                    drawPath(path, color = colors[axisIndex], style = Stroke(width = 4f))

                    // *** بخش جدید: رسم لیبل متحرک و نئونی ***
                    val lastX = (history.size - 1) * stepX
                    val lastY = (1 - ((history.last().getOrNull(axisIndex) ?: 0f) + 1) / 2) * size.height

                    // رسم نقطه نئونی
                    drawCircle(colors[axisIndex].copy(alpha = 0.5f), radius = 20f, center = Offset(lastX, lastY))
                    drawCircle(colors[axisIndex], radius = 9f, center = Offset(lastX, lastY))

//                    // رسم متن نئونی
//                    val labelText = labels[axisIndex]
//                    val textLayoutResult = textMeasurer.measure(labelText, style = textStyle.copy(color = colors[axisIndex]))
//                    val textOffset = Offset(lastX - textLayoutResult.size.width/2 , lastY - 35.dp.toPx())

//                    drawText(textMeasurer, labelText, textOffset, style = textStyle.copy(color = colors[axisIndex].copy(alpha = 0.5f)))
//                    drawText(textMeasurer, labelText, textOffset, style = textStyle.copy(color = Color.White))
                }

                // رسم برچسب‌های محور Y
                drawText(textMeasurer, "1.0", Offset(5.dp.toPx(), -5.dp.toPx()), style = textStyle)
                drawText(textMeasurer, "0.0", Offset(5.dp.toPx(), center.y - 10.sp.toPx()), style = textStyle)
                drawText(textMeasurer, "-1.0", Offset(5.dp.toPx(), size.height - 20.sp.toPx()), style = textStyle)
            }
            // *** راهنمای نمودار با نقاط نئونی ***
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                labels.forEachIndexed { index, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // استفاده از Canvas برای رسم نقطه نئونی
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawNeonDot(center = this.center, color = colors[index], radius = 9f, glowRadius = 20f)
                        }
                        Text(text = label, modifier = Modifier.padding(start = 8.dp))
                    }
                    }
                }
            }}}

/**
 * *** تابع کمکی جدید برای رسم یک نقطه نئونی ***
 */
fun DrawScope.drawNeonDot(center: Offset, color: Color, radius: Float = 6f, glowRadius: Float = 12f) {
    // رسم هاله درخشان
    drawCircle(color.copy(alpha = 0.5f), radius = glowRadius, center = center)
    // رسم نقطه اصلی
    drawCircle(color, radius = radius, center = center)
}

//// تابع کمکی جدید برای رسم مسیر نئونی
//private fun DrawScope.drawNeonPath(path: Path, color: Color) {
//    val paint = Paint().apply {
//        isAntiAlias = true
//        style = Paint.Style.STROKE
//        strokeWidth = 8f
//        maskFilter = android.graphics.BlurMaskFilter(15f, android.graphics.BlurMaskFilter.Blur.NORMAL)
//    }
//
//    drawIntoCanvas {
//        paint.color = color.copy(alpha = 0.5f).toArgb()
//        it.nativeCanvas.drawPath(path, paint)
//    }
//    drawPath(path, color = color, style = Stroke(width = 5f))
//}

