package ir.dekot.kavosh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.data.model.components.PingResult
import ir.dekot.kavosh.data.model.components.WifiScanResult
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkToolsViewModel @Inject constructor(
    private val repository: DeviceInfoRepository
) : ViewModel() {

    // --- State های اسکنر Wi-Fi ---
    private val _wifiScanResults = MutableStateFlow<List<WifiScanResult>>(emptyList())
    val wifiScanResults: StateFlow<List<WifiScanResult>> = _wifiScanResults.asStateFlow()

    private val _isScanningWifi = MutableStateFlow(false)
    val isScanningWifi: StateFlow<Boolean> = _isScanningWifi.asStateFlow()

    // --- State های ابزار پینگ ---
    private val _pingResult = MutableStateFlow(PingResult())
    val pingResult: StateFlow<PingResult> = _pingResult.asStateFlow()
    private var pingJob: Job? = null

    fun startWifiScan() {
        if (_isScanningWifi.value) return // جلوگیری از اسکن‌های همزمان

        viewModelScope.launch {
            _isScanningWifi.value = true
            _wifiScanResults.value = emptyList() // پاک کردن نتایج قبلی
            try {
                // فراخوانی تابع suspend
                val results = repository.getWifiScanResults()
                _wifiScanResults.value = results.sortedByDescending { it.level }
            } catch (e: Exception) {
                e.printStackTrace()
                // در آینده می‌توان پیام خطا به کاربر نشان داد
            } finally {
                // چه با موفقیت چه با خطا، حالت اسکن را به false برگردان
                _isScanningWifi.value = false
            }
        }
    }

    fun startPing(host: String) {
        if (_pingResult.value.isPinging) return

        pingJob?.cancel()
        _pingResult.value = PingResult(host = host, isPinging = true)

        pingJob = repository.pingHost(host)
            .onEach { line ->
                val currentLines = _pingResult.value.outputLines
                _pingResult.value = _pingResult.value.copy(
                    outputLines = currentLines + line
                )
            }
            .catch { e ->
                _pingResult.value = _pingResult.value.copy(
                    isPinging = false,
                    error = e.message ?: "An unknown error occurred."
                )
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            pingJob?.join()
            _pingResult.value = _pingResult.value.copy(isPinging = false)
        }
    }

    fun stopPing() {
        pingJob?.cancel()
        _pingResult.value = _pingResult.value.copy(isPinging = false)
    }
}