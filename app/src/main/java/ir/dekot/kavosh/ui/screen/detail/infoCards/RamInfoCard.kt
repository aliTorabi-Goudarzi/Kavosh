package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoRow

@Composable
fun RamInfoCard(info: RamInfo) {
    InfoCard(stringResource(R.string.ram_title)) {
        InfoRow(stringResource(R.string.ram_total), info.total)
        InfoRow(stringResource(R.string.ram_available), info.available)
    }
}