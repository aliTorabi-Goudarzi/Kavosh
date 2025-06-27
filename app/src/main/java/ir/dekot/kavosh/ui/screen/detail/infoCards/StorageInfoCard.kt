package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow

@Composable
fun StorageInfoCard(info: StorageInfo) {
    InfoCard(stringResource(R.string.storage_title)) {
        InfoRow(stringResource(R.string.storage_total), info.total)
        InfoRow(stringResource(R.string.storage_available), info.available)
    }
}