package ir.dekot.kavosh.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import ir.dekot.kavosh.feature_settings.model.SettingsDataSource

import ir.dekot.kavosh.feature_deviceInfo.model.SensorHandler
import ir.dekot.kavosh.feature_deviceInfo.model.repository.DeviceInfoRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.HardwareRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SystemRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.PowerRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ConnectivityRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.ApplicationRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.CameraRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.SettingsRepository
import ir.dekot.kavosh.feature_deviceInfo.model.repository.TestingRepository
import ir.dekot.kavosh.feature_deviceInfo.model.AppsDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.CameraDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.NetworkDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.PowerDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.SimDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.SystemInfoDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.DisplayDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.SensorDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.AppInfoDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.RamDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.StorageDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.StorageTestDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.CpuDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.GpuDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.ThermalDataSource
import ir.dekot.kavosh.feature_testing.model.NetworkToolsDataSource
import ir.dekot.kavosh.feature_testing.model.DiagnosticDataSource

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Data Source Providers ---

    @Provides
    @Singleton
    fun providePowerDataSource(@ApplicationContext context: Context): PowerDataSource {
        return PowerDataSource(context)
    }

    // --- New split data sources ---

    @Provides
    @Singleton
    fun provideSystemInfoDataSource(@ApplicationContext context: Context): SystemInfoDataSource {
        return SystemInfoDataSource(context)
    }

    @Provides
    @Singleton
    fun provideDisplayDataSource(@ApplicationContext context: Context): DisplayDataSource {
        return DisplayDataSource(context)
    }

    @Provides
    @Singleton
    fun provideSensorDataSource(@ApplicationContext context: Context): SensorDataSource {
        return SensorDataSource(context)
    }

    @Provides
    @Singleton
    fun provideAppInfoDataSource(@ApplicationContext context: Context): AppInfoDataSource {
        return AppInfoDataSource(context)
    }

    @Provides
    @Singleton
    fun provideRamDataSource(@ApplicationContext context: Context): RamDataSource {
        return RamDataSource(context)
    }

    @Provides
    @Singleton
    fun provideStorageDataSource(@ApplicationContext context: Context): StorageDataSource {
        return StorageDataSource(context)
    }

    @Provides
    @Singleton
    fun provideStorageTestDataSource(@ApplicationContext context: Context): StorageTestDataSource {
        return StorageTestDataSource(context)
    }

    @Provides
    @Singleton
    fun provideCpuDataSource(@ApplicationContext context: Context): CpuDataSource {
        return CpuDataSource(context)
    }

    @Provides
    @Singleton
    fun provideGpuDataSource(@ApplicationContext context: Context): GpuDataSource {
        return GpuDataSource(context)
    }

    @Provides
    @Singleton
    fun provideThermalDataSource(@ApplicationContext context: Context): ThermalDataSource {
        return ThermalDataSource(context)
    }

    @Provides
    @Singleton
    fun provideSettingsDataSource(@ApplicationContext context: Context): SettingsDataSource {
        return SettingsDataSource(context)
    }

    @Provides
    @Singleton
    fun provideNetworkDataSource(@ApplicationContext context: Context): NetworkDataSource {
        return NetworkDataSource(context)
    }

    @Provides
    @Singleton
    fun provideNetworkToolsDataSource(@ApplicationContext context: Context): NetworkToolsDataSource {
        return NetworkToolsDataSource(context)
    }

    @Provides
    @Singleton
    fun provideCameraDataSource(@ApplicationContext context: Context): CameraDataSource {
        return CameraDataSource(context)
    }

    @Provides
    @Singleton
    fun provideSimDataSource(@ApplicationContext context: Context): SimDataSource {
        return SimDataSource(context)
    }

    @Provides
    @Singleton
    fun provideAppsDataSource(@ApplicationContext context: Context): AppsDataSource {
        return AppsDataSource(context)
    }

    // --- Providers for new specialized repositories ---

    @Provides
    @Singleton
    fun provideHardwareRepository(
        cpuDataSource: CpuDataSource,
        gpuDataSource: GpuDataSource,
        thermalDataSource: ThermalDataSource,
        ramDataSource: RamDataSource,
        storageDataSource: StorageDataSource,
        displayDataSource: DisplayDataSource,
        sensorDataSource: SensorDataSource,
        powerDataSource: PowerDataSource,
    ): HardwareRepository {
        return HardwareRepository(
            cpuDataSource,
            gpuDataSource,
            thermalDataSource,
            ramDataSource,
            storageDataSource,
            displayDataSource,
            sensorDataSource,
            powerDataSource
        )
    }

    @Provides
    @Singleton
    fun provideSystemRepository(
        systemInfoDataSource: SystemInfoDataSource,
        appInfoDataSource: AppInfoDataSource
    ): SystemRepository {
        return SystemRepository(systemInfoDataSource, appInfoDataSource)
    }

    @Provides
    @Singleton
    fun providePowerRepository(
        powerDataSource: PowerDataSource
    ): PowerRepository {
        return PowerRepository(powerDataSource)
    }

    @Provides
    @Singleton
    fun provideConnectivityRepository(
        networkDataSource: NetworkDataSource,
        simDataSource: SimDataSource,
        networkToolsDataSource: NetworkToolsDataSource
    ): ConnectivityRepository {
        return ConnectivityRepository(networkDataSource, simDataSource, networkToolsDataSource)
    }

    @Provides
    @Singleton
    fun provideApplicationRepository(
        appsDataSource: AppsDataSource,
        settingsDataSource: SettingsDataSource
    ): ApplicationRepository {
        return ApplicationRepository(appsDataSource, settingsDataSource)
    }

    @Provides
    @Singleton
    fun provideCameraRepository(
        cameraDataSource: CameraDataSource
    ): CameraRepository {
        return CameraRepository(cameraDataSource)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDataSource: SettingsDataSource
    ): SettingsRepository {
        return SettingsRepository(settingsDataSource)
    }

    @Provides
    @Singleton
    fun provideTestingRepository(
        storageTestDataSource: StorageTestDataSource,
        settingsDataSource: SettingsDataSource
    ): TestingRepository {
        return TestingRepository(storageTestDataSource, settingsDataSource)
    }

    @Provides
    @Singleton
    fun provideDeviceInfoRepository(
        hardwareRepository: HardwareRepository,
        systemRepository: SystemRepository,
        powerRepository: PowerRepository,
        connectivityRepository: ConnectivityRepository,
        applicationRepository: ApplicationRepository,
        cameraRepository: CameraRepository
    ): DeviceInfoRepository {
        return DeviceInfoRepository(
            hardwareRepository,
            systemRepository,
            powerRepository,
            connectivityRepository,
            applicationRepository,
            cameraRepository
        )
    }

    // --- Other Providers ---

    /**
     * این تابع به Hilt یاد می‌دهد که چگونه یک نمونه از SensorHandler بسازد.
     * چون SensorHandler خودش با @Inject مشخص شده، این متد ساده است.
     */
    @Provides
    @Singleton
    fun provideSensorHandler(@ApplicationContext context: Context): SensorHandler {
        return SensorHandler(context)
    }

    /**
     * Provider برای DiagnosticDataSource
     * منبع داده ابزارهای تشخیصی جدید
     */
    @Provides
    @Singleton
    fun provideDiagnosticDataSource(
        @ApplicationContext context: Context,
        cpuDataSource: CpuDataSource,
        ramDataSource: RamDataSource,
        storageDataSource: StorageDataSource,
        powerDataSource: PowerDataSource,
        systemInfoDataSource: SystemInfoDataSource
    ): DiagnosticDataSource {
        return DiagnosticDataSource(
            context,
            cpuDataSource,
            ramDataSource,
            storageDataSource,
            powerDataSource,
            systemInfoDataSource
        )
    }
}