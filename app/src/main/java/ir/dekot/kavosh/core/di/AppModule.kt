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
import ir.dekot.kavosh.feature_deviceInfo.model.MemoryDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.NetworkDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.PowerDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.SimDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.SocDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.SystemDataSource
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

    @Provides
    @Singleton
    fun provideSocDataSource(@ApplicationContext context: Context): SocDataSource {
        return SocDataSource(context)
    }

    @Provides
    @Singleton
    fun provideSystemDataSource(@ApplicationContext context: Context): SystemDataSource {
        return SystemDataSource(context)
    }

    @Provides
    @Singleton
    fun provideMemoryDataSource(@ApplicationContext context: Context): MemoryDataSource {
        return MemoryDataSource(context)
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
        socDataSource: SocDataSource,
        systemDataSource: SystemDataSource,
        memoryDataSource: MemoryDataSource,
        powerDataSource: PowerDataSource,
    ): HardwareRepository {
        return HardwareRepository(socDataSource, systemDataSource, memoryDataSource, powerDataSource)
    }

    @Provides
    @Singleton
    fun provideSystemRepository(
        systemDataSource: SystemDataSource
    ): SystemRepository {
        return SystemRepository(systemDataSource)
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
        memoryDataSource: MemoryDataSource,
        settingsDataSource: SettingsDataSource
    ): TestingRepository {
        return TestingRepository(memoryDataSource, settingsDataSource)
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
        socDataSource: SocDataSource,
        memoryDataSource: MemoryDataSource,
        powerDataSource: PowerDataSource,
        systemDataSource: SystemDataSource
    ): DiagnosticDataSource {
        return DiagnosticDataSource(
            context,
            socDataSource,
            memoryDataSource,
            powerDataSource,
            systemDataSource
        )
    }
}