package ir.dekot.kavosh.ui.viewmodel

import android.app.Activity
import android.content.Context
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
import ir.dekot.kavosh.util.report.PdfGenerator
import ir.dekot.kavosh.util.report.ReportFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import javax.inject.Inject

sealed class ExportResult {
    data class Success(val message: String) : ExportResult()
    data class Failure(val message: String) : ExportResult()
}

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.R)
class DeviceInfoViewModel @Inject constructor(
    private val repository: DeviceInfoRepository,
    @ApplicationContext private val context: Context // تزریق Context برای استفاده در خروجی
) : ViewModel() {

    // --- کانال جدید برای ارسال نتیجه خروجی به UI ---
    private val _exportResult = MutableSharedFlow<ExportResult>()
    val exportResult = _exportResult.asSharedFlow()

    // --- اصلاح منطق خروجی گرفتن با SharedFlow ---
    private val _exportRequest = MutableSharedFlow<ExportFormat>()
    val exportRequest = _exportRequest.asSharedFlow() // به جای receiveAsFlow

    var pendingExportFormat: ExportFormat? = null
        private set

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo = _deviceInfo.asStateFlow()

    private val _thermalDetails = MutableStateFlow<List<ThermalInfo>>(emptyList())
    val thermalDetails = _thermalDetails.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen = _currentScreen.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    private val _scanStatusText = MutableStateFlow("آماده برای اسکن...")
    val scanStatusText = _scanStatusText.asStateFlow()

    private val _themeState = MutableStateFlow(Theme.SYSTEM)
    val themeState: StateFlow<Theme> = _themeState.asStateFlow()

    private val _dashboardItems = MutableStateFlow<List<DashboardItem>>(emptyList())
    val dashboardItems: StateFlow<List<DashboardItem>> = _dashboardItems.asStateFlow()

    // --- وضعیت جدید برای کنترل قابلیت جابجایی ---
    private val _isReorderingEnabled = MutableStateFlow(true)
    val isReorderingEnabled: StateFlow<Boolean> = _isReorderingEnabled.asStateFlow()

    // --- وضعیت جدید برای کنترل تم پویا ---
    private val _isDynamicThemeEnabled = MutableStateFlow(true)
    val isDynamicThemeEnabled: StateFlow<Boolean> = _isDynamicThemeEnabled.asStateFlow()

    init {
        _themeState.value = repository.getTheme()
        // خواندن وضعیت اولیه قابلیت جابجایی
        _isReorderingEnabled.value = repository.isReorderingEnabled()
        // خواندن وضعیت اولیه تم پویا
        _isDynamicThemeEnabled.value = repository.isDynamicThemeEnabled()
        loadDashboardItems()

        if (repository.isFirstLaunch()) {
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
    }

    private suspend fun fetchAllDeviceInfo(activity: Activity): DeviceInfo {
        // اجرای تمام عملیات‌های واکشی به صورت موازی با async
        val cpuInfoJob = viewModelScope.async { repository.getCpuInfo() }
        val gpuInfoJob = viewModelScope.async { repository.getGpuInfo(activity) }
        val ramInfoJob = viewModelScope.async { repository.getRamInfo() }
        val storageInfoJob = viewModelScope.async { repository.getStorageInfo() }
        val systemInfoJob = viewModelScope.async { repository.getSystemInfo() }
        val sensorInfoJob = viewModelScope.async { repository.getSensorInfo(activity) }
        val cameraInfoJob = viewModelScope.async { repository.getCameraInfoList() }

        // اطلاعات غیرهمزمان را جداگانه دریافت می‌کنیم
        val displayInfo = repository.getDisplayInfo(activity)
        val thermalInfo = repository.getThermalInfo()
        val networkInfo = repository.getNetworkInfo()

        // منتظر می‌مانیم تا تمام کارهای موازی تمام شوند و نتیجه را برمی‌گردانیم
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
            // اگر آیتم جدیدی به برنامه اضافه شده باشد، آن را به انتهای لیست اضافه کن
            val newItems = allPossibleItems.filter { item -> loadedItems.none { it.category == item.category } }
            _dashboardItems.value = loadedItems + newItems
        }
    }


    private fun getFullDashboardList(): List<DashboardItem> {
        return listOf(
            DashboardItem(InfoCategory.SOC, "پردازنده", Icons.Default.Memory),
            DashboardItem(InfoCategory.DEVICE, "دستگاه", Icons.Default.PhoneAndroid),
            DashboardItem(InfoCategory.SYSTEM, "سیستم", Icons.Default.Android),
            DashboardItem(InfoCategory.BATTERY, "باتری", Icons.Default.BatteryFull),
            DashboardItem(InfoCategory.SENSORS, "سنسورها", Icons.Default.Sensors),
            DashboardItem(InfoCategory.THERMAL, "دما", Icons.Default.Thermostat),
            DashboardItem(InfoCategory.NETWORK, "شبکه", Icons.Default.NetworkWifi),
            DashboardItem(InfoCategory.CAMERA, "دوربین", Icons.Default.PhotoCamera)
        )
    }

    fun onExportRequested(format: ExportFormat) {
        viewModelScope.launch {
            pendingExportFormat = format
            _exportRequest.emit(format) // به جای send از emit استفاده می‌کنیم
        }
    }

    /**
     * منطق اصلی خروجی گرفتن که حالا از کلاس‌های کمکی استفاده می‌کند.
     */
    fun performExport(uri: Uri, format: ExportFormat) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // دریافت اطلاعات یک بار در ابتدا
                val currentDeviceInfo = _deviceInfo.value
                val currentBatteryInfo = repository.getInitialBatteryInfo() ?: BatteryInfo()

                context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
                    FileOutputStream(pfd.fileDescriptor).use { fos ->
                        when (format) {
                            ExportFormat.TXT -> {
                                val fullReportText = ReportFormatter.formatFullReport(currentDeviceInfo, currentBatteryInfo)
                                fos.write(fullReportText.toByteArray())
                            }
                            ExportFormat.PDF -> {
                                // فراخوانی تابع استخراج شده از کلاس کمکی
                                PdfGenerator.writeStyledPdf(fos, currentDeviceInfo, currentBatteryInfo)
                            }
                        }
                    }
                }
                _exportResult.emit(ExportResult.Success("فایل با موفقیت در مسیر انتخابی شما ذخیره شد."))
            } catch (e: Exception) {
                e.printStackTrace()
                _exportResult.emit(ExportResult.Failure("خطا در ساخت فایل. لطفاً دوباره تلاش کنید."))
            } finally {
                pendingExportFormat = null
            }
        }
    }


    /**
     * وضعیت قابلیت تم پویا را تغییر داده و ذخیره می‌کند.
     */
    fun onDynamicThemeToggled(enabled: Boolean) {
        _isDynamicThemeEnabled.value = enabled
        viewModelScope.launch {
            repository.setDynamicThemeEnabled(enabled)
        }
    }

    /**
     * وضعیت قابلیت جابجایی داشبورد را تغییر داده و ذخیره می‌کند.
     */
    fun onReorderingToggled(enabled: Boolean) {
        _isReorderingEnabled.value = enabled
        viewModelScope.launch {
            repository.setReorderingEnabled(enabled)
        }
    }


    fun onThemeSelected(theme: Theme) {
        _themeState.value = theme
        viewModelScope.launch {
            repository.saveTheme(theme)
        }
    }

    fun navigateToSettings() {
        _currentScreen.value = Screen.Settings
    }

    fun navigateToEditDashboard() {
        _currentScreen.value = Screen.EditDashboard
    }

    fun loadDataForNonFirstLaunch(activity: Activity) {
        if (!repository.isFirstLaunch()) {
            loadDataWithoutAnimation(activity)
        }
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

            _currentScreen.value = Screen.Dashboard
            _isScanning.value = false
        }
    }

    private fun loadDataWithoutAnimation(activity: Activity) {
        viewModelScope.launch {
            _isScanning.value = true // نمایش یک لودینگ کوچک
            // فراخوانی تابع جدید برای بارگذاری موازی
            _deviceInfo.value = fetchAllDeviceInfo(activity)
            _isScanning.value = false // پنهان کردن لودینگ
        }
    }

    fun navigateToDetail(category: InfoCategory) {
        if (category == InfoCategory.THERMAL) {
            prepareThermalDetails()
        }
        _currentScreen.value = Screen.Detail(category)
    }

    fun navigateBack() {
        if (_currentScreen.value is Screen.EditDashboard) {
            loadDashboardItems()
        }
        _currentScreen.value = Screen.Dashboard
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

    private fun saveDashboardChanges() {
        val currentItems = _dashboardItems.value
        val newOrder = currentItems.map { it.category }
        val newHiddenSet = currentItems.filter { !it.isVisible }.map { it.category }.toSet()
        repository.saveDashboardOrder(newOrder)
        repository.saveHiddenCategories(newHiddenSet)
    }

    fun saveDashboardOrder(orderedCategories: List<InfoCategory>) {
        viewModelScope.launch {
            val currentItems = _dashboardItems.value
            val hiddenCategories = currentItems.filter { !it.isVisible }.map { it.category }
            val newFullOrder = orderedCategories + hiddenCategories
            repository.saveDashboardOrder(newFullOrder)
            // لیست داخلی را نیز مجددا بارگذاری می‌کنیم تا همگام بماند
            loadDashboardItems()
        }
    }
}