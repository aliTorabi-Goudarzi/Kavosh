package ir.dekot.kavosh.ui.screen.infoCards

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

        // لیست فرکانس‌های زنده را به صورت تکه‌های دوتایی درمی‌آوریم
        val freqsToShow = (if (liveFrequencies.isNotEmpty()) liveFrequencies else info.liveFrequencies)
            .mapIndexed { index, freq -> Pair(index, freq) }
            .chunked(2)

        // برای هر تکه دوتایی، یک ردیف (Row) ایجاد می‌کنیم
        freqsToShow.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // برای هر آیتم در ردیف، یک ستون (Column) با وزن برابر ایجاد می‌کنیم
                rowItems.forEach { (index, freq) ->
                    Column(modifier = Modifier.weight(1f)) {
                        InfoRow("هسته $index", freq)

                        val maxFreq = info.maxFrequenciesKhz.getOrElse(index) { 0L }
                        val currentFreq = freq.removeSuffix(" MHz").trim().toLongOrNull() ?: 0L
                        val progress = if (maxFreq > 0) (currentFreq * 1000) / maxFreq.toFloat() else 0f
                        val animatedProgress by animateFloatAsState(targetValue = progress, label = "CpuProgress$index")

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(5.dp)
                        )
                    }
                }
                // اگر ردیف فقط یک آیتم داشت (در تعداد فرد هسته‌ها)، یک فضای خالی با وزن برابر اضافه می‌کنیم
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp)) // یک فاصله اضافه قبل از بخش بعدی
        SectionTitleInCard(title = "بازه سرعت کلاک")
        info.clockSpeedRanges.forEach { range ->
            val parts = range.split(":")
            InfoRow(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" }.trim())
        }
    }
}