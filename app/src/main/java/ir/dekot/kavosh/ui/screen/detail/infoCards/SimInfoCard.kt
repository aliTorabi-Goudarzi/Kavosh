package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.SimInfo
import ir.dekot.kavosh.ui.composables.InfoCard
import ir.dekot.kavosh.ui.composables.InfoRow

@Composable
fun SimInfoCard(info: SimInfo) {
    InfoCard(title = stringResource(id = R.string.sim_slot_index, info.slotIndex + 1)) {
        InfoRow(stringResource(R.string.sim_carrier), info.carrierName)
        InfoRow(stringResource(R.string.sim_country_iso), info.countryIso)
        InfoRow(stringResource(R.string.sim_country_code), info.mobileCountryCode)
        InfoRow(stringResource(R.string.sim_network_code), info.mobileNetworkCode)
        InfoRow(stringResource(R.string.sim_is_roaming), stringResource(if (info.isRoaming) R.string.label_on else R.string.label_off))
        InfoRow(stringResource(R.string.sim_data_roaming), info.dataRoaming)
    }
}