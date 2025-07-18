package ir.dekot.kavosh.feature_deviceInfo.view.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.view.InfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.InfoRow
import ir.dekot.kavosh.feature_deviceInfo.model.DisplayInfo

@Composable
fun DisplayInfoCard(info: DisplayInfo) {
    InfoCard(stringResource(R.string.display_title)) {
        InfoRow(stringResource(R.string.display_resolution), info.resolution)
        InfoRow(stringResource(R.string.display_density), info.density)
        InfoRow(stringResource(R.string.display_refresh_rate), info.refreshRate)
    }
}