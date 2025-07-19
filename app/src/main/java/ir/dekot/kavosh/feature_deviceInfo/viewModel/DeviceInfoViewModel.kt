package ir.dekot.kavosh.feature_deviceInfo.viewModel

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
import ir.dekot.kavosh.feature_deviceInfo.model.BatteryInfo
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_deviceInfo.model.ThermalInfo
import ir.dekot.kavosh.feature_deviceInfo.model.repository.HardwareRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.PowerRepository
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

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.R)
class DeviceInfoViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository,
    private val powerRepository: PowerRepository,
    private val sensorHandler: SensorHandler,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    val sensorState: StateFlow<SensorState> = sensorHandler.sensorState

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo = _deviceInfo.asStateFlow()

    private val _thermalDetails = MutableStateFlow<List<ThermalInfo>>(emptyList())
    val thermalDetails = _thermalDetails.asStateFlow()

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
     * به‌روزرسانی اطلاعات دستگاه
     * @param deviceInfo اطلاعات جدید دستگاه
     */
    fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        _deviceInfo.value = deviceInfo
        prepareThermalDetails()
    }
}