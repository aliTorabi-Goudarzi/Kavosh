package ir.dekot.kavosh.feature_deviceInfo.model.repository

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن اصلی اطلاعات دستگاه - مسئول تجمیع اطلاعات از مخازن تخصصی
 * Repository اصلی که اطلاعات کامل دستگاه را از مخازن مختلف جمع‌آوری می‌کند
 */
@Singleton
class DeviceInfoRepository @Inject constructor(
    private val hardwareRepository: HardwareRepository,
    private val systemRepository: SystemRepository,
    private val powerRepository: PowerRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val applicationRepository: ApplicationRepository,
    private val cameraRepository: CameraRepository
) {

    /**
     * دریافت اطلاعات کامل دستگاه با Activity
     * @param activity Activity مورد نیاز برای دسترسی به برخی اطلاعات سیستم
     * @return اطلاعات کامل دستگاه شامل تمام بخش‌ها
     */
    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun getDeviceInfo(activity: Activity): DeviceInfo {
        return DeviceInfo(
            cpu = hardwareRepository.getCpuInfo(),
            gpu = hardwareRepository.getGpuInfo(activity),
            ram = hardwareRepository.getRamInfo(),
            display = hardwareRepository.getDisplayInfo(activity),
            storage = hardwareRepository.getStorageInfo(),
            system = systemRepository.getSystemInfo(),
            network = connectivityRepository.getNetworkInfo(),
            sensors = hardwareRepository.getSensorInfo(activity),
            thermal = hardwareRepository.getThermalInfo(),
            cameras = cameraRepository.getCameraInfoList(),
            simCards = connectivityRepository.getSimInfo(),
            apps = applicationRepository.getInstalledApps()
        )
    }

    /**
     * دریافت اطلاعات کامل دستگاه بدون Activity (محدود)
     * این متد فقط اطلاعاتی را برمی‌گرداند که نیاز به Activity ندارند
     */
    fun getBasicDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            cpu = hardwareRepository.getCpuInfo(),
            ram = hardwareRepository.getRamInfo(),
            storage = hardwareRepository.getStorageInfo(),
            system = systemRepository.getSystemInfo(),
            network = connectivityRepository.getNetworkInfo(),
            thermal = hardwareRepository.getThermalInfo(),
            cameras = cameraRepository.getCameraInfoList(),
            apps = applicationRepository.getInstalledApps()
        )
    }
}