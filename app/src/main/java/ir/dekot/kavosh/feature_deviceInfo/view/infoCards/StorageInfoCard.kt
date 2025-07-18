package ir.dekot.kavosh.feature_deviceInfo.view.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.model.StorageInfo
import ir.dekot.kavosh.feature_deviceInfo.view.InfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.InfoRow

@Composable
fun StorageInfoCard(info: StorageInfo) {
    InfoCard(stringResource(R.string.storage_title)) {
        InfoRow(stringResource(R.string.storage_total), info.total)
        InfoRow(stringResource(R.string.storage_available), info.available)
    }
}