package ir.dekot.kavosh.feature_deviceInfo.viewModel

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
import ir.dekot.kavosh.feature_deviceInfo.model.SensorHandler
import ir.dekot.kavosh.feature_deviceInfo.model.SensorState
import ir.dekot.kavosh.core.util.formatSizeOrSpeed
import ir.dekot.kavosh.feature_deviceInfo.model.AppInfo
import ir.dekot.kavosh.feature_deviceInfo.model.BatteryInfo
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_deviceInfo.model.ThermalInfo
import ir.dekot.kavosh.feature_deviceInfo.model.repository.DeviceInfoRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.HardwareRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.PowerRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ConnectivityRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ApplicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val deviceInfoRepository: DeviceInfoRepository,
    private val hardwareRepository: HardwareRepository,
    private val settingsRepository: SettingsRepository,
    private val powerRepository: PowerRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val applicationRepository: ApplicationRepository,
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
        if (!settingsRepository.isFirstLaunch()) {
            val cachedInfo = settingsRepository.getDeviceInfoCache()
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
            val currentCount = applicationRepository.getCurrentPackageCount()
            val cachedCount = applicationRepository.getPackageCountCache()

            // **شرط اصلی برای استفاده از کش**
            if (currentCount == cachedCount) {
                val cachedUserApps = applicationRepository.getUserAppsCache()
                val cachedSystemApps = applicationRepository.getSystemAppsCache()

                if (cachedUserApps != null && cachedSystemApps != null) {
                    _userApps.value = cachedUserApps
                    _systemApps.value = cachedSystemApps
                    _appsLoadingState.value = AppsLoadingState.LOADED
                    return@launch // از کش استفاده شد، عملیات پایان یافت
                }
            }

            // **اگر کش معتبر نبود، واکشی کامل را شروع کن**
            _appsLoadingState.value = AppsLoadingState.LOADING // StateFlow is thread-safe // postValue برای ترد پس‌زمینه

            val allApps = applicationRepository.getAppsInfo()
            val (user, system) = allApps.partition { !it.isSystemApp }

            // ذخیره نتایج جدید در کش
            applicationRepository.saveAppsCache(user, system, allApps.size)

            _userApps.value = user
            _systemApps.value = system
            _appsLoadingState.value = AppsLoadingState.LOADED
        }
        }


        // --- توابع مربوط به اسکن و بارگذاری داده ---

    fun loadDataForNonFirstLaunch(activity: Activity) {
        // اگر کش داریم، این متد نباید دوباره اسکن کند
        // اگر اجرای اول برنامه باشد، این تابع کاری انجام نمی‌دهد.
        if (settingsRepository.isFirstLaunch()) return

        // **منطق جدید و کلیدی برای حل مشکل**
        // بررسی می‌کنیم که آیا اطلاعات بارگذاری شده از کش، قدیمی است یا نه.
        // نشانه ما برای قدیمی بودن کش، خالی بودن لیست برنامه‌هاست.
        if (_deviceInfo.value.apps.isEmpty()) {
            viewModelScope.launch {
                // به صورت آرام و در پس‌زمینه، اطلاعات را مجددا واکشی می‌کنیم.
                val freshInfo = fetchAllDeviceInfo(activity)
                // کش را با اطلاعات جدید و کامل به‌روزرسانی می‌کنیم.
                settingsRepository.saveDeviceInfoCache(freshInfo)
                // وضعیت UI را با اطلاعات جدید به‌روز می‌کنیم.
                _deviceInfo.value = freshInfo
            }
        }
    }

    /**
     * بارگذاری داده‌ها برای اولین اجرا (بدون نمایش صفحه اسپلش)
     * این متد برای استفاده در صفحه بارگذاری جدید طراحی شده است
     */
    suspend fun loadDataForFirstLaunch(activity: Activity) {
        val freshInfo = fetchAllDeviceInfo(activity)
        settingsRepository.saveDeviceInfoCache(freshInfo)
        _deviceInfo.value = freshInfo
        settingsRepository.setFirstLaunchCompleted()
    }

    /**
     * اطمینان از بارگذاری کامل داده‌ها
     * این متد برای اجراهای بعدی استفاده می‌شود
     */
    suspend fun ensureDataIsLoaded(activity: Activity) {
        if (_deviceInfo.value.apps.isEmpty() || _deviceInfo.value.simCards.isEmpty()||_deviceInfo.value.cameras.isEmpty()) {
            val freshInfo = fetchAllDeviceInfo(activity)
            settingsRepository.saveDeviceInfoCache(freshInfo)
            _deviceInfo.value = freshInfo
        }
    }

    /**
     * بارگذاری اطلاعات برنامه‌ها در صورت نیاز
     */
//    fun loadAppsIfNeeded() {
//        if (_userApps.value.isEmpty() && _systemApps.value.isEmpty()) {
//            loadApps()
//        }
//    }

    private suspend fun fetchAllDeviceInfo(activity: Activity): DeviceInfo {
        return deviceInfoRepository.getDeviceInfo(activity)
    }

    fun startScan(activity: Activity, onScanComplete: () -> Unit) {
        if (_isScanning.value) return

        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            val animationJob = launch {
                launch { for (i in 1..100) { delay(100); _scanProgress.value = i / 100f } }
                _scanStatusText.value = "در حال خواندن مشخصات دستگاه..."
                delay(5000)
                _scanStatusText.value = "دریافت اطلاعات از درایور ها..."
                delay(5000)
                _scanStatusText.value = "ثبت اطلاعات..."
            }

            val dataLoadingJob = launch {
                _deviceInfo.value = fetchAllDeviceInfo(activity)
                // **اصلاح کلیدی: ذخیره اطلاعات در کش پس از اسکن موفق**
                settingsRepository.saveDeviceInfoCache(_deviceInfo.value)
            }

            animationJob.join()
            dataLoadingJob.join()
            settingsRepository.setFirstLaunchCompleted()
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
                _liveCpuFrequencies.value = hardwareRepository.getLiveCpuFrequencies()
                _liveGpuLoad.value = hardwareRepository.getGpuLoadPercentage()
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
            _batteryInfo.value = powerRepository.getBatteryInfo(initialIntent)
        }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    if (it.action == Intent.ACTION_BATTERY_CHANGED) {
                        _batteryInfo.value = powerRepository.getBatteryInfo(it)
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
        powerRepository.getInitialBatteryInfo()?.let { batteryData ->
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
            val simCards = connectivityRepository.getSimInfo()
            _deviceInfo.value = _deviceInfo.value.copy(simCards = simCards)
        }
    }
}