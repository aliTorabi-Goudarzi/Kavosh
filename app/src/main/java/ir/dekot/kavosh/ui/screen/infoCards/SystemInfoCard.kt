package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

@Composable
fun SystemInfoCard(info: SystemInfo) {
    InfoCard("اطلاعات سیستم") {
        InfoRow("نسخه اندروید", info.androidVersion)
        InfoRow("سطح API", info.sdkLevel)
        InfoRow("بیلد نامبر", info.buildNumber)
        InfoRow("وضعیت روت", if (info.isRooted) "روت شده" else "روت نشده")
    }
}