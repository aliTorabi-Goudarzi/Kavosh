package ir.dekot.kavosh.feature_export_and_sharing.viewModel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.model.repository.DeviceInfoRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.PowerRepository
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import ir.dekot.kavosh.feature_deviceInfo.viewModel.ExportResult
import ir.dekot.kavosh.feature_export_and_sharing.model.ExportFormat
import ir.dekot.kavosh.feature_export_and_sharing.model.HtmlReportGenerator
import ir.dekot.kavosh.feature_export_and_sharing.model.PdfGenerator
import ir.dekot.kavosh.feature_export_and_sharing.model.QrCodeGenerator
import ir.dekot.kavosh.feature_export_and_sharing.model.ReportFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val settingsRepository: SettingsRepository,
    private val powerRepository: PowerRepository,
    @param:ApplicationContext private val context: Context
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
                val currentBatteryInfo = powerRepository.getInitialBatteryInfo()
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
                            ExportFormat.JSON -> {
                                val jsonReport = ReportFormatter.formatJsonReport(deviceInfo, currentBatteryInfo)
                                fos.write(jsonReport.toByteArray())
                            }
                            ExportFormat.HTML -> {
                                HtmlReportGenerator.generateHtmlReport(context, fos, deviceInfo, currentBatteryInfo)
                            }
                            ExportFormat.QR_CODE -> {
                                val qrBitmap = QrCodeGenerator.generateQuickShareQrCode(context, deviceInfo, currentBatteryInfo)
                                QrCodeGenerator.saveQrCodeAsPng(qrBitmap, fos)
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

    /**
     * اشتراک‌گذاری سریع اطلاعات پایه دستگاه
     */
    fun onQuickShareRequested() {
        viewModelScope.launch {
            try {
                // استفاده از کش یا دریافت اطلاعات پایه
                val deviceInfo = settingsRepository.getDeviceInfoCache() ?: deviceInfoRepository.getBasicDeviceInfo()
                val batteryInfo = powerRepository.getCurrentBatteryInfo()

                // ایجاد متن خلاصه برای اشتراک‌گذاری سریع
                val quickInfo = buildString {
                    appendLine("📱 اطلاعات دستگاه")
                    appendLine("━━━━━━━━━━━━━━━━━━━━")
                    appendLine("🔧 پردازنده: ${deviceInfo.cpu.model}")
                    appendLine("🎮 پردازنده گرافیکی: ${deviceInfo.gpu.model}")
                    appendLine("💾 حافظه RAM: ${deviceInfo.ram.total}")
                    appendLine("📱 سیستم‌عامل: Android ${deviceInfo.system.androidVersion}")
                    appendLine("🔋 باتری: ${batteryInfo.level}% (${batteryInfo.status})")
                    appendLine("━━━━━━━━━━━━━━━━━━━━")
                    appendLine("📊 تولید شده توسط کاوش")
                }

                // استفاده از Intent برای اشتراک‌گذاری
                val shareIntent = Intent().apply {
                    setAction(Intent.ACTION_SEND)
                    setType("text/plain")
                    putExtra(Intent.EXTRA_TEXT, quickInfo)
                    putExtra(Intent.EXTRA_SUBJECT, "اطلاعات دستگاه")
                }

                val chooserIntent = Intent.createChooser(shareIntent, "اشتراک‌گذاری از طریق...")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

                _exportResult.emit(ExportResult.Success("اطلاعات با موفقیت به اشتراک گذاشته شد"))

            } catch (e: Exception) {
                _exportResult.emit(ExportResult.Failure("خطا در اشتراک‌گذاری: ${e.message}"))
            }
        }
    }

    /**
     * اشتراک‌گذاری QR Code
     */
    fun onQrCodeShareRequested() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deviceInfo = settingsRepository.getDeviceInfoCache() ?: deviceInfoRepository.getBasicDeviceInfo()
                val batteryInfo = powerRepository.getCurrentBatteryInfo()

                // تولید QR Code برای اشتراک‌گذاری آنلاین
                val shareableQrBitmap = QrCodeGenerator.generateShareableQrCode(context, deviceInfo, batteryInfo)

                // ذخیره موقت QR Code
                val tempFile = File(context.cacheDir, "qr_share_${System.currentTimeMillis()}.png")
                val fos = FileOutputStream(tempFile)
                QrCodeGenerator.saveQrCodeAsPng(shareableQrBitmap, fos)
                fos.close()

                // تولید URI برای اشتراک‌گذاری
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )

                // Intent اشتراک‌گذاری تصویر
                val shareIntent = Intent().apply {
                    setAction(Intent.ACTION_SEND)
                    setType("image/png")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "QR Code اطلاعات دستگاه - تولید شده با اپلیکیشن کاوش")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooserIntent = Intent.createChooser(shareIntent, "اشتراک‌گذاری QR Code از طریق...")
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

                _exportResult.emit(ExportResult.Success("QR Code با موفقیت به اشتراک گذاشته شد"))

            } catch (e: Exception) {
                _exportResult.emit(ExportResult.Failure("خطا در اشتراک‌گذاری QR Code: ${e.message}"))
            }
        }
    }
}