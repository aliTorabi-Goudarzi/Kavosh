package ir.dekot.kavosh.ui.viewmodel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.AppInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.domain.sensor.SensorHandler
import ir.dekot.kavosh.domain.sensor.SensorState
import ir.dekot.kavosh.util.formatSizeOrSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

// کلاس‌های ExportResult حذف نشده‌اند چون هنوز اینجا استفاده می‌شوند
sealed class ExportResult {
    data class Success(val message: String) : ExportResult()
    data class Failure(val message: String) : ExportResult()
}

// یک enum برای مدیریت بهتر وضعیت لودینگ می‌سازیم
enum class AppsLoadingState { IDLE, LOADING, LOADED }

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.R)
class DeviceInfoViewModel @Inject constructor(
    private val repository: DeviceInfoRepository,
    private val sensorHandler: SensorHandler,
    @param:ApplicationContext private val context: Context
) : ViewModel() {


    // --- State های جدید و بهینه شده برای بخش برنامه‌ها ---
    private val _userApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val userApps: StateFlow<List<AppInfo>> = _userApps.asStateFlow()

    private val _systemApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val systemApps: StateFlow<List<AppInfo>> = _systemApps.asStateFlow()

    private val _appsLoadingState = MutableStateFlow(AppsLoadingState.IDLE)
    val appsLoadingState: StateFlow<AppsLoadingState> = _appsLoadingState.asStateFlow()


    // --- State های جدید برای بخش برنامه‌ها ---
    private val _appsList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appsList: StateFlow<List<AppInfo>> = _appsList.asStateFlow()

    private val _isAppsLoading = MutableStateFlow(false)
    val isAppsLoading: StateFlow<Boolean> = _isAppsLoading.asStateFlow()

    val sensorState: StateFlow<SensorState> = sensorHandler.sensorState

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo = _deviceInfo.asStateFlow()

    private val _thermalDetails = MutableStateFlow<List<ThermalInfo>>(emptyList())
    val thermalDetails = _thermalDetails.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    private val _scanStatusText = MutableStateFlow("آماده برای اسکن...")
    val scanStatusText = _scanStatusText.asStateFlow()

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

    private var socPollingJob: Job? = null
    private var batteryReceiver: BroadcastReceiver? = null
    private var networkPollingJob: Job? = null


    init {
        // فقط تلاش می‌کنیم کش را بارگذاری کنیم.
        if (!repository.isFirstLaunch()) {
            val cachedInfo = repository.getDeviceInfoCache()
            if (cachedInfo != null) {
                _deviceInfo.value = cachedInfo
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAllPolling()
        sensorHandler.stopListening()
    }

    /**
     * این تابع تمام کارهای زمان‌بر (polling) را متوقف می‌کند.
     * حالا از بیرون (توسط لایه UI) فراخوانی می‌شود.
     */
    fun stopAllPolling() {
        socPollingJob?.cancel()
        socPollingJob = null
        networkPollingJob?.cancel()
        networkPollingJob = null
        unregisterBatteryReceiver()
    }

    /**
     * این تابع بر اساس دسته‌بندی، polling مربوطه را شروع می‌کند.
     */
    fun startPollingForCategory(category: InfoCategory) {
        stopAllPolling() // ابتدا همه را متوقف کن
        when (category) {
            InfoCategory.THERMAL -> prepareThermalDetails()
            InfoCategory.SOC -> startSocPolling()
            InfoCategory.BATTERY -> registerBatteryReceiver()
            InfoCategory.NETWORK -> startNetworkPolling()
            else -> { /* نیازی به polling نیست */ }
        }
    }

    /**
     * کامنت: این تابع تنها زمانی فراخوانی می‌شود که کاربر وارد صفحه "برنامه‌ها" می‌شود.
     */
    /**
     * کامنت: این تابع به طور کامل بازنویسی شده است.
     * حالا یک بار اطلاعات را واکشی، پارتیشن‌بندی و در StateFlow های مجزا ذخیره می‌کند.
     */
    /**
     * کامنت: این تابع حالا تمام کارهای سنگین خود را در یک ترد پس‌زمینه (IO) انجام می‌دهد.
     * این کار ترد اصلی UI را آزاد نگه می‌دارد و از یخ‌زدگی انیمیشن و کندی ناوبری جلوگیری می‌کند.
     */
    fun loadAppsListIfNeeded() {
        // اگر در حال لودینگ یا از قبل لود شده است، کاری نکن
        if (_appsLoadingState.value != AppsLoadingState.IDLE) return

        viewModelScope.launch(Dispatchers.IO) {
            val currentCount = repository.getCurrentPackageCount()
            val cachedCount = repository.getPackageCountCache()

            // **شرط اصلی برای استفاده از کش**
            if (currentCount == cachedCount) {
                val cachedUserApps = repository.getUserAppsCache()
                val cachedSystemApps = repository.getSystemAppsCache()

                if (cachedUserApps != null && cachedSystemApps != null) {
                    _userApps.value = cachedUserApps
                    _systemApps.value = cachedSystemApps
                    _appsLoadingState.value = AppsLoadingState.LOADED
                    return@launch // از کش استفاده شد، عملیات پایان یافت
                }
            }

            // **اگر کش معتبر نبود، واکشی کامل را شروع کن**
            _appsLoadingState.value = AppsLoadingState.LOADING // StateFlow is thread-safe // postValue برای ترد پس‌زمینه

            val allApps = repository.getAppsInfo()
            val (user, system) = allApps.partition { !it.isSystemApp }

            // ذخیره نتایج جدید در کش
            repository.saveAppsCache(user, system, allApps.size)

            _userApps.value = user
            _systemApps.value = system
            _appsLoadingState.value = AppsLoadingState.LOADED
        }
        }


        // --- توابع مربوط به اسکن و بارگذاری داده ---

    fun loadDataForNonFirstLaunch(activity: Activity) {
        // اگر کش داریم، این متد نباید دوباره اسکن کند
        // اگر اجرای اول برنامه باشد، این تابع کاری انجام نمی‌دهد.
        if (repository.isFirstLaunch()) return

        // **منطق جدید و کلیدی برای حل مشکل**
        // بررسی می‌کنیم که آیا اطلاعات بارگذاری شده از کش، قدیمی است یا نه.
        // نشانه ما برای قدیمی بودن کش، خالی بودن لیست برنامه‌هاست.
        if (_deviceInfo.value.apps.isEmpty()) {
            viewModelScope.launch {
                // به صورت آرام و در پس‌زمینه، اطلاعات را مجددا واکشی می‌کنیم.
                val freshInfo = fetchAllDeviceInfo(activity)
                // کش را با اطلاعات جدید و کامل به‌روزرسانی می‌کنیم.
                repository.saveDeviceInfoCache(freshInfo)
                // وضعیت UI را با اطلاعات جدید به‌روز می‌کنیم.
                _deviceInfo.value = freshInfo
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
        val simInfoJob = viewModelScope.async { repository.getSimInfo() } // <-- job جدید

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
            simCards = simInfoJob.await(), // <-- افزودن نتیجه به مدل
            display = displayInfo,
            thermal = thermalInfo,
            network = networkInfo,
        )
    }

    fun startScan(activity: Activity, onScanComplete: () -> Unit) {
        if (_isScanning.value) return

        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            val animationJob = launch {
                launch { for (i in 1..100) { delay(150); _scanProgress.value = i / 100f } }
                _scanStatusText.value = "در حال خواندن مشخصات دستگاه..."
                delay(5000)
                _scanStatusText.value = "دریافت اطلاعات از درایور ها..."
                delay(5000)
                _scanStatusText.value = "ثبت اطلاعات..."
            }

            val dataLoadingJob = launch {
                _deviceInfo.value = fetchAllDeviceInfo(activity)
                // **اصلاح کلیدی: ذخیره اطلاعات در کش پس از اسکن موفق**
                repository.saveDeviceInfoCache(_deviceInfo.value)
            }

            animationJob.join()
            dataLoadingJob.join()
            repository.setFirstLaunchCompleted()
//            hasLoadedData = true

            onScanComplete() // به لایه ناوبری اطلاع می‌دهیم که اسکن تمام شد
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
    /**
     * اطلاعات سیم‌کارت را به صورت جداگانه واکشی کرده و وضعیت برنامه را به‌روز می‌کند.
     * این تابع برای استفاده پس از اعطای مجوز طراحی شده است.
     */
    fun fetchSimInfo() {
        viewModelScope.launch {
            val simCards = repository.getSimInfo()
            _deviceInfo.value = _deviceInfo.value.copy(simCards = simCards)
        }
    }
}