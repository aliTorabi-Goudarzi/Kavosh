package ir.dekot.kavosh.ui.screen.detail.infoCards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.detail.infoCards.used_compose.InfoRow
import ir.dekot.kavosh.ui.screen.shared.SectionTitleInCard

@Composable
fun CpuInfoCard(info: CpuInfo, liveFrequencies: List<String>) {
    InfoCard(stringResource(R.string.cpu_title)) {
        InfoRow(stringResource(R.string.cpu_model), info.model)
        InfoRow(stringResource(R.string.cpu_topology), info.topology)
        InfoRow(stringResource(R.string.cpu_process), info.process)
        InfoRow(stringResource(R.string.cpu_architecture), info.architecture)
        InfoRow(stringResource(R.string.cpu_core_count), info.coreCount.toString())

        SectionTitleInCard(title = stringResource(R.string.cpu_live_speed))

        val freqsToShow = (if (liveFrequencies.isNotEmpty()) liveFrequencies else info.liveFrequencies)
            .mapIndexed { index, freq -> Pair(index, freq) }
            .chunked(2)

        freqsToShow.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { (index, freq) ->
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow(stringResource(R.string.cpu_core_prefix, index), freq)

                        val maxFreq = info.maxFrequenciesKhz.getOrElse(index) { 0L }
                        val currentFreq = freq.split(" ")[0].toLongOrNull() ?: 0L
                        val progress = if (maxFreq > 0) (currentFreq * 1000) / maxFreq.toFloat() else 0f
                        val animatedProgress by animateFloatAsState(targetValue = progress, label = "CpuProgress$index")

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(5.dp)
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        SectionTitleInCard(title = stringResource(R.string.cpu_clock_range))
        info.clockSpeedRanges.forEach { range ->
            val parts = range.split(":")
            InfoRow(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" }.trim())
        }
    }
}