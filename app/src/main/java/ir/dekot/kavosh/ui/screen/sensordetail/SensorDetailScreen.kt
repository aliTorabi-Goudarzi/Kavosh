package ir.dekot.kavosh.ui.screen.sensordetail

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
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
    return when {
        bearing in 337.5..360.0 || bearing in 0.0..22.5 -> stringResource(R.string.compass_n)
        bearing in 22.5..67.5 -> stringResource(R.string.compass_ne)
        bearing in 67.5..112.5 -> stringResource(R.string.compass_e)
        bearing in 112.5..157.5 -> stringResource(R.string.compass_se)
        bearing in 157.5..202.5 -> stringResource(R.string.compass_s)
        bearing in 202.5..247.5 -> stringResource(R.string.compass_sw)
        bearing in 247.5..292.5 -> stringResource(R.string.compass_w)
        bearing in 292.5..337.5 -> stringResource(R.string.compass_nw)
        else -> ""
    }
}