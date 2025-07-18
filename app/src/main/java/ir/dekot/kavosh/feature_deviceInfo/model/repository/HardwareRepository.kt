package ir.dekot.kavosh.feature_deviceInfo.model.repository

import android.app.Activity
import androidx.annotation.RequiresApi
import ir.dekot.kavosh.feature_deviceInfo.model.CpuInfo
import ir.dekot.kavosh.feature_deviceInfo.model.DisplayInfo
import ir.dekot.kavosh.feature_deviceInfo.model.GpuInfo
import ir.dekot.kavosh.feature_deviceInfo.model.MemoryDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.RamInfo
import ir.dekot.kavosh.feature_deviceInfo.model.SensorInfo
import ir.dekot.kavosh.feature_deviceInfo.model.SocDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.StorageInfo
import ir.dekot.kavosh.feature_deviceInfo.model.SystemDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.ThermalInfo
import ir.dekot.kavosh.feature_deviceInfo.model.PowerDataSource

import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن اطلاعات سخت‌افزار - مسئول مدیریت اطلاعات مربوط به سخت‌افزار دستگاه
 * شامل پردازنده، کارت گرافیک، حافظه، نمایشگر، حسگرها و اطلاعات حرارتی
 */
@Singleton
class HardwareRepository @Inject constructor(
    private val socDataSource: SocDataSource,
    private val systemDataSource: SystemDataSource,
    private val memoryDataSource: MemoryDataSource,
    private val powerDataSource: PowerDataSource,
) {

    // --- اطلاعات پردازنده و کارت گرافیک ---
    
    /**
     * دریافت اطلاعات پردازنده
     * @return اطلاعات کامل CPU شامل تعداد هسته، معماری و فرکانس
     */
    fun getCpuInfo(): CpuInfo = socDataSource.getCpuInfo()

    /**
     * دریافت فرکانس زنده هسته‌های پردازنده
     * @return لیست فرکانس فعلی هر هسته
     */
    fun getLiveCpuFrequencies(): List<String> = socDataSource.getLiveCpuFrequencies()

    /**
     * دریافت درصد بار کارت گرافیک
     * @return درصد استفاده از GPU یا null در صورت عدم دسترسی
     */
    fun getGpuLoadPercentage(): Int? = socDataSource.getGpuLoadPercentage()

    /**
     * دریافت اطلاعات کارت گرافیک
     * @param activity Activity مورد نیاز برای دسترسی به OpenGL
     * @return اطلاعات کامل GPU
     */
    suspend fun getGpuInfo(activity: Activity): GpuInfo = socDataSource.getGpuInfo(activity)

    // --- اطلاعات حافظه و ذخیره‌سازی ---
    
    /**
     * دریافت اطلاعات حافظه RAM
     * @return اطلاعات کامل RAM شامل کل، استفاده شده و آزاد
     */
    fun getRamInfo(): RamInfo = memoryDataSource.getRamInfo()

    /**
     * دریافت اطلاعات ذخیره‌سازی
     * @return اطلاعات کامل حافظه داخلی و خارجی
     */
    fun getStorageInfo(): StorageInfo = memoryDataSource.getStorageInfo()

    // --- اطلاعات نمایشگر ---
    
    /**
     * دریافت اطلاعات نمایشگر
     * @param activity Activity مورد نیاز برای دسترسی به اطلاعات نمایش
     * @return اطلاعات کامل نمایشگر شامل رزولوشن، DPI و نرخ تازه‌سازی
     */
    @RequiresApi(30)
    fun getDisplayInfo(activity: Activity): DisplayInfo = systemDataSource.getDisplayInfo(activity)

    // --- اطلاعات حسگرها ---
    
    /**
     * دریافت لیست حسگرهای دستگاه
     * @param activity Activity مورد نیاز برای دسترسی به حسگرها
     * @return لیست کامل حسگرهای موجود در دستگاه
     */
    fun getSensorInfo(activity: Activity): List<SensorInfo> = systemDataSource.getSensorInfo(activity)

    // --- اطلاعات حرارتی ---
    
    /**
     * دریافت اطلاعات حرارتی دستگاه
     * @return لیست اطلاعات حرارتی از منابع مختلف
     */
    fun getThermalInfo(): List<ThermalInfo> = powerDataSource.getThermalInfo()
}
