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

    init {
        if (repository.isFirstLaunch()) {
            _currentScreen.value = Screen.Splash
        } else {
            _currentScreen.value = Screen.Dashboard
        }
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
                    thermal = repository.getThermalInfo()
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
                thermal = repository.getThermalInfo()
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