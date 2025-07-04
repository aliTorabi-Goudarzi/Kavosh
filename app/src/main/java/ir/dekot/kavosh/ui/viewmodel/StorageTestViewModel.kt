package ir.dekot.kavosh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.ui.screen.storagetest.StorageTestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel برای مدیریت تست سرعت حافظه
 */
@HiltViewModel
class StorageTestViewModel @Inject constructor(
    private val repository: DeviceInfoRepository
) : ViewModel() {

    // وضعیت فعلی تست
    private val _testState = MutableStateFlow(StorageTestState.IDLE)
    val testState: StateFlow<StorageTestState> = _testState.asStateFlow()

    // سرعت خواندن
    private val _readSpeed = MutableStateFlow("--")
    val readSpeed: StateFlow<String> = _readSpeed.asStateFlow()

    // سرعت نوشتن
    private val _writeSpeed = MutableStateFlow("--")
    val writeSpeed: StateFlow<String> = _writeSpeed.asStateFlow()

    // پیشرفت تست (0.0 تا 1.0)
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    /**
     * شروع تست سرعت حافظه
     */
    suspend fun startTest() {
        if (_testState.value == StorageTestState.TESTING) return

        viewModelScope.launch {
            try {
                _testState.value = StorageTestState.TESTING
                _progress.value = 0f
                _readSpeed.value = "--"
                _writeSpeed.value = "--"

                // شروع تست با callback برای پیشرفت
                val result = repository.performStorageSpeedTest { progressValue ->
                    _progress.value = progressValue
                }

                // نمایش نتایج
                _readSpeed.value = result.first
                _writeSpeed.value = result.second
                _progress.value = 1f
                _testState.value = StorageTestState.COMPLETED

            } catch (_: Exception) {
                _testState.value = StorageTestState.ERROR
                _readSpeed.value = "خطا"
                _writeSpeed.value = "خطا"
                _progress.value = 0f
            }
        }
    }

    /**
     * بازنشانی تست
     */
    fun resetTest() {
        _testState.value = StorageTestState.IDLE
        _readSpeed.value = "--"
        _writeSpeed.value = "--"
        _progress.value = 0f
    }
}