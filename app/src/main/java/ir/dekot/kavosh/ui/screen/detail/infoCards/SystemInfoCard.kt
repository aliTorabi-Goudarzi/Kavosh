package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow

@Composable
fun SystemInfoCard(info: SystemInfo) {
    InfoCard(stringResource(R.string.system_info_title)) {
        InfoRow(stringResource(R.string.system_android_version), info.androidVersion)
        InfoRow(stringResource(R.string.system_sdk_level), info.sdkLevel)
        InfoRow(stringResource(R.string.system_build_number), info.buildNumber)
        InfoRow(
            stringResource(R.string.system_root_status),
            stringResource(if (info.isRooted) R.string.label_rooted else R.string.label_not_rooted)
        )
    }
}