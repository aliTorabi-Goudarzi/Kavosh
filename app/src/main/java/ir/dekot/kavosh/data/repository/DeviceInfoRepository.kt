package ir.dekot.kavosh.data.repository

import android.app.Activity
import android.content.Intent
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.data.model.components.*
import ir.dekot.kavosh.data.source.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoRepository @Inject constructor(
    private val powerDataSource: PowerDataSource,
    private val socDataSource: SocDataSource,
    private val systemDataSource: SystemDataSource,
    private val memoryDataSource: MemoryDataSource,
    private val settingsDataSource: SettingsDataSource
) {

    // --- SettingsDataSource ---
    fun isFirstLaunch(): Boolean = settingsDataSource.isFirstLaunch()
    fun setFirstLaunchCompleted() = settingsDataSource.setFirstLaunchCompleted()

    // --- PowerDataSource ---
    fun getThermalInfo(): List<ThermalInfo> = powerDataSource.getThermalInfo()
    fun getBatteryInfo(intent: Intent): BatteryInfo = powerDataSource.getBatteryInfo(intent)
    fun getInitialBatteryInfo(): BatteryInfo? = powerDataSource.getInitialBatteryInfo()

    // --- SocDataSource ---
    fun getCpuInfo(): CpuInfo = socDataSource.getCpuInfo()
    fun getLiveCpuFrequencies(): List<String> = socDataSource.getLiveCpuFrequencies()
    fun getGpuLoadPercentage(): Int? = socDataSource.getGpuLoadPercentage()
    suspend fun getGpuInfo(activity: Activity): GpuInfo = socDataSource.getGpuInfo(activity)

    // --- SystemDataSource ---
    // امضای این دو متد را برای دریافت Activity تغییر می‌دهیم
    @RequiresApi(30)
    fun getDisplayInfo(activity: Activity): DisplayInfo = systemDataSource.getDisplayInfo(activity)
    fun getSystemInfo(): SystemInfo = systemDataSource.getSystemInfo()
    fun getSensorInfo(activity: Activity): List<SensorInfo> = systemDataSource.getSensorInfo(activity)

    // --- MemoryDataSource ---
    fun getRamInfo(): RamInfo = memoryDataSource.getRamInfo()
    fun getStorageInfo(): StorageInfo = memoryDataSource.getStorageInfo()
}