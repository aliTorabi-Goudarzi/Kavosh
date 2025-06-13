package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.ui.screen.SectionTitleInCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

// ورودی تابع حالا شامل لیست فرکانس‌های زنده است
@Composable
fun CpuInfoCard(info: CpuInfo, liveFrequencies: List<String>) {
    InfoCard("پردازنده (CPU)") {
        InfoRow("مدل", info.model)
        InfoRow("توپولوژی", info.topology)
        InfoRow("لیتوگرافی", info.process)
        InfoRow("معماری", info.architecture)
        InfoRow("تعداد هسته‌ها", info.coreCount.toString())

        // بخش جدید: نمایش فرکانس‌های زنده
        SectionTitleInCard(title = "سرعت هسته‌ها (لحظه‌ای)")
        // اگر لیست فرکانس زنده خالی بود (مثلا در لحظه اول)، مقدار اولیه را نشان بده
        val freqsToShow =
            if (liveFrequencies.isNotEmpty()) liveFrequencies else info.liveFrequencies
        freqsToShow.forEachIndexed { index, freq ->
            InfoRow("هسته $index", freq)
        }

        // بخش جدید: نمایش بازه سرعت
        SectionTitleInCard(title = "بازه سرعت کلاک")
        info.clockSpeedRanges.forEach { range ->
            // اینجا فرض می‌کنیم range به فرمت "هسته X: Y - Z MHz" است
            val parts = range.split(":")
            InfoRow(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" }.trim())
        }
    }
}