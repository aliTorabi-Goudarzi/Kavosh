package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

@Composable
fun GpuInfoCard(info: GpuInfo, liveLoad: Int?) {
    InfoCard("پردازنده گرافیکی (GPU)") {
        InfoRow("مدل", info.model)
        InfoRow("سازنده", info.vendor)
        // اینجا از عملگر الویس (?:) برای نمایش 0 به عنوان جایگزین استفاده می‌کنیم
        InfoRow("لود GPU", "${liveLoad ?: 0} %")
    }
}
// امضای تابع حالا شامل لود لحظه‌ای است