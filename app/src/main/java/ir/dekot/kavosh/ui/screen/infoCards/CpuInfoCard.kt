package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.ui.screen.SectionTitleInCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

@Composable
fun CpuInfoCard(info: CpuInfo, liveFrequencies: List<String>) {
    InfoCard("پردازنده (CPU)") {
        InfoRow("مدل", info.model)
        InfoRow("توپولوژی", info.topology)
        InfoRow("لیتوگرافی", info.process)
        InfoRow("معماری", info.architecture)
        InfoRow("تعداد هسته‌ها", info.coreCount.toString())

        SectionTitleInCard(title = "سرعت هسته‌ها (لحظه‌ای)")
        val freqsToShow = if (liveFrequencies.isNotEmpty()) liveFrequencies else info.liveFrequencies

        freqsToShow.forEachIndexed { index, freq ->
            InfoRow("هسته $index", freq)

            // محاسبه درصد پیشرفت برای هر هسته
            val maxFreq = info.maxFrequenciesKhz.getOrElse(index) { 0L }
            val currentFreq = freq.removeSuffix(" MHz").trim().toLongOrNull() ?: 0L
            val progress = if (maxFreq > 0) (currentFreq * 1000) / maxFreq.toFloat() else 0f
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "CpuProgress$index")

            // اضافه کردن نوار پیشرفت برای هر هسته
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        SectionTitleInCard(title = "بازه سرعت کلاک")
        info.clockSpeedRanges.forEach { range ->
            val parts = range.split(":")
            InfoRow(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" }.trim())
        }
    }
}