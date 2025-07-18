package ir.dekot.kavosh.feature_testing.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.CpuInfo

import ir.dekot.kavosh.feature_deviceInfo.model.repository.HardwareRepository
import ir.dekot.kavosh.feature_testing.CpuStresser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CpuStressTestViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository
) : ViewModel() {

    private val cpuStresser = CpuStresser()

    // وضعیت تست (روشن یا خاموش)
    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    // اطلاعات CPU که تغییر نمی‌کنند
    private val _cpuInfo = MutableStateFlow(hardwareRepository.getCpuInfo())
    val cpuInfo: StateFlow<CpuInfo> = _cpuInfo.asStateFlow()

    // فرکانس لحظه‌ای هسته‌ها
    private val _liveFrequencies = MutableStateFlow<List<String>>(emptyList())
    val liveFrequencies: StateFlow<List<String>> = _liveFrequencies.asStateFlow()

    private var pollingJob: Job? = null

    /**
     * رویداد کلیک روی دکمه شروع/پایان تست را مدیریت می‌کند.
     */
    fun onTestToggle() {
        _isTesting.value = !_isTesting.value
        if (_isTesting.value) {
            startTest()
        } else {
            stopTest()
        }
    }

    private fun startTest() {
        // شروع پردازش سنگین روی تمام هسته‌ها
        cpuStresser.start(_cpuInfo.value.coreCount)
        // شروع دریافت فرکانس لحظه‌ای هسته‌ها
        startPolling()
    }

    private fun stopTest() {
        cpuStresser.stop()
        stopPolling()
    }

    private fun startPolling() {
        stopPolling() // ابتدا job قبلی را متوقف کن
        pollingJob = viewModelScope.launch {
            while (isActive) {
                _liveFrequencies.value = hardwareRepository.getLiveCpuFrequencies()
                delay(1000) // هر یک ثانیه یک بار
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        // اطمینان از توقف تست در زمان بسته شدن ViewModel
        stopTest()
    }
}