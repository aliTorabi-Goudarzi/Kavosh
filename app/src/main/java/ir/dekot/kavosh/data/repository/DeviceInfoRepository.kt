package ir.dekot.kavosh.data.repository

import android.app.Activity
import android.content.Intent
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.data.model.components.NetworkInfo
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.SensorInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.source.MemoryDataSource
import ir.dekot.kavosh.data.source.NetworkDataSource
import ir.dekot.kavosh.data.source.PowerDataSource
import ir.dekot.kavosh.data.source.SettingsDataSource
import ir.dekot.kavosh.data.source.SocDataSource
import ir.dekot.kavosh.data.source.SystemDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoRepository @Inject constructor(
    private val powerDataSource: PowerDataSource,
    private val socDataSource: SocDataSource,
    private val systemDataSource: SystemDataSource,
    private val memoryDataSource: MemoryDataSource,
    private val settingsDataSource: SettingsDataSource,
    private val networkDataSource: NetworkDataSource // <-- تزریق سورس جدید
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

    // --- NetworkDataSource ---
    fun getNetworkInfo(): NetworkInfo = networkDataSource.getNetworkInfo() // <-- متد جدید
    // StateFlow وضعیت هات‌اسپات را در معرض نمایش قرار می‌دهیم
//    val isHotspotEnabled: StateFlow<Boolean> = networkDataSource.isHotspotEnabled


}