package ir.dekot.kavosh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import android.os.Build
import ir.dekot.kavosh.data.model.diagnostic.DeviceComparison
import ir.dekot.kavosh.data.model.diagnostic.HealthCheckResult
import ir.dekot.kavosh.data.model.diagnostic.HealthCheckSummary
import ir.dekot.kavosh.data.model.diagnostic.PerformanceScore
import ir.dekot.kavosh.data.source.DiagnosticDataSource
import ir.dekot.kavosh.data.source.SettingsDataSource
import ir.dekot.kavosh.ui.viewmodel.ExportFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel برای ابزارهای تشخیصی
 * مدیریت Health Check، Performance Score و Device Comparison
 */
@HiltViewModel
class DiagnosticViewModel @Inject constructor(
    private val diagnosticDataSource: DiagnosticDataSource,
    private val settingsDataSource: SettingsDataSource
) : ViewModel() {

    // Health Check
    private val _healthCheckResult = MutableStateFlow<HealthCheckResult?>(null)
    val healthCheckResult: StateFlow<HealthCheckResult?> = _healthCheckResult.asStateFlow()

    private val _isHealthCheckLoading = MutableStateFlow(false)
    val isHealthCheckLoading: StateFlow<Boolean> = _isHealthCheckLoading.asStateFlow()

    // Performance Score
    private val _performanceScore = MutableStateFlow<PerformanceScore?>(null)
    val performanceScore: StateFlow<PerformanceScore?> = _performanceScore.asStateFlow()

    private val _isPerformanceScoreLoading = MutableStateFlow(false)
    val isPerformanceScoreLoading: StateFlow<Boolean> = _isPerformanceScoreLoading.asStateFlow()

    // Device Comparison
    private val _deviceComparison = MutableStateFlow<DeviceComparison?>(null)
    val deviceComparison: StateFlow<DeviceComparison?> = _deviceComparison.asStateFlow()

    private val _isDeviceComparisonLoading = MutableStateFlow(false)
    val isDeviceComparisonLoading: StateFlow<Boolean> = _isDeviceComparisonLoading.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Test History
    private val _healthCheckHistory = MutableStateFlow<List<HealthCheckSummary>>(emptyList())
    val healthCheckHistory: StateFlow<List<HealthCheckSummary>> = _healthCheckHistory.asStateFlow()

    private val _performanceScoreHistory = MutableStateFlow<List<PerformanceScore>>(emptyList())
    val performanceScoreHistory: StateFlow<List<PerformanceScore>> = _performanceScoreHistory.asStateFlow()

    private val _deviceComparisonHistory = MutableStateFlow<List<DeviceComparison>>(emptyList())
    val deviceComparisonHistory: StateFlow<List<DeviceComparison>> = _deviceComparisonHistory.asStateFlow()

    // Export Events
    private val _exportRequest = MutableSharedFlow<ExportRequest>()
    val exportRequest: SharedFlow<ExportRequest> = _exportRequest.asSharedFlow()

    init {
        // بارگذاری تاریخچه ذخیره شده هنگام ایجاد ViewModel
        loadSavedHistory()
    }

    /**
     * بارگذاری تاریخچه ذخیره شده از SharedPreferences
     */
    private fun loadSavedHistory() {
        _healthCheckHistory.value = settingsDataSource.getHealthCheckHistory()
        _performanceScoreHistory.value = settingsDataSource.getPerformanceScoreHistory()
        _deviceComparisonHistory.value = settingsDataSource.getDeviceComparisonHistory()
    }

    /**
     * انجام بررسی سلامت دستگاه
     */
    fun performHealthCheck() {
        viewModelScope.launch {
            try {
                _isHealthCheckLoading.value = true
                _errorMessage.value = null
                
                val result = diagnosticDataSource.performHealthCheck()
                _healthCheckResult.value = result

                // اضافه کردن به تاریخچه
                val summary = HealthCheckSummary(
                    timestamp = result.lastCheckTime,
                    overallScore = result.overallScore,
                    overallStatus = result.overallStatus,
                    deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                    androidVersion = Build.VERSION.RELEASE
                )
                val currentHistory = _healthCheckHistory.value.toMutableList()
                currentHistory.add(0, summary) // اضافه کردن به ابتدای لیست
                if (currentHistory.size > 10) { // نگه داشتن حداکثر 10 تست
                    currentHistory.removeAt(currentHistory.size - 1)
                }
                _healthCheckHistory.value = currentHistory

                // ذخیره در SharedPreferences
                settingsDataSource.saveHealthCheckHistory(currentHistory)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to perform health check: ${e.message}"
            } finally {
                _isHealthCheckLoading.value = false
            }
        }
    }

    /**
     * محاسبه امتیاز عملکرد
     */
    fun calculatePerformanceScore() {
        viewModelScope.launch {
            try {
                _isPerformanceScoreLoading.value = true
                _errorMessage.value = null
                
                val result = diagnosticDataSource.calculatePerformanceScore()
                _performanceScore.value = result

                // اضافه کردن به تاریخچه
                val currentHistory = _performanceScoreHistory.value.toMutableList()
                currentHistory.add(0, result)
                if (currentHistory.size > 10) {
                    currentHistory.removeAt(currentHistory.size - 1)
                }
                _performanceScoreHistory.value = currentHistory

                // ذخیره در SharedPreferences
                settingsDataSource.savePerformanceScoreHistory(currentHistory)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to calculate performance score: ${e.message}"
            } finally {
                _isPerformanceScoreLoading.value = false
            }
        }
    }

    /**
     * مقایسه دستگاه
     */
    fun compareDevice() {
        viewModelScope.launch {
            try {
                _isDeviceComparisonLoading.value = true
                _errorMessage.value = null
                
                val result = diagnosticDataSource.compareDevice()
                _deviceComparison.value = result

                // اضافه کردن به تاریخچه
                val currentHistory = _deviceComparisonHistory.value.toMutableList()
                currentHistory.add(0, result)
                if (currentHistory.size > 10) {
                    currentHistory.removeAt(currentHistory.size - 1)
                }
                _deviceComparisonHistory.value = currentHistory

                // ذخیره در SharedPreferences
                settingsDataSource.saveDeviceComparisonHistory(currentHistory)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to compare device: ${e.message}"
            } finally {
                _isDeviceComparisonLoading.value = false
            }
        }
    }

    /**
     * پاک کردن پیام خطا
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * تازه‌سازی همه داده‌ها
     */
    fun refreshAll() {
        performHealthCheck()
        calculatePerformanceScore()
        compareDevice()
    }

    /**
     * پاک کردن کش داده‌ها
     */
    fun clearCache() {
        _healthCheckResult.value = null
        _performanceScore.value = null
        _deviceComparison.value = null
        _errorMessage.value = null
    }

    /**
     * درخواست خروجی گزارش بررسی سلامت
     */
    fun exportHealthCheckReport(format: ExportFormat) {
        viewModelScope.launch {
            _healthCheckResult.value?.let { result ->
                _exportRequest.emit(ExportRequest.HealthCheck(result, format))
            }
        }
    }

    /**
     * درخواست خروجی گزارش امتیاز عملکرد
     */
    fun exportPerformanceScoreReport(format: ExportFormat) {
        viewModelScope.launch {
            _performanceScore.value?.let { score ->
                _exportRequest.emit(ExportRequest.PerformanceScore(score, format))
            }
        }
    }

    /**
     * درخواست خروجی گزارش مقایسه دستگاه
     */
    fun exportDeviceComparisonReport(format: ExportFormat) {
        viewModelScope.launch {
            _deviceComparison.value?.let { comparison ->
                _exportRequest.emit(ExportRequest.DeviceComparison(comparison, format))
            }
        }
    }

    /**
     * درخواست خروجی گزارش تاریخچه بررسی سلامت
     */
    fun exportHealthCheckHistoryReport(summary: HealthCheckSummary, format: ExportFormat) {
        viewModelScope.launch {
            _exportRequest.emit(ExportRequest.HealthCheckHistory(summary, format))
        }
    }

    /**
     * درخواست خروجی گزارش تاریخچه امتیاز عملکرد
     */
    fun exportPerformanceScoreHistoryReport(score: PerformanceScore, format: ExportFormat) {
        viewModelScope.launch {
            _exportRequest.emit(ExportRequest.PerformanceScoreHistory(score, format))
        }
    }

    /**
     * درخواست خروجی گزارش تاریخچه مقایسه دستگاه
     */
    fun exportDeviceComparisonHistoryReport(comparison: DeviceComparison, format: ExportFormat) {
        viewModelScope.launch {
            _exportRequest.emit(ExportRequest.DeviceComparisonHistory(comparison, format))
        }
    }
}

/**
 * انواع درخواست خروجی گزارش
 */
sealed class ExportRequest {
    data class HealthCheck(val result: HealthCheckResult, val format: ExportFormat) : ExportRequest()
    data class PerformanceScore(val score: ir.dekot.kavosh.data.model.diagnostic.PerformanceScore, val format: ExportFormat) : ExportRequest()
    data class DeviceComparison(val comparison: ir.dekot.kavosh.data.model.diagnostic.DeviceComparison, val format: ExportFormat) : ExportRequest()
    data class HealthCheckHistory(val summary: HealthCheckSummary, val format: ExportFormat) : ExportRequest()
    data class PerformanceScoreHistory(val score: ir.dekot.kavosh.data.model.diagnostic.PerformanceScore, val format: ExportFormat) : ExportRequest()
    data class DeviceComparisonHistory(val comparison: ir.dekot.kavosh.data.model.diagnostic.DeviceComparison, val format: ExportFormat) : ExportRequest()
}


