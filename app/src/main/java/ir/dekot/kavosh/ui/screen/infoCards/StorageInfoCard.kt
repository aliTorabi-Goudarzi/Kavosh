package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

@Composable
fun StorageInfoCard(info: StorageInfo) {
    InfoCard("حافظه داخلی") {
        InfoRow("کل حافظه", info.total)
        InfoRow("حافظه در دسترس", info.available)
    }
}