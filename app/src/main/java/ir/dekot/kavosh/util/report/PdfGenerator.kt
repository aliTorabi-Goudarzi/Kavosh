package ir.dekot.kavosh.util.report

import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.graphics.withTranslation
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import java.io.FileOutputStream

/**
 * یک آبجکت کمکی برای تولید گزارش کامل مشخصات دستگاه در فرمت PDF.
 */
object PdfGenerator {

    /**
     * تابع نهایی برای ساخت PDF استایل‌دار با صفحه‌بندی صحیح.
     */
    fun writeStyledPdf(fos: FileOutputStream, deviceInfo: DeviceInfo, batteryInfo: BatteryInfo) {
        val spannableBuilder = SpannableStringBuilder()

        val mainTitle = "گزارش کامل مشخصات دستگاه\n\n"
        spannableBuilder.append(mainTitle)
        spannableBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, mainTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableBuilder.setSpan(RelativeSizeSpan(1.5f), 0, mainTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableBuilder.setSpan(ForegroundColorSpan(android.graphics.Color.BLACK), 0, mainTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        InfoCategory.entries.forEach { category ->
            val startSection = spannableBuilder.length
            val sectionTitle = "--- ${category.title} ---\n"
            spannableBuilder.append(sectionTitle)
            spannableBuilder.setSpan(StyleSpan(Typeface.BOLD), startSection, spannableBuilder.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            spannableBuilder.setSpan(ForegroundColorSpan(android.graphics.Color.rgb(0, 50, 150)), startSection, spannableBuilder.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            // استفاده مستقیم از تابع جدید برای دریافت بدنه گزارش، بدون نیاز به دستکاری رشته
            val contentText = ReportFormatter.formatCategoryBody(category, deviceInfo, batteryInfo)
            spannableBuilder.append(contentText)
            spannableBuilder.append("\n\n")
        }

        val pageHeight = 1120
        val pageWidth = 792
        val margin = 50f
        val contentWidth = (pageWidth - 2 * margin).toInt()
        val contentHeight = (pageHeight - 2 * margin).toInt()

        val pdfDocument = PdfDocument()
        val paint = TextPaint().apply {
            textSize = 12f
        }

        val fullTextLayout = StaticLayout.Builder.obtain(
            spannableBuilder, 0, spannableBuilder.length, paint, contentWidth
        ).build()

        val totalTextHeight = fullTextLayout.height
        var yOffset = 0
        var pageNumber = 1

        while (yOffset < totalTextHeight) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.withTranslation(margin, margin) {
                translate(0f, -yOffset.toFloat())
                fullTextLayout.draw(this)
            }

            pdfDocument.finishPage(page)

            yOffset += contentHeight
            pageNumber++
        }

        try {
            pdfDocument.writeTo(fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
}