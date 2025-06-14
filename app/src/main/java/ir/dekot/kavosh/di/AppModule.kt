package ir.dekot.kavosh.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import ir.dekot.kavosh.data.source.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * این تابع به Hilt یاد می‌دهد که چگونه یک نمونه از DeviceInfoRepository بسازد.
     * Hilt به صورت خودکار تمام پارامترهای این تابع (DataSourceها) را فراهم می‌کند.
     */
    @Provides
    @Singleton
    fun provideDeviceInfoRepository(
        powerDataSource: PowerDataSource,
        socDataSource: SocDataSource,
        systemDataSource: SystemDataSource,
        memoryDataSource: MemoryDataSource,
        settingsDataSource: SettingsDataSource
    ): DeviceInfoRepository {
        return DeviceInfoRepository(
            powerDataSource,
            socDataSource,
            systemDataSource,
            memoryDataSource,
            settingsDataSource
        )
    }

    @Provides
    @Singleton
    fun providePowerDataSource(@ApplicationContext context: Context): PowerDataSource {
        return PowerDataSource(context)
    }

    /**
     * این تابع به Hilt یاد می‌دهد که چگونه SocDataSource را بسازد.
     * از آنجایی که constructor این کلاس دیگر ورودی ندارد، این تابع هم ورودی نیاز ندارد.
     */
    @Provides
    @Singleton
    fun provideSocDataSource(): SocDataSource {
        return SocDataSource() // بدون پاس دادن context
    }

    /**
     * این تابع به Hilt یاد می‌دهد که چگونه SystemDataSource را بسازد.
     * از آنجایی که constructor این کلاس دیگر ورودی ندارد، این تابع هم ورودی نیاز ندارد.
     */
    @Provides
    @Singleton
    fun provideSystemDataSource(): SystemDataSource {
        return SystemDataSource() // بدون پاس دادن context
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
}

