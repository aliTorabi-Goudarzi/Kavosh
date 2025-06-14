package ir.dekot.kavosh.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.dekot.kavosh.data.repository.DeviceInfoRepository
import javax.inject.Singleton

/**
 * Hilt Module برای تعریف نحوه ساخت وابستگی‌های سراسری اپلیکیشن.
 *
 * @Module: این انوتیشن به Hilt می‌گوید که این کلاس یک ماژول است.
 * @InstallIn(SingletonComponent::class): این ماژول را در کامپوننت Singleton نصب می‌کند،
 * یعنی وابستگی‌های تعریف شده در این ماژول در تمام طول عمر اپلیکیشن زنده خواهند ماند.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * این تابع به Hilt یاد می‌دهد که چگونه یک نمونه از DeviceInfoRepository بسازد.
     *
     * @Provides: نشان می‌دهد که این تابع یک وابستگی را "فراهم" می‌کند.
     * @Singleton: تضمین می‌کند که فقط یک نمونه (instance) از DeviceInfoRepository
     * در کل اپلیکیشن ساخته شود (الگوی Singleton).
     * @ApplicationContext: Hilt به طور خودکار Context سطح اپلیکیشن را به این تابع تزریق می‌کند.
     */
    @Provides
    @Singleton
    fun provideDeviceInfoRepository(@ApplicationContext context: Context): DeviceInfoRepository {
        return DeviceInfoRepository(context)
    }
}

