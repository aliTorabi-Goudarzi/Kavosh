package ir.dekot.kavosh.feature_export_and_sharing.model

import android.content.Context
import android.os.Build
import ir.dekot.kavosh.feature_deviceInfo.model.BatteryInfo
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_deviceInfo.model.getTitle
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * کلاس تولید گزارش Excel با فرمت‌بندی زیبا
 */
object ExcelReportGenerator {

    /**
     * تولید گزارش Excel کامل
     */
    fun generateExcelReport(
        context: Context,
        outputStream: OutputStream,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ) {
        val workbook = XSSFWorkbook()
        
        try {
            // ایجاد شیت اصلی
            val sheet = workbook.createSheet("گزارش دستگاه")
            
            // تنظیم عرض ستون‌ها
            sheet.setColumnWidth(0, 6000) // ستون برچسب
            sheet.setColumnWidth(1, 8000) // ستون مقدار
            
            // ایجاد استایل‌ها
            val styles = createStyles(workbook)
            
            var currentRow = 0
            
            // ایجاد هدر
            currentRow = createHeader(sheet, styles, currentRow, deviceInfo)
            currentRow += 2
            
            // ایجاد خلاصه دستگاه
            currentRow = createDeviceOverview(sheet, styles, currentRow, deviceInfo, batteryInfo)
            currentRow += 2
            
            // ایجاد بخش‌های مختلف اطلاعات
            InfoCategory.entries.forEach { category ->
                currentRow = createCategorySection(
                    context, sheet, styles, currentRow, category, deviceInfo, batteryInfo
                )
                currentRow += 1
            }
            
            // ایجاد فوتر
            createFooter(sheet, styles, currentRow)
            
            // نوشتن به فایل
            workbook.write(outputStream)
            
        } finally {
            workbook.close()
        }
    }

    /**
     * ایجاد استایل‌های مختلف برای Excel
     */
    private fun createStyles(workbook: Workbook): Map<String, CellStyle> {
        val styles = mutableMapOf<String, CellStyle>()
        
        // استایل هدر اصلی
        val headerStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 16
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
        }
        styles["header"] = headerStyle
        
