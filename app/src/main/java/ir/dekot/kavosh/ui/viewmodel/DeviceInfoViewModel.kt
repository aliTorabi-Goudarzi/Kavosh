package ir.dekot.kavosh.ui.viewmodel

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import ir.dekot.kavosh.data.model.settings.Theme // <-- ایمپورت جدید
import kotlinx.coroutines.flow.StateFlow // <-- ایمپورت جدید

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.R)
class DeviceInfoViewModel @Inject constructor(
    private val repository: DeviceInfoRepository
) : ViewModel() {

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

    init {
        // در زمان ساخته شدن ViewModel، آخرین تم ذخیره شده را می‌خوانیم
        _themeState.value = repository.getTheme()

        if (repository.isFirstLaunch()) {
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
        // گوش دادن به تغییرات وضعیت هات‌اسپات

    }

    /**
     * این متد زمانی فراخوانی می‌شود که کاربر تم جدیدی را انتخاب می‌کند.
     */
    fun onThemeSelected(theme: Theme) {
        // 1. تم جدید را در StateFlow قرار می‌دهیم تا UI آپدیت شود
        _themeState.value = theme
        // 2. انتخاب جدید کاربر را برای استفاده در آینده ذخیره می‌کنیم
        viewModelScope.launch {
            repository.saveTheme(theme)
        }
    }

    // متد جدید برای ناوبری به صفحه تنظیمات
    fun navigateToSettings() {
        _currentScreen.value = Screen.Settings
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
                _deviceInfo.value = DeviceInfo(
                    cpu = repository.getCpuInfo(),
                    gpu = repository.getGpuInfo(activity),
                    ram = repository.getRamInfo(),
                    storage = repository.getStorageInfo(),
                    display = repository.getDisplayInfo(activity), // <-- Activity را اینجا پاس می‌دهیم
                    system = repository.getSystemInfo(),
                    sensors = repository.getSensorInfo(activity), // <-- Activity را اینجا پاس می‌دهیم
                    thermal = repository.getThermalInfo(),
                    network = repository.getNetworkInfo(), //
                    cameras = repository.getCameraInfoList() // <-- این خط را اضافه کنید// <-- این خط را اضافه کنید
                )
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
            _deviceInfo.value = DeviceInfo(
                cpu = repository.getCpuInfo(),
                gpu = repository.getGpuInfo(activity),
                ram = repository.getRamInfo(),
                storage = repository.getStorageInfo(),
                display = repository.getDisplayInfo(activity), // <-- Activity را اینجا پاس می‌دهیم
                system = repository.getSystemInfo(),
                sensors = repository.getSensorInfo(activity), // <-- Activity را اینجا پاس می‌دهیم
                thermal = repository.getThermalInfo(),
                cameras = repository.getCameraInfoList() // <-- این خط را اضافه کنید
            )
        }
    }

    fun navigateToDetail(category: InfoCategory) {
        if (category == InfoCategory.THERMAL) {
            prepareThermalDetails()
        }
        _currentScreen.value = Screen.Detail(category)
    }

    fun navigateBack() {
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
}