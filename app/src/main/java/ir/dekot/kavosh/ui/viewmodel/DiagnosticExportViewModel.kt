package ir.dekot.kavosh.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.diagnostic.*
import ir.dekot.kavosh.util.report.DiagnosticReportFormatter
import ir.dekot.kavosh.util.report.DiagnosticPdfGenerator
import ir.dekot.kavosh.ui.viewmodel.ExportResult
import ir.dekot.kavosh.ui.viewmodel.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * ViewModel برای مدیریت خروجی گزارش‌های تشخیصی
 */
@HiltViewModel
class DiagnosticExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    // نتیجه خروجی گزارش
    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult: SharedFlow<ExportResult> = _exportResult.asSharedFlow()

    // درخواست انتخاب فایل
    private val _filePickerRequest = MutableSharedFlow<ExportFormat>()
    val filePickerRequest: SharedFlow<ExportFormat> = _filePickerRequest.asSharedFlow()

    // درخواست فعلی در انتظار
    var pendingExportRequest: ExportRequest? = null
        private set

    /**
     * شروع فرآیند خروجی گزارش
     */
    fun startExport(request: ExportRequest) {
        viewModelScope.launch {
            pendingExportRequest = request
            val format = when (request) {
                is ExportRequest.HealthCheck -> request.format
                is ExportRequest.PerformanceScore -> request.format
                is ExportRequest.DeviceComparison -> request.format
                is ExportRequest.HealthCheckHistory -> request.format
                is ExportRequest.PerformanceScoreHistory -> request.format
                is ExportRequest.DeviceComparisonHistory -> request.format
            }
            _filePickerRequest.emit(format)
        }
    }

    /**
     * انجام خروجی گزارش پس از انتخاب فایل
     */
    fun performExport(uri: Uri) {
        val request = pendingExportRequest ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                    FileOutputStream(pfd.fileDescriptor).use { fos ->
                        val format = when (request) {
                            is ExportRequest.HealthCheck -> request.format
                            is ExportRequest.PerformanceScore -> request.format
                            is ExportRequest.DeviceComparison -> request.format
                            is ExportRequest.HealthCheckHistory -> request.format
                            is ExportRequest.PerformanceScoreHistory -> request.format
                            is ExportRequest.DeviceComparisonHistory -> request.format
                        }

                        when (format) {
                            ExportFormat.TXT -> {
                                val reportText = generateTextReport(request)
                                fos.write(reportText.toByteArray())
                            }
                            ExportFormat.PDF -> {
                                generatePdfReport(request, fos)
                            }
                            ExportFormat.JSON -> {
                                val jsonReport = generateJsonReport(request)
                                fos.write(jsonReport.toByteArray())
                            }
                        }
                    }
                }
                
                _exportResult.emit(ExportResult.Success(context.getString(R.string.file_exported_successfully)))
                
            } catch (e: Exception) {
                e.printStackTrace()
                _exportResult.emit(ExportResult.Failure(context.getString(R.string.file_export_failed)))
            } finally {
                pendingExportRequest = null
            }
        }
    }

    /**
     * اشتراک‌گذاری سریع گزارش
     */
    fun quickShare(request: ExportRequest) {
        viewModelScope.launch {
            try {
                val reportText = generateTextReport(request)
                val title = getReportTitle(request)
                
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, reportText)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                }

                val chooserIntent = android.content.Intent.createChooser(shareIntent, "اشتراک‌گذاری گزارش از طریق...")
                chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

                _exportResult.emit(ExportResult.Success("گزارش با موفقیت به اشتراک گذاشته شد"))

            } catch (e: Exception) {
                _exportResult.emit(ExportResult.Failure("خطا در اشتراک‌گذاری: ${e.message}"))
            }
        }
    }

    /**
     * تولید گزارش متنی
     */
    private fun generateTextReport(request: ExportRequest): String {
        return when (request) {
            is ExportRequest.HealthCheck -> 
                DiagnosticReportFormatter.formatHealthCheckReport(context, request.result)
            is ExportRequest.PerformanceScore -> 
                DiagnosticReportFormatter.formatPerformanceScoreReport(context, request.score)
            is ExportRequest.DeviceComparison -> 
                DiagnosticReportFormatter.formatDeviceComparisonReport(context, request.comparison)
            is ExportRequest.HealthCheckHistory -> 
                generateHealthCheckHistoryReport(request.summary)
            is ExportRequest.PerformanceScoreHistory -> 
                DiagnosticReportFormatter.formatPerformanceScoreReport(context, request.score)
            is ExportRequest.DeviceComparisonHistory -> 
                DiagnosticReportFormatter.formatDeviceComparisonReport(context, request.comparison)
        }
    }

    /**
     * تولید گزارش JSON
     */
    private fun generateJsonReport(request: ExportRequest): String {
        return when (request) {
            is ExportRequest.HealthCheck -> 
                DiagnosticReportFormatter.formatHealthCheckJsonReport(request.result)
            is ExportRequest.PerformanceScore -> 
                DiagnosticReportFormatter.formatPerformanceScoreJsonReport(request.score)
            is ExportRequest.DeviceComparison -> 
                DiagnosticReportFormatter.formatDeviceComparisonJsonReport(request.comparison)
            is ExportRequest.HealthCheckHistory -> 
                generateHealthCheckHistoryJsonReport(request.summary)
            is ExportRequest.PerformanceScoreHistory -> 
                DiagnosticReportFormatter.formatPerformanceScoreJsonReport(request.score)
            is ExportRequest.DeviceComparisonHistory -> 
                DiagnosticReportFormatter.formatDeviceComparisonJsonReport(request.comparison)
        }
    }

    /**
     * تولید گزارش PDF
     */
    private fun generatePdfReport(request: ExportRequest, fos: FileOutputStream) {
        val reportText = generateTextReport(request)
        DiagnosticPdfGenerator.generatePdf(context, fos, reportText, getReportTitle(request))
    }

    /**
     * تولید گزارش کامل تاریخچه بررسی سلامت
     */
    private fun generateHealthCheckHistoryReport(summary: HealthCheckSummary): String {
        val builder = StringBuilder()

        builder.appendLine("=".repeat(50))
        builder.appendLine("گزارش کامل بررسی سلامت - تاریخچه")
        builder.appendLine("=".repeat(50))
        builder.appendLine()

        // اطلاعات کلی
        builder.appendLine("📊 اطلاعات کلی:")
        builder.appendLine("تاریخ تست: ${java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(summary.timestamp))}")
        builder.appendLine("امتیاز کلی: ${summary.overallScore}/100")
        builder.appendLine("وضعیت: ${getHealthStatusText(summary.overallStatus)}")
        builder.appendLine("نام دستگاه: ${summary.deviceName}")
        builder.appendLine("نسخه اندروید: ${summary.androidVersion}")
        builder.appendLine("مدت زمان تست: ${summary.testDuration / 1000} ثانیه")
        builder.appendLine("تعداد مسائل بحرانی: ${summary.criticalIssuesCount}")
        builder.appendLine("تعداد هشدارها: ${summary.warningsCount}")
        builder.appendLine()

        // جزئیات بررسی‌ها
        if (summary.checks.isNotEmpty()) {
            builder.appendLine("📋 جزئیات بررسی‌ها:")
            builder.appendLine("-".repeat(30))

            summary.checks.forEach { check ->
                builder.appendLine()
                builder.appendLine("🔍 ${check.name}")
                builder.appendLine("   امتیاز: ${check.score}/100")
                builder.appendLine("   وضعیت: ${getHealthStatusText(check.status)}")
                builder.appendLine("   توضیحات: ${check.description}")
                check.details?.let { details ->
                    builder.appendLine("   جزئیات: $details")
                }
                check.recommendation?.let { rec ->
                    builder.appendLine("   💡 توصیه: $rec")
                }
            }
        }

        // توصیه‌های کلی
        if (summary.recommendations.isNotEmpty()) {
            builder.appendLine()
            builder.appendLine("💡 توصیه‌های کلی:")
            builder.appendLine("-".repeat(20))
            summary.recommendations.forEachIndexed { index, recommendation ->
                builder.appendLine("${index + 1}. $recommendation")
            }
        }

        builder.appendLine()
        builder.appendLine("=".repeat(50))
        builder.appendLine("گزارش تولید شده توسط کاوش - ${java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")

        return builder.toString()
    }

    /**
     * دریافت متن وضعیت سلامت
     */
    private fun getHealthStatusText(status: HealthStatus): String = when (status) {
        HealthStatus.EXCELLENT -> "عالی"
        HealthStatus.GOOD -> "خوب"
        HealthStatus.FAIR -> "متوسط"
        HealthStatus.POOR -> "ضعیف"
        HealthStatus.CRITICAL -> "بحرانی"
    }

    /**
     * تولید گزارش JSON کامل تاریخچه بررسی سلامت
     */
    private fun generateHealthCheckHistoryJsonReport(summary: HealthCheckSummary): String {
        val jsonObject = kotlinx.serialization.json.buildJsonObject {
            put("report_type", "health_check_history")
            put("timestamp", System.currentTimeMillis())
            put("test_date", summary.timestamp)
            put("overall_score", summary.overallScore)
            put("overall_status", summary.overallStatus.name)
            put("device_name", summary.deviceName)
            put("android_version", summary.androidVersion)
            put("test_duration", summary.testDuration)
            put("critical_issues_count", summary.criticalIssuesCount)
            put("warnings_count", summary.warningsCount)

            // جزئیات بررسی‌ها
            put("checks", kotlinx.serialization.json.buildJsonObject {
                summary.checks.forEach { check ->
                    put(check.category.name.lowercase(), kotlinx.serialization.json.buildJsonObject {
                        put("name", check.name)
                        put("score", check.score)
                        put("status", check.status.name)
                        put("description", check.description)
                        check.details?.let { put("details", it) }
                        check.recommendation?.let { put("recommendation", it) }
                    })
                }
            })

            // توصیه‌های کلی
            put("recommendations", kotlinx.serialization.json.buildJsonObject {
                summary.recommendations.forEachIndexed { index, rec ->
                    put("recommendation_${index + 1}", rec)
                }
            })
        }

        return kotlinx.serialization.json.Json { prettyPrint = true }.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            jsonObject
        )
    }

    /**
     * دریافت عنوان گزارش
     */
    private fun getReportTitle(request: ExportRequest): String {
        return when (request) {
            is ExportRequest.HealthCheck -> context.getString(R.string.health_check_title)
            is ExportRequest.PerformanceScore -> context.getString(R.string.performance_score_title)
            is ExportRequest.DeviceComparison -> context.getString(R.string.device_comparison_title)
            is ExportRequest.HealthCheckHistory -> "تاریخچه بررسی سلامت"
            is ExportRequest.PerformanceScoreHistory -> "تاریخچه امتیاز عملکرد"
            is ExportRequest.DeviceComparisonHistory -> "تاریخچه مقایسه دستگاه"
        }
    }
}


