package ir.dekot.kavosh.data.repository

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.AppInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.CameraInfo
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.data.model.components.NetworkInfo
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.SensorInfo
import ir.dekot.kavosh.data.model.components.SimInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.model.components.WifiScanResult
import ir.dekot.kavosh.data.model.settings.Theme
import ir.dekot.kavosh.data.model.settings.PredefinedColorTheme
import ir.dekot.kavosh.data.model.settings.CustomColorTheme
import ir.dekot.kavosh.data.model.settings.ColorTheme
import ir.dekot.kavosh.data.source.CameraDataSource
import ir.dekot.kavosh.data.source.MemoryDataSource
import ir.dekot.kavosh.data.source.NetworkDataSource
import ir.dekot.kavosh.data.source.NetworkToolsDataSource
import ir.dekot.kavosh.data.source.PowerDataSource
import ir.dekot.kavosh.data.source.SettingsDataSource
import ir.dekot.kavosh.data.source.SimDataSource
import ir.dekot.kavosh.data.source.SocDataSource
import ir.dekot.kavosh.data.source.SystemDataSource
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import ir.dekot.kavosh.data.source.AppsDataSource // <-- ایمپورت سورس جدید

@Singleton
class DeviceInfoRepository @Inject constructor(
    private val powerDataSource: PowerDataSource,
    private val socDataSource: SocDataSource,
    private val systemDataSource: SystemDataSource,
    private val memoryDataSource: MemoryDataSource,
    private val settingsDataSource: SettingsDataSource,
    private val networkDataSource: NetworkDataSource, // <-- تزریق سورس جدید
    private val cameraDataSource: CameraDataSource, // <-- تزریق سورس جدید
    private val networkToolsDataSource: NetworkToolsDataSource, // <-- تزریق سورس جدید
    private val simDataSource: SimDataSource, // <-- تزریق سورس جدید
    private val appsDataSource: AppsDataSource // <-- تزریق سورس جدید
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
    /**
     * *** تابع جدید برای اتصال به DataSource ***
     */
    fun performStorageSpeedTest(onProgress: (Float) -> Unit): Pair<String, String> =
        memoryDataSource.performStorageSpeedTest(onProgress)

    /**
     * تست پیشرفته سرعت حافظه با فایل ۱ گیگابایتی
     */
    fun performEnhancedStorageSpeedTest(
        onProgress: (Float) -> Unit,
        onSpeedUpdate: (writeSpeed: Double, readSpeed: Double) -> Unit,
        onPhaseChange: (phase: String) -> Unit,
        onSpeedHistoryUpdate: (writeHistory: List<ir.dekot.kavosh.data.model.storage.SpeedDataPoint>, readHistory: List<ir.dekot.kavosh.data.model.storage.SpeedDataPoint>) -> Unit
    ): ir.dekot.kavosh.data.model.storage.StorageSpeedTestResult =
        memoryDataSource.performEnhancedStorageSpeedTest(onProgress, onSpeedUpdate, onPhaseChange, onSpeedHistoryUpdate)

    /**
     * ذخیره تاریخچه تست سرعت حافظه
     */
    fun saveStorageSpeedTestHistory(history: List<ir.dekot.kavosh.data.model.storage.StorageTestSummary>) =
        settingsDataSource.saveStorageSpeedTestHistory(history)

    /**
     * بازیابی تاریخچه تست سرعت حافظه
     */
    fun getStorageSpeedTestHistory(): List<ir.dekot.kavosh.data.model.storage.StorageTestSummary> =
        settingsDataSource.getStorageSpeedTestHistory()

    // --- NetworkDataSource ---
    fun getNetworkInfo(): NetworkInfo = networkDataSource.getNetworkInfo()

    // --- CameraDataSource ---
    fun getCameraInfoList(): List<CameraInfo> = cameraDataSource.getCameraInfoList()

    // متدهای جدید برای کنترل تم پویا
    fun setDynamicThemeEnabled(enabled: Boolean) = settingsDataSource.setDynamicThemeEnabled(enabled)
    fun isDynamicThemeEnabled(): Boolean = settingsDataSource.isDynamicThemeEnabled()

    // ... (سایر توابع SystemDataSource)
    fun getAppVersion(): String = systemDataSource.getAppVersion()

    // --- متدهای جدید برای کش ---
    fun saveDeviceInfoCache(deviceInfo: DeviceInfo) = settingsDataSource.saveDeviceInfoCache(deviceInfo)
    fun getDeviceInfoCache(): DeviceInfo? = settingsDataSource.getDeviceInfoCache()
    fun clearDeviceInfoCache() = settingsDataSource.clearDeviceInfoCache()

    /**
     * دریافت اطلاعات کامل دستگاه
     * @param activity Activity مورد نیاز برای دسترسی به برخی اطلاعات سیستم
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun getDeviceInfo(activity: Activity): DeviceInfo {
        return DeviceInfo(
            cpu = getCpuInfo(),
            gpu = getGpuInfo(activity),
            ram = getRamInfo(),
            display = getDisplayInfo(activity),
            storage = getStorageInfo(),
            system = getSystemInfo(),
            network = getNetworkInfo(),
            sensors = getSensorInfo(activity),
            thermal = getThermalInfo(),
            cameras = getCameraInfoList(),
            simCards = getSimInfo(),
            apps = getInstalledApps()
        )
    }

    /**
     * دریافت اطلاعات کامل دستگاه بدون Activity (محدود)
     * این متد فقط اطلاعاتی را برمی‌گرداند که نیاز به Activity ندارند
     */
    fun getBasicDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            cpu = getCpuInfo(),
            ram = getRamInfo(),
            storage = getStorageInfo(),
            system = getSystemInfo(),
            network = getNetworkInfo(),
            thermal = getThermalInfo(),
            cameras = getCameraInfoList(),
            apps = getInstalledApps()
        )
    }

    /**
     * دریافت اطلاعات فعلی باتری
     */
    fun getCurrentBatteryInfo(): BatteryInfo {
        return getInitialBatteryInfo() ?: BatteryInfo()
    }

    // **اصلاح: این تابع حالا suspend است**
    suspend fun getWifiScanResults(): List<WifiScanResult> {
        return networkToolsDataSource.scanForWifiNetworks().map {
            WifiScanResult(
                ssid = it.SSID.ifEmpty { "(Hidden Network)" },
                bssid = it.BSSID,
                capabilities = it.capabilities,
                level = it.level,
                frequency = it.frequency
            )
        }
    }

    fun pingHost(host: String): Flow<String> = networkToolsDataSource.pingHost(host)

    // --- متد جدید برای اطلاعات سیم‌کارت ---
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getSimInfo(): List<SimInfo> = simDataSource.getSimInfo()

    // --- متد جدید برای اطلاعات برنامه‌ها ---
    fun getAppsInfo(): List<AppInfo> = appsDataSource.getInstalledApps()
    fun getInstalledApps(): List<AppInfo> = appsDataSource.getInstalledApps()

    // --- متدهای جدید برای کش برنامه‌ها ---
    fun saveAppsCache(userApps: List<AppInfo>, systemApps: List<AppInfo>, count: Int) =
        settingsDataSource.saveAppsCache(userApps, systemApps, count)

    fun getUserAppsCache(): List<AppInfo>? = settingsDataSource.getUserAppsCache()
    fun getSystemAppsCache(): List<AppInfo>? = settingsDataSource.getSystemAppsCache()
    fun getPackageCountCache(): Int = settingsDataSource.getPackageCountCache()
    fun getCurrentPackageCount(): Int = appsDataSource.getPackageCount()

    // --- متدهای جدید برای مدیریت تم‌های رنگی ---
    fun savePredefinedColorTheme(colorTheme: PredefinedColorTheme) =
        settingsDataSource.savePredefinedColorTheme(colorTheme)

    fun saveCustomColorTheme(customTheme: CustomColorTheme) =
        settingsDataSource.saveCustomColorTheme(customTheme)

    fun getCurrentColorTheme(): ColorTheme? =
        settingsDataSource.getCurrentColorTheme()

    fun resetColorTheme() = settingsDataSource.resetColorTheme()

    fun hasCustomColorTheme(): Boolean = settingsDataSource.hasCustomColorTheme()
}