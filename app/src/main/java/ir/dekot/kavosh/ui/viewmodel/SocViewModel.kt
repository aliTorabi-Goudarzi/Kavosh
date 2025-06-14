package ir.dekot.kavosh.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel // <-- انوتیشن برای شناسایی ViewModel توسط Hilt
@RequiresApi(Build.VERSION_CODES.R)
class SocViewModel @Inject constructor (private val repository: DeviceInfoRepository) : ViewModel() {

    private val _liveCpuFrequencies = MutableStateFlow<List<String>>(emptyList())
    val liveCpuFrequencies = _liveCpuFrequencies.asStateFlow()

    private val _liveGpuLoad = MutableStateFlow<Int?>(null)
    val liveGpuLoad = _liveGpuLoad.asStateFlow()

    private var socPollingJob: Job? = null

    fun startSocPolling() {
        stopSocPolling() // جلوگیری از اجرای همزمان چند جاب
        socPollingJob = viewModelScope.launch {
            while (true) {
                _liveCpuFrequencies.value = repository.getLiveCpuFrequencies()
                _liveGpuLoad.value = repository.getGpuLoadPercentage()
                delay(1500) // هر ۱.۵ ثانیه آپدیت کن
            }
        }
    }

    fun stopSocPolling() {
        socPollingJob?.cancel()
        socPollingJob = null
    }
}