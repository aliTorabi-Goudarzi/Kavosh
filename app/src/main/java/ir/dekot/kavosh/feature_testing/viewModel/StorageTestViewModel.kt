package ir.dekot.kavosh.feature_testing.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.feature_deviceInfo.model.repository.TestingRepository
import ir.dekot.kavosh.feature_testing.model.SpeedDataPoint
import ir.dekot.kavosh.feature_testing.model.StorageSpeedTestResult
import ir.dekot.kavosh.feature_testing.model.StorageTestStatus
import ir.dekot.kavosh.feature_testing.model.StorageTestSummary
import ir.dekot.kavosh.feature_testing.view.StorageTestState as UIStorageTestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel پیشرفته برای مدیریت تست سرعت حافظه
 * شامل مجوز کاربر، نمودار زنده، و تاریخچه تست‌ها
 */
@HiltViewModel
class StorageTestViewModel @Inject constructor(
    private val testingRepository: TestingRepository
) : ViewModel() {

    // وضعیت فعلی تست
    private val _testState = MutableStateFlow(UIStorageTestState.IDLE)
    val testState: StateFlow<UIStorageTestState> = _testState.asStateFlow()

    // سرعت خواندن
    private val _readSpeed = MutableStateFlow("--")
    val readSpeed: StateFlow<String> = _readSpeed.asStateFlow()

    // سرعت نوشتن
    private val _writeSpeed = MutableStateFlow("--")
    val writeSpeed: StateFlow<String> = _writeSpeed.asStateFlow()

    // پیشرفت تست (0.0 تا 1.0)
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    // --- State های جدید برای تست پیشرفته ---

    // نمایش دیالوگ مجوز
    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    // سرعت‌های فعلی برای نمودار زنده
    private val _currentWriteSpeed = MutableStateFlow(0.0)
    val currentWriteSpeed: StateFlow<Double> = _currentWriteSpeed.asStateFlow()

    private val _currentReadSpeed = MutableStateFlow(0.0)
    val currentReadSpeed: StateFlow<Double> = _currentReadSpeed.asStateFlow()

    // تاریخچه سرعت‌ها برای نمودار
    private val _writeSpeedHistory = MutableStateFlow<List<SpeedDataPoint>>(emptyList())
    val writeSpeedHistory: StateFlow<List<SpeedDataPoint>> = _writeSpeedHistory.asStateFlow()

    private val _readSpeedHistory = MutableStateFlow<List<SpeedDataPoint>>(emptyList())
    val readSpeedHistory: StateFlow<List<SpeedDataPoint>> = _readSpeedHistory.asStateFlow()

    // پیام وضعیت فعلی
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // تاریخچه تست‌ها
    private val _testHistory = MutableStateFlow<List<StorageTestSummary>>(emptyList())
    val testHistory: StateFlow<List<StorageTestSummary>> = _testHistory.asStateFlow()

    // نتیجه آخرین تست
    private val _lastTestResult = MutableStateFlow<StorageSpeedTestResult?>(null)
    val lastTestResult: StateFlow<StorageSpeedTestResult?> = _lastTestResult.asStateFlow()

    init {
        // بارگذاری تاریخچه تست‌ها
        loadTestHistory()
    }

    /**
     * بارگذاری تاریخچه تست‌ها از SharedPreferences
     */
    private fun loadTestHistory() {
        _testHistory.value = testingRepository.getStorageSpeedTestHistory()
    }

    /**
     * درخواست شروع تست - نمایش دیالوگ مجوز
     */
    fun requestStartTest() {
        _showPermissionDialog.value = true
    }

    /**
     * تأیید مجوز و شروع تست پیشرفته
     */
    fun grantPermissionAndStartTest() {
        viewModelScope.launch {
            // بستن دیالوگ فوری
            _showPermissionDialog.value = false

            // تأخیر کوتاه برای اطمینان از بسته شدن دیالوگ
            delay(100)

            // شروع تست
            startEnhancedTest()
        }
    }

    /**
     * رد مجوز
     */
    fun denyPermission() {
        _showPermissionDialog.value = false
    }

    /**
     * شروع تست پیشرفته سرعت حافظه
     * اجرا در background thread برای جلوگیری از منجمد شدن UI
     */
    private fun startEnhancedTest() {
        if (_testState.value == UIStorageTestState.TESTING) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // به‌روزرسانی UI در main thread
                withContext(Dispatchers.Main) {
                    _testState.value = UIStorageTestState.TESTING
                    _progress.value = 0f
                    _readSpeed.value = "--"
                    _writeSpeed.value = "--"
                    _currentWriteSpeed.value = 0.0
                    _currentReadSpeed.value = 0.0
                    _writeSpeedHistory.value = emptyList()
                    _readSpeedHistory.value = emptyList()
                }

                // شروع تست پیشرفته در background thread
                val result = testingRepository.performEnhancedStorageSpeedTest(
                    onProgress = { progress ->
                        // به‌روزرسانی progress در main thread
                        viewModelScope.launch(Dispatchers.Main) {
                            _progress.value = progress
                        }
                    },
                    onSpeedUpdate = { writeSpeed, readSpeed ->
                        // به‌روزرسانی سرعت‌ها در main thread
                        viewModelScope.launch(Dispatchers.Main) {
                            _currentWriteSpeed.value = writeSpeed
                            _currentReadSpeed.value = readSpeed
                            _writeSpeed.value = if (writeSpeed > 0) "${"%.1f".format(writeSpeed)} MB/s" else "--"
                            _readSpeed.value = if (readSpeed > 0) "${"%.1f".format(readSpeed)} MB/s" else "--"
                        }
                    },
                    onPhaseChange = { phase ->
                        // به‌روزرسانی پیام وضعیت در main thread
                        viewModelScope.launch(Dispatchers.Main) {
                            _statusMessage.value = phase
                        }
                    },
                    onSpeedHistoryUpdate = { writeHistory, readHistory ->
                        // به‌روزرسانی تاریخچه سرعت‌ها در main thread برای نمودار زنده
                        viewModelScope.launch(Dispatchers.Main) {
                            _writeSpeedHistory.value = writeHistory
                            _readSpeedHistory.value = readHistory
                        }
                    }
                )

                // به‌روزرسانی نتایج نهایی در main thread
                withContext(Dispatchers.Main) {
                    if (result.testStatus == StorageTestStatus.COMPLETED) {
                        // نمایش نتایج نهایی
                        _readSpeed.value = "${"%.1f".format(result.readSpeed)} MB/s"
                        _writeSpeed.value = "${"%.1f".format(result.writeSpeed)} MB/s"
                        _testState.value = UIStorageTestState.COMPLETED
                        _lastTestResult.value = result
                        _statusMessage.value = "تست با موفقیت تکمیل شد"

                        // اضافه کردن به تاریخچه
                        addToHistory(result)
                    } else {
                        _testState.value = UIStorageTestState.ERROR
                        _readSpeed.value = "خطا"
                        _writeSpeed.value = "خطا"
                        _statusMessage.value = result.errorMessage ?: "خطای ناشناخته"
                    }
                }

            } catch (e: Exception) {
                // مدیریت خطا در main thread
                withContext(Dispatchers.Main) {
                    _testState.value = UIStorageTestState.ERROR
                    _readSpeed.value = "خطا"
                    _writeSpeed.value = "خطا"
                    _statusMessage.value = e.message ?: "خطای ناشناخته"
                    _progress.value = 0f
                }
            }
        }
    }

    /**
     * اضافه کردن نتیجه تست به تاریخچه
     */
    private fun addToHistory(result: StorageSpeedTestResult) {
        val summary = StorageTestSummary(
            id = result.id,
            timestamp = result.timestamp,
            writeSpeed = result.writeSpeed,
            readSpeed = result.readSpeed,
            testDuration = result.testDuration,
            fileSizeBytes = 1024L * 1024L * 1024L // ۱ گیگابایت - مقدار ثابت برای نمایش صحیح
        )

        val currentHistory = _testHistory.value.toMutableList()
        currentHistory.add(0, summary) // اضافه کردن به ابتدای لیست

        // نگه داشتن حداکثر ۱۰ تست
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.size - 1)
        }

        _testHistory.value = currentHistory

        // ذخیره در SharedPreferences
        testingRepository.saveStorageSpeedTestHistory(currentHistory)
    }



    /**
     * شروع تست سرعت حافظه (متد قدیمی برای سازگاری)
     */
    fun startTest() {
        startEnhancedTest()
    }

    /**
     * بازنشانی تست
     */
    fun resetTest() {
        _testState.value = UIStorageTestState.IDLE
        _readSpeed.value = "--"
        _writeSpeed.value = "--"
        _progress.value = 0f
        _currentWriteSpeed.value = 0.0
        _currentReadSpeed.value = 0.0
        _writeSpeedHistory.value = emptyList()
        _readSpeedHistory.value = emptyList()
        _statusMessage.value = ""
        _lastTestResult.value = null
    }
}