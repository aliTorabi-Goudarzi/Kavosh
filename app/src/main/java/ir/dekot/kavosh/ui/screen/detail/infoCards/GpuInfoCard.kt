package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow

@Composable
fun GpuInfoCard(info: GpuInfo, liveLoad: Int?) {
    InfoCard(stringResource(R.string.gpu_title)) {
        InfoRow(stringResource(R.string.gpu_model), info.model)
        InfoRow(stringResource(R.string.gpu_vendor), info.vendor)
        InfoRow(
            stringResource(R.string.gpu_load),
            stringResource(R.string.unit_format_percent, liveLoad ?: 0)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(8.dp))

            val progress = (liveLoad ?: 0) / 100f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "GpuProgressAnimation")

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}
// امضای تابع حالا شامل لود لحظه‌ای است