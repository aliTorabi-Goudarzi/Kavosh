package ir.dekot.kavosh.util.report

import android.content.Context
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.ui.viewmodel.getTitle
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * کلاس تولید گزارش HTML زیبا و قابل مشاهده در مرورگر
 */
object HtmlReportGenerator {

    /**
     * تولید گزارش HTML کامل
     */
    fun generateHtmlReport(
        context: Context,
        outputStream: OutputStream,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ) {
        val htmlContent = buildHtmlContent(context, deviceInfo, batteryInfo)
        outputStream.write(htmlContent.toByteArray(Charsets.UTF_8))
    }

    /**
     * ساخت محتوای HTML
     */
    private fun buildHtmlContent(
        context: Context,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        return """
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>گزارش کامل دستگاه - کاوش</title>
    <style>
        ${getHtmlStyles()}
    </style>
</head>
<body>
    <div class="container">
        <header class="header">
            <h1>📱 گزارش کامل دستگاه</h1>
            <p class="subtitle">تولید شده توسط اپلیکیشن کاوش</p>
            <p class="date">📅 تاریخ تولید: $currentDate</p>
        </header>

        <div class="device-overview">
            <h2>🔍 خلاصه دستگاه</h2>
            <div class="overview-grid">
                <div class="overview-item">
                    <span class="label">برند:</span>
                    <span class="value">${android.os.Build.MANUFACTURER}</span>
                </div>
                <div class="overview-item">
                    <span class="label">مدل:</span>
                    <span class="value">${android.os.Build.MODEL}</span>
                </div>
                <div class="overview-item">
                    <span class="label">اندروید:</span>
                    <span class="value">${deviceInfo.system.androidVersion}</span>
                </div>
                <div class="overview-item">
                    <span class="label">باتری:</span>
                    <span class="value">${batteryInfo.level}%</span>
                </div>
            </div>
        </div>

        ${generateCategorySections(context, deviceInfo, batteryInfo)}

        <footer class="footer">
            <p>🚀 تولید شده با اپلیکیشن کاوش - تشخیص و بررسی دستگاه</p>
            <p>📧 برای اطلاعات بیشتر با ما تماس بگیرید</p>
        </footer>
    </div>
</body>
</html>
        """.trimIndent()
    }

    /**
     * تولید بخش‌های مختلف اطلاعات
     */
    private fun generateCategorySections(
        context: Context,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): String {
        val sections = StringBuilder()
        
        InfoCategory.entries.forEach { category ->
            val categoryTitle = category.getTitle(context)
            val categoryData = ReportFormatter.getCategoryData(context, category, deviceInfo, batteryInfo)
            
            sections.append("""
                <section class="category-section">
                    <h2>${getCategoryIcon(category)} $categoryTitle</h2>
                    <div class="info-grid">
            """.trimIndent())
            
            categoryData.forEach { (label, value) ->
                if (value.isNotEmpty()) {
                    sections.append("""
                        <div class="info-item">
                            <span class="info-label">$label:</span>
                            <span class="info-value">$value</span>
                        </div>
                    """.trimIndent())
                } else if (label.startsWith("---")) {
                    sections.append("""
                        <div class="info-subsection">
                            <h3>${label.replace("---", "").trim()}</h3>
                        </div>
                    """.trimIndent())
                }
            }
            
            sections.append("""
                    </div>
                </section>
            """.trimIndent())
        }
        
        return sections.toString()
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

    /**
     * استایل‌های CSS برای HTML
     */
    private fun getHtmlStyles(): String {
        return """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                padding: 20px;
                color: #333;
            }
            
            .container {
                max-width: 1200px;
                margin: 0 auto;
                background: white;
                border-radius: 20px;
                box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                overflow: hidden;
            }
            
            .header {
                background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                color: white;
                padding: 40px;
                text-align: center;
            }
            
            .header h1 {
                font-size: 2.5em;
                margin-bottom: 10px;
                text-shadow: 0 2px 4px rgba(0,0,0,0.3);
            }
            
            .subtitle {
                font-size: 1.2em;
                opacity: 0.9;
                margin-bottom: 10px;
            }
            
            .date {
                font-size: 1em;
                opacity: 0.8;
            }
            
            .device-overview {
                padding: 30px;
                background: #f8f9fa;
                border-bottom: 1px solid #e9ecef;
            }
            
            .device-overview h2 {
                font-size: 1.8em;
                margin-bottom: 20px;
                color: #495057;
            }
            
            .overview-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                gap: 15px;
            }
            
            .overview-item {
                background: white;
                padding: 15px;
                border-radius: 10px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            
            .label {
                font-weight: 600;
                color: #6c757d;
            }
            
            .value {
                font-weight: 700;
                color: #495057;
            }
            
            .category-section {
                padding: 30px;
                border-bottom: 1px solid #e9ecef;
            }
            
            .category-section:last-of-type {
                border-bottom: none;
            }
            
            .category-section h2 {
                font-size: 1.6em;
                margin-bottom: 20px;
                color: #495057;
                border-bottom: 2px solid #4facfe;
                padding-bottom: 10px;
            }
            
            .info-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                gap: 15px;
            }
            
            .info-item {
                background: #f8f9fa;
                padding: 12px 15px;
                border-radius: 8px;
                border-right: 4px solid #4facfe;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            
            .info-label {
                font-weight: 600;
                color: #6c757d;
                flex: 1;
            }
            
            .info-value {
                font-weight: 500;
                color: #495057;
                text-align: left;
                max-width: 60%;
                word-break: break-word;
            }
            
            .info-subsection {
                grid-column: 1 / -1;
                margin: 20px 0 10px 0;
            }
            
            .info-subsection h3 {
                font-size: 1.2em;
                color: #6c757d;
                border-bottom: 1px solid #dee2e6;
                padding-bottom: 5px;
            }
            
            .footer {
                background: #343a40;
                color: white;
                padding: 30px;
                text-align: center;
            }
            
            .footer p {
                margin-bottom: 10px;
                opacity: 0.9;
            }
            
            @media (max-width: 768px) {
                .container {
                    margin: 10px;
                    border-radius: 15px;
                }
                
                .header {
                    padding: 30px 20px;
                }
                
                .header h1 {
                    font-size: 2em;
                }
                
                .category-section {
                    padding: 20px;
                }
                
                .info-grid {
                    grid-template-columns: 1fr;
                }
                
                .overview-grid {
                    grid-template-columns: 1fr;
                }
            }
        """.trimIndent()
    }
}
