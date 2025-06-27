package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow

@Composable
fun DisplayInfoCard(info: DisplayInfo) {
    InfoCard(stringResource(R.string.display_title)) {
        InfoRow(stringResource(R.string.display_resolution), info.resolution)
        InfoRow(stringResource(R.string.display_density), info.density)
        InfoRow(stringResource(R.string.display_refresh_rate), info.refreshRate)
    }
}