package ir.dekot.kavosh.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.util.report.PdfGenerator
import ir.dekot.kavosh.util.report.ReportFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: DeviceInfoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- State های مربوط به خروجی گرفتن ---
    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult = _exportResult.asSharedFlow()

    private val _exportRequest = MutableSharedFlow<ExportFormat>()
    val exportRequest = _exportRequest.asSharedFlow()

    var pendingExportFormat: ExportFormat? = null
        private set

    /**
     * رویدادی برای شروع فرآیند خروجی گرفتن ارسال می‌کند.
     * این رویداد توسط MainActivity دریافت شده و پنجره انتخاب فایل را نمایش می‌دهد.
     * @param format فرمت فایل مورد نظر (TXT یا PDF).
     */
    fun onExportRequested(format: ExportFormat) {
        viewModelScope.launch {
            pendingExportFormat = format
            _exportRequest.emit(format)
        }
    }

    /**
     * پس از انتخاب مسیر توسط کاربر، این تابع فایل گزارش را تولید و ذخیره می‌کند.
     * @param uri مسیر (Uri) انتخاب شده توسط کاربر.
     * @param format فرمت فایل مورد نظر.
     * @param deviceInfo اطلاعات کامل دستگاه که باید در گزارش نوشته شود.
     */
    fun performExport(uri: Uri, format: ExportFormat, deviceInfo: DeviceInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // دریافت آخرین اطلاعات باتری برای گزارش
                val currentBatteryInfo = repository.getInitialBatteryInfo()
                    ?: throw IllegalStateException("Battery info not available")

                context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                    FileOutputStream(pfd.fileDescriptor).use { fos ->
                        when (format) {
                            ExportFormat.TXT -> {
                                val fullReportText = ReportFormatter.formatFullReport(context, deviceInfo, currentBatteryInfo)
                                fos.write(fullReportText.toByteArray())
                            }
                            ExportFormat.PDF -> {
                                PdfGenerator.writeStyledPdf(context, fos, deviceInfo, currentBatteryInfo)
                            }
                        }
                    }
                }
                _exportResult.emit(ExportResult.Success(context.getString(R.string.file_exported_successfully)))
            } catch (e: Exception) {
                e.printStackTrace()
                _exportResult.emit(ExportResult.Failure(context.getString(R.string.file_export_failed)))
            } finally {
                pendingExportFormat = null
            }
        }
    }
}