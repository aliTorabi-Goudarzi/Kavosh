@file:Suppress("SameParameterValue")

package ir.dekot.kavosh.feature_export_and_sharing.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.graphics.withTranslation
import java.io.FileOutputStream
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * تولیدکننده PDF برای گزارش‌های تشخیصی
 */
object DiagnosticPdfGenerator {

    /**
     * تولید PDF از متن گزارش
     */
    fun generatePdf(context: Context, fos: FileOutputStream, reportText: String, title: String) {
        val spannableBuilder = SpannableStringBuilder()

        // عنوان اصلی
        val mainTitle = "$title\n\n"
        spannableBuilder.append(mainTitle)
        spannableBuilder.setSpan(StyleSpan(Typeface.BOLD), 0, mainTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableBuilder.setSpan(RelativeSizeSpan(1.5f), 0, mainTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        spannableBuilder.setSpan(ForegroundColorSpan(Color.BLACK), 0, mainTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        // محتوای گزارش
        spannableBuilder.append(reportText)

        // تنظیمات صفحه
        val pageHeight = 1120
        val pageWidth = 792
        val margin = 50f
        val contentWidth = (pageWidth - 2 * margin).toInt()
        val contentHeight = (pageHeight - 2 * margin).toInt()

        val pdfDocument = PdfDocument()
        val paint = TextPaint().apply {
            textSize = 12f
            color = Color.BLACK
            isAntiAlias = true
        }

        // ایجاد layout متن
        val fullTextLayout = StaticLayout.Builder.obtain(
            spannableBuilder, 0, spannableBuilder.length, paint, contentWidth
        ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
         .setLineSpacing(0f, 1.2f)
         .build()

        val totalTextHeight = fullTextLayout.height
        var yOffset = 0
        var pageNumber = 1

        // تولید صفحات
        while (yOffset < totalTextHeight) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // رسم محتوا
            canvas.withTranslation(margin, margin) {
                translate(0f, -yOffset.toFloat())
                fullTextLayout.draw(this)
            }

            // رسم شماره صفحه
            drawPageNumber(canvas, pageNumber, pageWidth, pageHeight, margin)

            pdfDocument.finishPage(page)

            yOffset += contentHeight
            pageNumber++
        }

        // ذخیره PDF
        try {
            pdfDocument.writeTo(fos)
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * رسم شماره صفحه
     */
    private fun drawPageNumber(canvas: Canvas, pageNumber: Int, pageWidth: Int, pageHeight: Int, margin: Float) {
        val paint = Paint().apply {
            textSize = 10f
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val pageText = "صفحه $pageNumber"
        canvas.drawText(
            pageText,
            pageWidth / 2f,
            pageHeight - margin / 2,
            paint
        )
    }

    /**
     * تولید PDF با استایل پیشرفته
     */
    fun generateStyledPdf(
        context: Context, 
        fos: FileOutputStream, 
        reportText: String, 
        title: String,
        sections: List<ReportSection> = emptyList()
    ) {
        val spannableBuilder = SpannableStringBuilder()

        // عنوان اصلی با استایل
        addStyledTitle(spannableBuilder, title)

        // اضافه کردن بخش‌ها
        if (sections.isNotEmpty()) {
            sections.forEach { section ->
                addStyledSection(spannableBuilder, section)
            }
        } else {
            // اگر بخش‌های جداگانه نداریم، کل متن را اضافه می‌کنیم
            spannableBuilder.append(reportText)
        }

        // تولید PDF
        generatePdfFromSpannable(context, fos, spannableBuilder, title)
    }

    /**
     * اضافه کردن عنوان با استایل
     */
    private fun addStyledTitle(builder: SpannableStringBuilder, title: String) {
        val titleText = "$title\n\n"
        val startIndex = builder.length
        builder.append(titleText)
        val endIndex = builder.length

        builder.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        builder.setSpan(RelativeSizeSpan(1.8f), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        builder.setSpan(ForegroundColorSpan("#1976D2".toColorInt()), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    /**
     * اضافه کردن بخش با استایل
     */
    private fun addStyledSection(builder: SpannableStringBuilder, section: ReportSection) {
        // عنوان بخش
        val sectionTitle = "${section.title}\n"
        val titleStartIndex = builder.length
        builder.append(sectionTitle)
        val titleEndIndex = builder.length

        builder.setSpan(StyleSpan(Typeface.BOLD), titleStartIndex, titleEndIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        builder.setSpan(RelativeSizeSpan(1.3f), titleStartIndex, titleEndIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        builder.setSpan(ForegroundColorSpan("#424242".toColorInt()), titleStartIndex, titleEndIndex, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        // محتوای بخش
        builder.append("${section.content}\n\n")
    }

    /**
     * تولید PDF از SpannableStringBuilder
     */
    private fun generatePdfFromSpannable(
        context: Context,
        fos: FileOutputStream,
        spannableBuilder: SpannableStringBuilder,
        title: String
    ) {
        val pageHeight = 1120
        val pageWidth = 792
        val margin = 50f
        val contentWidth = (pageWidth - 2 * margin).toInt()
        val contentHeight = (pageHeight - 2 * margin).toInt()

        val pdfDocument = PdfDocument()
        val paint = TextPaint().apply {
            textSize = 12f
            color = Color.BLACK
            isAntiAlias = true
        }

        val fullTextLayout = StaticLayout.Builder.obtain(
            spannableBuilder, 0, spannableBuilder.length, paint, contentWidth
        ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
         .setLineSpacing(0f, 1.3f)
         .build()

        val totalTextHeight = fullTextLayout.height
        var yOffset = 0
        var pageNumber = 1

        while (yOffset < totalTextHeight) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // رسم پس‌زمینه
            drawPageBackground(canvas, pageWidth, pageHeight)

            // رسم محتوا
            canvas.withTranslation(margin, margin) {
                translate(0f, -yOffset.toFloat())
                fullTextLayout.draw(this)
            }

            // رسم header و footer
            drawPageHeader(canvas, title, pageWidth, margin)
            drawPageFooter(canvas, pageNumber, pageWidth, pageHeight, margin)

            pdfDocument.finishPage(page)

            yOffset += contentHeight
            pageNumber++
        }

        try {
            pdfDocument.writeTo(fos)
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * رسم پس‌زمینه صفحه
     */
    private fun drawPageBackground(canvas: Canvas, pageWidth: Int, pageHeight: Int) {
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paint)
    }

    /**
     * رسم header صفحه
     */
    private fun drawPageHeader(canvas: Canvas, title: String, pageWidth: Int, margin: Float) {
        val paint = Paint().apply {
            textSize = 14f
            color = "#1976D2".toColorInt()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        canvas.drawText(
            title,
            pageWidth / 2f,
            margin / 2,
            paint
        )

        // خط زیر header
        val linePaint = Paint().apply {
            color = "#E0E0E0".toColorInt()
            strokeWidth = 1f
        }
        canvas.drawLine(
            margin,
            margin * 0.7f,
            pageWidth - margin,
            margin * 0.7f,
            linePaint
        )
    }

    /**
     * رسم footer صفحه
     */
    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, pageWidth: Int, pageHeight: Int, margin: Float) {
        // خط بالای footer
        val linePaint = Paint().apply {
            color = "#E0E0E0".toColorInt()
            strokeWidth = 1f
        }
        canvas.drawLine(
            margin,
            pageHeight - margin * 0.7f,
            pageWidth - margin,
            pageHeight - margin * 0.7f,
            linePaint
        )

        // شماره صفحه
        val paint = Paint().apply {
            textSize = 10f
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawText(
            "صفحه $pageNumber",
            pageWidth / 2f,
            pageHeight - margin / 3,
            paint
        )

        // تاریخ تولید
        val datePaint = Paint().apply {
            textSize = 8f
            color = Color.GRAY
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        val currentDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(
            "تولید شده: $currentDate",
            margin,
            pageHeight - margin / 3,
            datePaint
        )

        // نام برنامه
        val appPaint = Paint().apply {
            textSize = 8f
            color = Color.GRAY
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText(
            "کاوش - Device Inspector",
            pageWidth - margin,
            pageHeight - margin / 3,
            appPaint
        )
    }
}

/**
 * بخش گزارش برای استایل‌دهی
 */
data class ReportSection(
    val title: String,
    val content: String
)
