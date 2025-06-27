package ir.dekot.kavosh.ui.screen.sensordetail.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R

/**
 * کامپوزبل اختصاصی برای نمایش داده‌های سنسور مجاورت (Proximity).
 * @param distance فاصله شناسایی شده توسط سنسور به سانتی‌متر.
 */
@Composable
fun ProximitySensorView(distance: Float) {
    // اکثر سنسورها یک آستانه حدود 5 سانتی‌متر دارند
    val isNear = distance < 5f

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