package ir.dekot.kavosh.feature_deviceInfo.view.infoCards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.view.InfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.InfoRow

@Composable
fun StorageSpeedTestCard(
    isTesting: Boolean,
    progress: Float,
    writeSpeed: String,
    readSpeed: String,
    onStartTest: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "StorageTestProgress")

    InfoCard(title = stringResource(R.string.storage_speed_test_title)) {
        // بخش توضیحات
        Text(
            text = stringResource(R.string.storage_speed_test_description),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // بخش نتایج
        InfoRow(label = stringResource(R.string.storage_speed_write), value = writeSpeed)
        InfoRow(label = stringResource(R.string.storage_speed_read), value = readSpeed)

        Spacer(modifier = Modifier.height(16.dp))

        // بخش کنترل تست (دکمه و نوار پیشرفت)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isTesting) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStartTest,
                enabled = !isTesting // دکمه در حین تست غیرفعال می‌شود
            ) {
                Text(text = stringResource(R.string.storage_speed_test_button))
            }
        }
    }
}