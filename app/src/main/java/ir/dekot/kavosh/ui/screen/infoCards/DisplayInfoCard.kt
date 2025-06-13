package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

@Composable
fun DisplayInfoCard(info: DisplayInfo) {
    InfoCard("صفحه نمایش") {
        InfoRow("رزولوشن", info.resolution)
        InfoRow("تراکم پیکسلی", info.density)
        InfoRow("نرخ نوسازی", info.refreshRate)
    }
}