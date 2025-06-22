package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoRow

@Composable
fun RamInfoCard(info: RamInfo) {
    InfoCard("حافظه RAM") {
        InfoRow("کل حافظه", info.total)
        InfoRow("حافظه در دسترس", info.available)
    }
}