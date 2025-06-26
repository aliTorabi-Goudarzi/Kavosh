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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.ui.screen.dashboard.DashboardItem
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
import ir.dekot.kavosh.R

sealed class ExportResult {
    data class Success(val message: String) : ExportResult()
    data class Failure(val message: String) : ExportResult()
}

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.R)
class DeviceInfoViewModel @Inject constructor(
    private val repository: DeviceInfoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- State جدید برای نسخه برنامه ---
    private val _appVersion = MutableStateFlow("")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    // --- State جدید برای زبان ---
    private val _language = MutableStateFlow("fa")
    val language: StateFlow<String> = _language.asStateFlow()

    // --- رویداد جدید برای اطلاع رسانی به Activity ---
    private val _languageChangeRequest = MutableSharedFlow<Unit>()
    val languageChangeRequest = _languageChangeRequest.asSharedFlow()


    // --- یک پرچم برای جلوگیری از بارگذاری مجدد داده‌ها ---
    private var hasLoadedData = false

    // --- State های اصلی ---
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

    // --- State های مربوط به تنظیمات ---
    private val _themeState = MutableStateFlow(Theme.SYSTEM)
    val themeState: StateFlow<Theme> = _themeState.asStateFlow()

    private val _dashboardItems = MutableStateFlow<List<DashboardItem>>(emptyList())
    val dashboardItems: StateFlow<List<DashboardItem>> = _dashboardItems.asStateFlow()

    private val _isReorderingEnabled = MutableStateFlow(true)
    val isReorderingEnabled: StateFlow<Boolean> = _isReorderingEnabled.asStateFlow()

    private val _isDynamicThemeEnabled = MutableStateFlow(true)
    val isDynamicThemeEnabled: StateFlow<Boolean> = _isDynamicThemeEnabled.asStateFlow()

    // --- State ها و منطق‌های ادغام شده از ViewModel های دیگر ---
    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()

    private val _liveCpuFrequencies = MutableStateFlow<List<String>>(emptyList())
    val liveCpuFrequencies = _liveCpuFrequencies.asStateFlow()

    private val _liveGpuLoad = MutableStateFlow<Int?>(null)
    val liveGpuLoad = _liveGpuLoad.asStateFlow()

    private var socPollingJob: Job? = null
    private var batteryReceiver: BroadcastReceiver? = null

    // --- State های جدید برای سرعت شبکه ---
    private val _downloadSpeed = MutableStateFlow("0.0 KB/s")
    val downloadSpeed = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow("0.0 KB/s")
    val uploadSpeed = _uploadSpeed.asStateFlow()

    private var networkPollingJob: Job? = null

    // --- State های مربوط به خروجی گرفتن ---
    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult = _exportResult.asSharedFlow()

    private val _exportRequest = MutableSharedFlow<ExportFormat>()
    val exportRequest = _exportRequest.asSharedFlow()

    var pendingExportFormat: ExportFormat? = null
        private set

    init {
        // بارگذاری تنظیمات ذخیره شده در شروع
        _themeState.value = repository.getTheme()
        _isReorderingEnabled.value = repository.isReorderingEnabled()
        _isDynamicThemeEnabled.value = repository.isDynamicThemeEnabled()
        _language.value = repository.getLanguage() // بارگذاری زبان
        _appVersion.value = repository.getAppVersion() // دریافت نسخه برنامه در شروع
        loadDashboardItems()

        if (repository.isFirstLaunch()) {
            hasLoadedData = false
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    /**
     * *** تابع جدید: ***
     * برای ناوبری به صفحه "درباره ما".
     */
    fun navigateToAbout() {
        _currentScreen.value = Screen.About
    }

    // --- متد جدید برای تغییر زبان ---
    fun onLanguageSelected(lang: String) {
        // اگر زبان تغییری نکرده، کاری انجام نده
        if (lang == _language.value) return

        viewModelScope.launch {
            repository.saveLanguage(lang)
            _language.value = lang
            // ارسال رویداد برای بازسازی Activity
            _languageChangeRequest.emit(Unit)
        }
    }

    companion object {
        // متد استاتیک برای دسترسی به زبان قبل از اینکه ViewModel ساخته شود
        fun getSavedLanguage(context: Context): String {
            val prefs = context.getSharedPreferences("device_inspector_prefs", Context.MODE_PRIVATE)
            // تغییر کلیدی در این خط: مقدار پیش‌فرض به "fa" تغییر کرد
            return prefs.getString("app_language", "fa") ?: "fa"
        }
    }

    // --- افزودن منطق جدید ---
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

                // *** تغییر کلیدی: پاس دادن context به تابع ***
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

    override fun onCleared() {
        super.onCleared()
        stopSocPolling()
        unregisterBatteryReceiver()
        stopNetworkPolling() // --- توقف در اینجا ---
    }

    // --- منطق اسکن و بارگذاری داده ---

    fun loadDataForNonFirstLaunch(activity: Activity) {
        // اگر اجرای اول است یا داده‌ها قبلا بارگذاری شده‌اند، خارج شو
        if (repository.isFirstLaunch() || hasLoadedData) return

        viewModelScope.launch {
            _isScanning.value = true
            try {
                // عملیات اصلی واکشی داده حالا در یک بلاک try-catch قرار دارد
                _deviceInfo.value = fetchAllDeviceInfo(activity)
            } catch (e: Exception) {
                // در صورت بروز خطا، آن را لاگ می‌کنیم تا در آینده بررسی شود
                e.printStackTrace()
                // می‌توان یک پیام خطا به کاربر نمایش داد
            } finally {
                _isScanning.value = false
                // پرچم را تنظیم می‌کنیم تا این عملیات دوباره اجرا نشود
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
            hasLoadedData = true // پس از اسکن موفق، پرچم را تنظیم کن

            _currentScreen.value = Screen.Dashboard
            _isScanning.value = false
        }
    }

    // --- سایر توابع ViewModel بدون تغییر باقی می‌مانند ---
    // (توابع مربوط به داده‌های زنده، ناوبری، داشبورد، تنظیمات و خروجی)

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

    fun navigateToDetail(category: InfoCategory) {
        stopSocPolling()
        unregisterBatteryReceiver()
        stopNetworkPolling() // --- توقف polling قبلی ---
        when (category) {
            InfoCategory.THERMAL -> prepareThermalDetails()
            InfoCategory.SOC -> startSocPolling()
            InfoCategory.BATTERY -> registerBatteryReceiver()
            InfoCategory.NETWORK -> startNetworkPolling() // --- شروع polling جدید ---
            else -> { }
        }
        _currentScreen.value = Screen.Detail(category)

    }

    fun navigateBack() {
        stopSocPolling()
        unregisterBatteryReceiver()

        if (_currentScreen.value is Screen.EditDashboard) {
            loadDashboardItems()
        }
        stopNetworkPolling() // --- توقف polling قبلی ---
        _currentScreen.value = Screen.Dashboard

    }

    private fun loadDashboardItems() {
        viewModelScope.launch {
            val orderedCategories = repository.getDashboardOrder()
            val hiddenCategories = repository.getHiddenCategories()
            val allPossibleItems = getFullDashboardList()

            val loadedItems = orderedCategories.mapNotNull { category ->
                allPossibleItems.find { it.category == category }?.copy(
                    isVisible = !hiddenCategories.contains(category)
                )
            }
            val newItems = allPossibleItems.filter { item -> loadedItems.none { it.category == item.category } }
            _dashboardItems.value = loadedItems + newItems
        }
    }

    private fun getFullDashboardList(): List<DashboardItem> {
        // *** تغییر کلیدی: استفاده از شناسه‌های منبع رشته (R.string.*) ***
        return listOf(
            DashboardItem(InfoCategory.SOC, R.string.category_soc, Icons.Default.Memory),
            DashboardItem(InfoCategory.DEVICE, R.string.category_device, Icons.Default.PhoneAndroid),
            DashboardItem(InfoCategory.SYSTEM, R.string.category_system, Icons.Default.Android),
            DashboardItem(InfoCategory.BATTERY, R.string.category_battery, Icons.Default.BatteryFull),
            DashboardItem(InfoCategory.SENSORS, R.string.category_sensors, Icons.Default.Sensors),
            DashboardItem(InfoCategory.THERMAL, R.string.category_thermal, Icons.Default.Thermostat),
            DashboardItem(InfoCategory.NETWORK, R.string.category_network, Icons.Default.NetworkWifi),
            DashboardItem(InfoCategory.CAMERA, R.string.category_camera, Icons.Default.PhotoCamera)
        )
    }


    fun onDashboardItemVisibilityChanged(category: InfoCategory, isVisible: Boolean) {
        viewModelScope.launch {
            val currentItems = _dashboardItems.value.toMutableList()
            val itemIndex = currentItems.indexOfFirst { it.category == category }
            if (itemIndex != -1) {
                currentItems[itemIndex] = currentItems[itemIndex].copy(isVisible = isVisible)
                _dashboardItems.value = currentItems
                saveDashboardChanges()
            }
        }
    }

    fun saveDashboardOrder(orderedCategories: List<InfoCategory>) {
        viewModelScope.launch {
            val currentItems = _dashboardItems.value
            val hiddenCategories = currentItems.filter { !it.isVisible }.map { it.category }
            val newFullOrder = orderedCategories + hiddenCategories
            repository.saveDashboardOrder(newFullOrder)
            loadDashboardItems()
        }
    }

    private fun saveDashboardChanges() {
        val currentItems = _dashboardItems.value
        val newOrder = currentItems.map { it.category }
        val newHiddenSet = currentItems.filter { !it.isVisible }.map { it.category }.toSet()
        repository.saveDashboardOrder(newOrder)
        repository.saveHiddenCategories(newHiddenSet)
    }

    fun navigateToSettings() {
        _currentScreen.value = Screen.Settings
    }

    fun navigateToEditDashboard() {
        _currentScreen.value = Screen.EditDashboard
    }

    fun onThemeSelected(theme: Theme) {
        _themeState.value = theme
        viewModelScope.launch {
            repository.saveTheme(theme)
        }
    }

    fun onDynamicThemeToggled(enabled: Boolean) {
        _isDynamicThemeEnabled.value = enabled
        viewModelScope.launch {
            repository.setDynamicThemeEnabled(enabled)
        }
    }

    fun onReorderingToggled(enabled: Boolean) {
        _isReorderingEnabled.value = enabled
        viewModelScope.launch {
            repository.setReorderingEnabled(enabled)
        }
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
                                // *** تغییر کلیدی: پاس دادن context به عنوان اولین آرگومان ***
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

    private fun prepareThermalDetails() {
        val combinedList = mutableListOf<ThermalInfo>()
        repository.getInitialBatteryInfo()?.let { batteryData ->
            if (batteryData.temperature.isNotBlank()) {
                combinedList.add(
                    ThermalInfo(
                        type = "باتری (Battery)",
                        temperature = batteryData.temperature
                    )
                )
            }
        }
        combinedList.addAll(deviceInfo.value.thermal)
        _thermalDetails.value = combinedList
    }
}