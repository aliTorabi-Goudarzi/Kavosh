package ir.dekot.kavosh.ui.viewmodel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.domain.sensor.SensorHandler
import ir.dekot.kavosh.domain.sensor.SensorState
import ir.dekot.kavosh.ui.navigation.Screen
import ir.dekot.kavosh.util.formatSizeOrSpeed
import ir.dekot.kavosh.util.report.PdfGenerator
import ir.dekot.kavosh.util.report.ReportFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import javax.inject.Inject

// کلاس‌های ExportResult حذف نشده‌اند چون هنوز اینجا استفاده می‌شوند
sealed class ExportResult {
    data class Success(val message: String) : ExportResult()
    data class Failure(val message: String) : ExportResult()
}

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.R)
class DeviceInfoViewModel @Inject constructor(
    private val repository: DeviceInfoRepository,
    private val sensorHandler: SensorHandler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val sensorState: StateFlow<SensorState> = sensorHandler.sensorState

    // --- State های اصلی و مربوط به داده‌های دستگاه ---
    private var hasLoadedData = false
    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo = _deviceInfo.asStateFlow()

    private val _thermalDetails = MutableStateFlow<List<ThermalInfo>>(emptyList())
    val thermalDetails = _thermalDetails.asStateFlow()

    // --- State های مربوط به UI و ناوبری ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen = _currentScreen.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    private val _scanStatusText = MutableStateFlow("آماده برای اسکن...")
    val scanStatusText = _scanStatusText.asStateFlow()

    // --- State های داده‌های زنده ---
    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()
    private val _liveCpuFrequencies = MutableStateFlow<List<String>>(emptyList())
    val liveCpuFrequencies = _liveCpuFrequencies.asStateFlow()
    private val _liveGpuLoad = MutableStateFlow<Int?>(null)
    val liveGpuLoad = _liveGpuLoad.asStateFlow()
    private val _downloadSpeed = MutableStateFlow("0.0 KB/s")
    val downloadSpeed = _downloadSpeed.asStateFlow()
    private val _uploadSpeed = MutableStateFlow("0.0 KB/s")
    val uploadSpeed = _uploadSpeed.asStateFlow()

    // --- Job های مربوط به Polling ---
    private var socPollingJob: Job? = null
    private var batteryReceiver: BroadcastReceiver? = null
    private var networkPollingJob: Job? = null

    // --- State های تست سرعت حافظه ---
    private val _isStorageTesting = MutableStateFlow(false)
    val isStorageTesting: StateFlow<Boolean> = _isStorageTesting.asStateFlow()
    private val _storageTestProgress = MutableStateFlow(0f)
    val storageTestProgress: StateFlow<Float> = _storageTestProgress.asStateFlow()
    private val _writeSpeed = MutableStateFlow("N/A")
    val writeSpeed: StateFlow<String> = _writeSpeed.asStateFlow()
    private val _readSpeed = MutableStateFlow("N/A")
    val readSpeed: StateFlow<String> = _readSpeed.asStateFlow()

    // --- State های مربوط به خروجی گرفتن ---
    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult = _exportResult.asSharedFlow()
    private val _exportRequest = MutableSharedFlow<ExportFormat>()
    val exportRequest = _exportRequest.asSharedFlow()
    var pendingExportFormat: ExportFormat? = null
        private set

    init {
        // منطق داشبورد از اینجا حذف شد
        if (repository.isFirstLaunch()) {
            hasLoadedData = false
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSocPolling()
        unregisterBatteryReceiver()
        stopNetworkPolling()
        sensorHandler.stopListening()
    }

    // --- تمام توابع دیگر (ناوبری، اسکن، داده‌های زنده و ...) بدون تغییر باقی می‌مانند ---
    // ... (کدهای مربوط به ناوبری، اسکن، start/stop polling و غیره را اینجا کپی کنید)
    fun navigateToDetail(category: InfoCategory) {
        stopSocPolling()
        unregisterBatteryReceiver()
        stopNetworkPolling()
        when (category) {
            InfoCategory.THERMAL -> prepareThermalDetails()
            InfoCategory.SOC -> startSocPolling()
            InfoCategory.BATTERY -> registerBatteryReceiver()
            InfoCategory.NETWORK -> startNetworkPolling()
            else -> {
            }
        }
        _currentScreen.value = Screen.Detail(category)
    }

    fun navigateBack() {
        // توقف polling ها هنگام بازگشت به داشبورد
        stopSocPolling()
        unregisterBatteryReceiver()
        stopNetworkPolling()

        // هنگام بازگشت از صفحه ویرایش، دیگر نیازی به بارگذاری مجدد آیتم‌ها در این ViewModel نیست
        _currentScreen.value = Screen.Dashboard
    }

    fun navigateToSettings() {
        _currentScreen.value = Screen.Settings
    }

    fun navigateToAbout() {
        _currentScreen.value = Screen.About
    }

    fun navigateToEditDashboard() {
        _currentScreen.value = Screen.EditDashboard
    }

    fun navigateToSensorDetail(sensorType: Int) {
        _currentScreen.value = Screen.SensorDetail(sensorType)
    }

    // --- توابع مربوط به اسکن و بارگذاری داده ---

    fun loadDataForNonFirstLaunch(activity: Activity) {
        if (repository.isFirstLaunch() || hasLoadedData) return

        viewModelScope.launch {
            _isScanning.value = true
            try {
                _deviceInfo.value = fetchAllDeviceInfo(activity)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
                hasLoadedData = true
            }
        }
    }

    private suspend fun fetchAllDeviceInfo(activity: Activity): DeviceInfo {
        val cpuInfoJob = viewModelScope.async { repository.getCpuInfo() }
        val gpuInfoJob = viewModelScope.async { repository.getGpuInfo(activity) }
        val ramInfoJob = viewModelScope.async { repository.getRamInfo() }
        val storageInfoJob = viewModelScope.async { repository.getStorageInfo() }
        val systemInfoJob = viewModelScope.async { repository.getSystemInfo() }
        val sensorInfoJob = viewModelScope.async { repository.getSensorInfo(activity) }
        val cameraInfoJob = viewModelScope.async { repository.getCameraInfoList() }

        val displayInfo = repository.getDisplayInfo(activity)
        val thermalInfo = repository.getThermalInfo()
        val networkInfo = repository.getNetworkInfo()

        return DeviceInfo(
            cpu = cpuInfoJob.await(),
            gpu = gpuInfoJob.await(),
            ram = ramInfoJob.await(),
            storage = storageInfoJob.await(),
            system = systemInfoJob.await(),
            sensors = sensorInfoJob.await(),
            cameras = cameraInfoJob.await(),
            display = displayInfo,
            thermal = thermalInfo,
            network = networkInfo
        )
    }

    fun startScan(activity: Activity) {
        if (_isScanning.value) return

        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            val animationJob = launch {
                launch {
                    for (i in 1..100) {
                        delay(150)
                        _scanProgress.value = i / 100f
                    }
                }
                _scanStatusText.value = "در حال خواندن مشخصات دستگاه..."
                delay(5000)
                _scanStatusText.value = "دریافت اطلاعات از درایور ها..."
                delay(5000)
                _scanStatusText.value = "ثبت اطلاعات..."
            }

            val dataLoadingJob = launch {
                _deviceInfo.value = fetchAllDeviceInfo(activity)
            }

            animationJob.join()
            dataLoadingJob.join()
            repository.setFirstLaunchCompleted()
            hasLoadedData = true

            _currentScreen.value = Screen.Dashboard
            _isScanning.value = false
        }
    }

    // --- توابع مربوط به داده‌های زنده ---
    private fun startSocPolling() {
        stopSocPolling()
        socPollingJob = viewModelScope.launch {
            while (true) {
                _liveCpuFrequencies.value = repository.getLiveCpuFrequencies()
                _liveGpuLoad.value = repository.getGpuLoadPercentage()
                delay(1500)
            }
        }
    }

    private fun stopSocPolling() {
        socPollingJob?.cancel()
        socPollingJob = null
    }

    private fun startNetworkPolling() {
        stopNetworkPolling()
        networkPollingJob = viewModelScope.launch {
            var lastRxBytes = TrafficStats.getTotalRxBytes()
            var lastTxBytes = TrafficStats.getTotalTxBytes()

            while (isActive) {
                delay(2000)
                val currentRxBytes = TrafficStats.getTotalRxBytes()
                val currentTxBytes = TrafficStats.getTotalTxBytes()

                val rxSpeed = (currentRxBytes - lastRxBytes) / 2
                val txSpeed = (currentTxBytes - lastTxBytes) / 2

                _downloadSpeed.value = formatSizeOrSpeed(context, rxSpeed, perSecond = true)
                _uploadSpeed.value = formatSizeOrSpeed(context, txSpeed, perSecond = true)

                lastRxBytes = currentRxBytes
                lastTxBytes = currentTxBytes
            }
        }
    }

    private fun stopNetworkPolling() {
        networkPollingJob?.cancel()
        networkPollingJob = null
    }

    private fun registerBatteryReceiver() {
        if (batteryReceiver != null) return

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(null, filter)?.let { initialIntent ->
            _batteryInfo.value = repository.getBatteryInfo(initialIntent)
        }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    if (it.action == Intent.ACTION_BATTERY_CHANGED) {
                        _batteryInfo.value = repository.getBatteryInfo(it)
                    }
                }
            }
        }
        context.registerReceiver(batteryReceiver, filter)
    }

    private fun unregisterBatteryReceiver() {
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: IllegalArgumentException) {
            } finally {
                batteryReceiver = null
            }
        }
    }


    // --- توابع مربوط به سنسور ---
    fun registerSensorListener(sensorType: Int) {
        sensorHandler.startListening(sensorType)
    }

    fun unregisterSensorListener() {
        sensorHandler.stopListening()
    }

    // --- سایر توابع ---

    fun startStorageSpeedTest() {
        if (_isStorageTesting.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isStorageTesting.value = true
            _writeSpeed.value = context.getString(R.string.testing)
            _readSpeed.value = context.getString(R.string.testing)
            _storageTestProgress.value = 0f

            try {
                val result = repository.performStorageSpeedTest { progress ->
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

    private fun prepareThermalDetails() {
        val combinedList = mutableListOf<ThermalInfo>()
        repository.getInitialBatteryInfo()?.let { batteryData ->
            if (batteryData.temperature.isNotBlank()) {
                combinedList.add(
                    ThermalInfo(
                        type = context.getString(R.string.category_battery), // استفاده از منبع رشته
                        temperature = batteryData.temperature
                    )
                )
            }
        }
        combinedList.addAll(deviceInfo.value.thermal)
        _thermalDetails.value = combinedList
    }

    fun onExportRequested(format: ExportFormat) {
        viewModelScope.launch {
            pendingExportFormat = format
            _exportRequest.emit(format)
        }
    }

    fun performExport(uri: Uri, format: ExportFormat) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDeviceInfo = _deviceInfo.value
                val currentBatteryInfo = repository.getInitialBatteryInfo() ?: BatteryInfo()

                context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                    FileOutputStream(pfd.fileDescriptor).use { fos ->
                        when (format) {
                            ExportFormat.TXT -> {
                                val fullReportText = ReportFormatter.formatFullReport(context, currentDeviceInfo, currentBatteryInfo)
                                fos.write(fullReportText.toByteArray())
                            }
                            ExportFormat.PDF -> {
                                PdfGenerator.writeStyledPdf(context, fos, currentDeviceInfo, currentBatteryInfo)
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