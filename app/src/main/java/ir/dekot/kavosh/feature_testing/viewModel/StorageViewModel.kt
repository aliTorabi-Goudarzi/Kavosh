package ir.dekot.kavosh.feature_testing.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.model.repository.TestingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val testingRepository: TestingRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    // --- State های تست سرعت حافظه ---
    private val _isStorageTesting = MutableStateFlow(false)
    val isStorageTesting: StateFlow<Boolean> = _isStorageTesting.asStateFlow()

    private val _storageTestProgress = MutableStateFlow(0f)
    val storageTestProgress: StateFlow<Float> = _storageTestProgress.asStateFlow()

    private val _writeSpeed = MutableStateFlow("N/A")
    val writeSpeed: StateFlow<String> = _writeSpeed.asStateFlow()

    private val _readSpeed = MutableStateFlow("N/A")
    val readSpeed: StateFlow<String> = _readSpeed.asStateFlow()

    /**
     * تست سرعت خواندن و نوشتن حافظه داخلی را آغاز می‌کند.
     * این تابع وضعیت‌های مربوط به UI را برای نمایش پیشرفت و نتایج به‌روز می‌کند.
     */
    fun startStorageSpeedTest() {
        if (_isStorageTesting.value) return // جلوگیری از اجرای همزمان تست

        viewModelScope.launch(Dispatchers.IO) {
            _isStorageTesting.value = true
            _writeSpeed.value = context.getString(R.string.testing)
            _readSpeed.value = context.getString(R.string.testing)
            _storageTestProgress.value = 0f

            try {
                val result = testingRepository.performStorageSpeedTest { progress ->
                    // به‌روزرسانی نوار پیشرفت باید در ترد اصلی انجام شود
                    viewModelScope.launch(Dispatchers.Main) {
                        _storageTestProgress.value = progress
                    }
                }
                _writeSpeed.value = result.first
                _readSpeed.value = result.second
            } catch (e: Exception) {
                e.printStackTrace()
                _writeSpeed.value = context.getString(R.string.label_error)
                _readSpeed.value = context.getString(R.string.label_error)
            } finally {
                _isStorageTesting.value = false
                _storageTestProgress.value = 0f
            }
        }
    }
}