        // استایل عنوان دسته
        val categoryStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 14
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            fillForegroundColor = IndexedColors.BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.RIGHT
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
        }
        styles["category"] = categoryStyle
        
        // استایل برچسب
        val labelStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 11
            }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.RIGHT
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
        }
        styles["label"] = labelStyle
        
        // استایل مقدار
        val valueStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                fontHeightInPoints = 11
            }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
        }
        styles["value"] = valueStyle
        
        // استایل فوتر
        val footerStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                italic = true
                fontHeightInPoints = 10
                color = IndexedColors.GREY_50_PERCENT.index
            }
            setFont(font)
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
        styles["footer"] = footerStyle
        
        return styles
    }

    /**
     * ایجاد هدر گزارش
     */
    private fun createHeader(
        sheet: Sheet,
        styles: Map<String, CellStyle>,
        startRow: Int,
        deviceInfo: DeviceInfo
    ): Int {
        var currentRow = startRow
        
        // عنوان اصلی
        val titleRow = sheet.createRow(currentRow++)
        titleRow.heightInPoints = 30f
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("📱 گزارش کامل دستگاه - ${Build.MANUFACTURER} ${Build.MODEL}")
        titleCell.cellStyle = styles["header"]
        sheet.addMergedRegion(CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1))
        
        // تاریخ تولید
        val dateRow = sheet.createRow(currentRow++)
        val dateCell = dateRow.createCell(0)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        dateCell.setCellValue("📅 تاریخ تولید: ${dateFormat.format(Date())}")
        dateCell.cellStyle = styles["footer"]
        sheet.addMergedRegion(CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1))
        
        return currentRow
    }

    /**
     * ایجاد خلاصه دستگاه
     */
    private fun createDeviceOverview(
        sheet: Sheet,
        styles: Map<String, CellStyle>,
        startRow: Int,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): Int {
        var currentRow = startRow
        
        // عنوان بخش
        val sectionRow = sheet.createRow(currentRow++)
        sectionRow.heightInPoints = 25f
        val sectionCell = sectionRow.createCell(0)
        sectionCell.setCellValue("🔍 خلاصه دستگاه")
        sectionCell.cellStyle = styles["category"]
        sheet.addMergedRegion(CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1))
        
        // اطلاعات خلاصه
        val overviewData = listOf(
            "برند" to Build.MANUFACTURER,
            "مدل" to Build.MODEL,
            "نسخه اندروید" to deviceInfo.system.androidVersion,
            "سطح باتری" to "${batteryInfo.level}%",
            "وضعیت باتری" to batteryInfo.status,
            "دمای باتری" to batteryInfo.temperature
        )
        
        overviewData.forEach { (label, value) ->
            val row = sheet.createRow(currentRow++)
            
            val labelCell = row.createCell(0)
            labelCell.setCellValue(label)
            labelCell.cellStyle = styles["label"]
            
            val valueCell = row.createCell(1)
            valueCell.setCellValue(value)
            valueCell.cellStyle = styles["value"]
        }
        
        return currentRow
    }

    /**
     * ایجاد بخش دسته‌بندی اطلاعات
     */
    private fun createCategorySection(
        context: Context,
        sheet: Sheet,
        styles: Map<String, CellStyle>,
        startRow: Int,
        category: InfoCategory,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): Int {
        var currentRow = startRow
        
        // عنوان دسته
        val categoryRow = sheet.createRow(currentRow++)
        categoryRow.heightInPoints = 25f
        val categoryCell = categoryRow.createCell(0)
        categoryCell.setCellValue("${getCategoryIcon(category)} ${category.getTitle(context)}")
        categoryCell.cellStyle = styles["category"]
        sheet.addMergedRegion(CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1))
        
        // داده‌های دسته
        val categoryData = ReportFormatter.getCategoryData(context, category, deviceInfo, batteryInfo)
        
        categoryData.forEach { (label, value) ->
            if (value.isNotEmpty()) {
                val row = sheet.createRow(currentRow++)
                
                val labelCell = row.createCell(0)
                labelCell.setCellValue(label)
                labelCell.cellStyle = styles["label"]
                
                val valueCell = row.createCell(1)
                valueCell.setCellValue(value)
                valueCell.cellStyle = styles["value"]
            } else if (label.startsWith("---")) {
                // زیرعنوان
                val subRow = sheet.createRow(currentRow++)
                val subCell = subRow.createCell(0)
                subCell.setCellValue(label.replace("---", "").trim())
                subCell.cellStyle = styles["category"]
                sheet.addMergedRegion(CellRangeAddress(currentRow - 1, currentRow - 1, 0, 1))
            }
        }
        
        return currentRow
    }

    /**
     * ایجاد فوتر گزارش
     */
    private fun createFooter(
        sheet: Sheet,
        styles: Map<String, CellStyle>,
        startRow: Int
    ) {
        val footerRow = sheet.createRow(startRow + 2)
        val footerCell = footerRow.createCell(0)
        footerCell.setCellValue("🚀 تولید شده با اپلیکیشن کاوش - تشخیص و بررسی دستگاه")
        footerCell.cellStyle = styles["footer"]
        sheet.addMergedRegion(CellRangeAddress(startRow + 2, startRow + 2, 0, 1))
    }

    /**
     * دریافت آیکون مناسب برای هر دسته
     */
    private fun getCategoryIcon(category: InfoCategory): String {
        return when (category) {
            InfoCategory.SOC -> "🧠"
            InfoCategory.DEVICE -> "📱"
            InfoCategory.SYSTEM -> "⚙️"
            InfoCategory.BATTERY -> "🔋"
            InfoCategory.SENSORS -> "🔬"
            InfoCategory.THERMAL -> "🌡️"
            InfoCategory.NETWORK -> "📡"
            InfoCategory.CAMERA -> "📷"
            InfoCategory.SIM -> "📶"
            InfoCategory.APPS -> "📦"
        }
    }
}
