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
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoRow

@Composable
fun GpuInfoCard(info: GpuInfo, liveLoad: Int?) {
    InfoCard("پردازنده گرافیکی (GPU)") {
        InfoRow("مدل", info.model)
        InfoRow("سازنده", info.vendor)
        InfoRow("لود GPU", "${liveLoad ?: 0} %")

        // ستون برای قرار دادن نوار پیشرفت
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(8.dp))

            // مقدار پیشرفت باید بین 0.0f و 1.0f باشد
            val progress = (liveLoad ?: 0) / 100f

            // انیمیشن نرم برای تغییرات نوار پیشرفت
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "GpuProgressAnimation")

            // نوار پیشرفت خطی
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