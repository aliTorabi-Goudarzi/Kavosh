package ir.dekot.kavosh.ui.screen.sensordetail.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R

/**
 * کامپوزبل اختصاصی برای نمایش داده‌های سنسور دمای محیط.
 * @param temperature مقدار دما بر حسب سانتی‌گراد.
 */
@Composable
fun AmbientTemperatureSensorView(temperature: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(
            imageVector = Icons.Default.Thermostat,
            contentDescription = stringResource(R.string.ambient_temperature_title),
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.unit_format_celsius, temperature),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.ambient_temperature_title),
            style = MaterialTheme.typography.titleLarge
        )
    }
}