package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

@Composable
fun BatteryInfoCard(info: BatteryInfo) {
    InfoCard("باتری") {
        InfoRow("سلامت", info.health)
        InfoRow("درصد شارژ", "${info.level}%")
        InfoRow("وضعیت شارژ", info.status)
        InfoRow("تکنولوژی", info.technology)
        InfoRow("دما", info.temperature)
        InfoRow("ولتاژ", info.voltage)
    }
}