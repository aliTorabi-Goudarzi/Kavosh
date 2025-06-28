package ir.dekot.kavosh.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.data.source.*
import ir.dekot.kavosh.domain.sensor.SensorHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSimDataSource(@ApplicationContext context: Context): SimDataSource {
        return SimDataSource(context)
    }

    /**
     * این تابع به Hilt یاد می‌دهد که چگونه یک نمونه از DeviceInfoRepository بسازد.
     * Hilt به صورت خودکار تمام پارامترهای این تابع (DataSourceها) را فراهم می‌کند.
     */
    @Provides
    @Singleton
    fun provideNetworkDataSource(@ApplicationContext context: Context): NetworkDataSource {
        return NetworkDataSource(context)
    }

    // **اصلاح ۱: افزودن provider برای سورس جدید**
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
    fun provideDeviceInfoRepository(
        powerDataSource: PowerDataSource,
        socDataSource: SocDataSource,
        systemDataSource: SystemDataSource,
        memoryDataSource: MemoryDataSource,
        settingsDataSource: SettingsDataSource,
        networkDataSource: NetworkDataSource, // <-- اضافه کردن به پارامترها
        simDataSource: SimDataSource, // <-- تزریق جدید
        cameraDataSource: CameraDataSource, // <-- اضافه کردن به پارامترها
        // **اصلاح ۲: افزودن وابستگی جدید به پارامترها**
        networkToolsDataSource: NetworkToolsDataSource
    ): DeviceInfoRepository {
        return DeviceInfoRepository(
            powerDataSource,
            socDataSource,
            systemDataSource,
            memoryDataSource,
            settingsDataSource,
            networkDataSource, // <-- پاس دادن به constructor
            cameraDataSource,// <-- پاس دادن به constructor
            // **اصلاح ۳: پاس دادن وابستگی جدید به constructor**
            networkToolsDataSource,
            simDataSource // <-- پاس دادن به constructor
        )
    }


    @Provides
    @Singleton
    fun providePowerDataSource(@ApplicationContext context: Context): PowerDataSource {
        return PowerDataSource(context)
    }

    /**
     * *** تغییر کلیدی: ***
     * این تابع حالا Context را به عنوان ورودی می‌گیرد و به SocDataSource پاس می‌دهد.
     */
    @Provides
    @Singleton
    fun provideSocDataSource(@ApplicationContext context: Context): SocDataSource {
        return SocDataSource(context)
    }

    /**
     * *** تغییر کلیدی: ***
     * این تابع حالا Context را به عنوان ورودی می‌گیرد و به SystemDataSource پاس می‌دهد.
     */
    @Provides
    @Singleton
    fun provideSystemDataSource(@ApplicationContext context: Context): SystemDataSource {
        return SystemDataSource(context)
    }

    // این تابع قبلا بدون پارامتر بود، حالا context را می‌گیرد
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

    /**
     * این تابع به Hilt یاد می‌دهد که چگونه یک نمونه از SensorHandler بسازد.
     * چون SensorHandler خودش با @Inject مشخص شده، این متد ساده است.
     */
    @Provides
    @Singleton
    fun provideSensorHandler(@ApplicationContext context: Context): SensorHandler {
        return SensorHandler(context)
    }
}

