package ir.dekot.kavosh.data.repository

import android.app.Activity
import android.content.Intent
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.CameraInfo
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.data.model.components.NetworkInfo
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.SensorInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.data.source.CameraDataSource
import ir.dekot.kavosh.data.source.MemoryDataSource
import ir.dekot.kavosh.data.source.NetworkDataSource
import ir.dekot.kavosh.data.source.PowerDataSource
import ir.dekot.kavosh.data.source.SettingsDataSource
import ir.dekot.kavosh.data.source.SocDataSource
import ir.dekot.kavosh.data.source.SystemDataSource
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoRepository @Inject constructor(
    private val powerDataSource: PowerDataSource,
    private val socDataSource: SocDataSource,
    private val systemDataSource: SystemDataSource,
    private val memoryDataSource: MemoryDataSource,
    private val settingsDataSource: SettingsDataSource,
    private val networkDataSource: NetworkDataSource, // <-- تزریق سورس جدید
    private val cameraDataSource: CameraDataSource // <-- تزریق سورس جدید
) {

    // --- SettingsDataSource ---

    // دو متد زیر را اضافه کنید
    fun saveLanguage(language: String) = settingsDataSource.saveLanguage(language)
    fun getLanguage(): String = settingsDataSource.getLanguage()
    fun isFirstLaunch(): Boolean = settingsDataSource.isFirstLaunch()
    fun setFirstLaunchCompleted() = settingsDataSource.setFirstLaunchCompleted()
    fun saveTheme(theme: Theme) = settingsDataSource.saveTheme(theme)
    fun getTheme(): Theme = settingsDataSource.getTheme()
    // متدهای جدید برای داشبورد
    fun saveDashboardOrder(categories: List<InfoCategory>) = settingsDataSource.saveDashboardOrder(categories)
    fun getDashboardOrder(): List<InfoCategory> = settingsDataSource.getDashboardOrder()
    fun saveHiddenCategories(hidden: Set<InfoCategory>) = settingsDataSource.saveHiddenCategories(hidden)
    fun getHiddenCategories(): Set<InfoCategory> = settingsDataSource.getHiddenCategories()
    // متدهای جدید برای کنترل قابلیت جابجایی
    fun setReorderingEnabled(enabled: Boolean) = settingsDataSource.setReorderingEnabled(enabled)
    fun isReorderingEnabled(): Boolean = settingsDataSource.isReorderingEnabled()



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
    fun getNetworkInfo(): NetworkInfo = networkDataSource.getNetworkInfo()

    // --- CameraDataSource ---
    fun getCameraInfoList(): List<CameraInfo> = cameraDataSource.getCameraInfoList()

    // متدهای جدید برای کنترل تم پویا
    fun setDynamicThemeEnabled(enabled: Boolean) = settingsDataSource.setDynamicThemeEnabled(enabled)
    fun isDynamicThemeEnabled(): Boolean = settingsDataSource.isDynamicThemeEnabled()


}