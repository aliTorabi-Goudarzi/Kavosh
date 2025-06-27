package ir.dekot.kavosh.ui.screen.sensordetail.views

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import kotlinx.coroutines.delay

/**
 * کامپوزبل عمومی برای نمایش رویدادهای سنسورهای تریگر (Trigger).
 * @param titleRes شناسه منبع رشته برای عنوان سنسور.
 * @param eventCount تعداد کل رویدادها.
 * @param icon آیکون مورد نظر برای نمایش.
 */
@Composable
fun TriggerSensorView(
    @StringRes titleRes: Int,
    eventCount: Int,
    icon: ImageVector = Icons.Default.CheckCircle // آیکون پیش‌فرض
) {
    var eventDetected by remember { mutableStateOf(false) }

    // این افکت هر بار که eventCount تغییر کند، انیمیشن را اجرا می‌کند
    LaunchedEffect(eventCount) {
        if (eventCount > 0) {
            eventDetected = true
            delay(1500) // پیام برای ۱.۵ ثانیه نمایش داده می‌شود
            eventDetected = false
        }
    }

    val textToShow = if (eventDetected) stringResource(R.string.trigger_sensor_detected) else stringResource(R.string.trigger_sensor_waiting)
    val color by animateColorAsState(
        targetValue = if (eventDetected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "triggerColorAnim"
    )
    val iconToShow = if (eventDetected) Icons.Default.CheckCircle else icon

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconToShow,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = textToShow, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}