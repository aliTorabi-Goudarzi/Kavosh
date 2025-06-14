package ir.dekot.kavosh.util

import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * این کلاس شامل تست‌های واحد برای آبجکت InfoFormatter است.
 */
class InfoFormatterTest {

    /**
     * هر متدی که با انوتیشن @Test مشخص شود، به عنوان یک سناریوی تست مجزا اجرا می‌شود.
     * نام متد باید به وضوح بیان کند که چه چیزی را تست می‌کند.
     */
    @Test
    fun formatInfoForSharing_socCategory_returnsCorrectString() {
        // 1. Arrange (آماده‌سازی)
        // در این بخش، ما داده‌های ساختگی و ورودی‌های لازم برای تست را آماده می‌کنیم.
        val sampleDeviceInfo = DeviceInfo(
            cpu = CpuInfo(model = "Test CPU Model", architecture = "ARMv8", topology = "8 Cores"),
            gpu = GpuInfo(model = "Test GPU Model", vendor = "Test Vendor"),
            ram = RamInfo(total = "8 GB", available = "4 GB")
        )
        val sampleBatteryInfo = BatteryInfo() // برای این تست به اطلاعات باتری نیازی نداریم

        // 2. Act (اجرا)
        // متدی که می‌خواهیم آن را تست کنیم، با داده‌های ساختگی فراخوانی می‌کنیم.
        val resultString = InfoFormatter.formatInfoForSharing(
            category = InfoCategory.SOC,
            deviceInfo = sampleDeviceInfo,
            batteryInfo = sampleBatteryInfo
        )

        // 3. Assert (بررسی صحت)
        // در این بخش، بررسی می‌کنیم که آیا خروجی به دست آمده با چیزی که انتظار داشتیم، مطابقت دارد یا نه.
        // به جای مقایسه کل رشته که شکننده است، بررسی می‌کنیم که آیا اطلاعات کلیدی در آن وجود دارند یا نه.
        println("Generated String for SOC:\n$resultString") // این خط برای دیباگ کردن و دیدن خروجی در کنسول است

        assertTrue(resultString.contains("--- پردازنده (SOC) ---"))
        assertTrue(resultString.contains("مدل CPU: Test CPU Model"))
        assertTrue(resultString.contains("معماری: ARMv8"))
        assertTrue(resultString.contains("مدل GPU: Test GPU Model"))
        assertTrue(resultString.contains("کل حافظه: 8 GB"))
        assertTrue(resultString.contains("ارسال شده توسط اپلیکیشن کاوش"))
    }

    /**
     * می‌توانیم برای هر دسته‌بندی یک تست جداگانه بنویسیم.
     */
    @Test
    fun formatInfoForSharing_batteryCategory_returnsCorrectString() {
        // 1. Arrange
        val sampleDeviceInfo = DeviceInfo() // برای این تست به این اطلاعات نیازی نداریم
        val sampleBatteryInfo = BatteryInfo(
            level = 95,
            health = "خوب",
            status = "در حال شارژ",
            temperature = "35.0 °C"
        )

        // 2. Act
        val resultString = InfoFormatter.formatInfoForSharing(
            category = InfoCategory.BATTERY,
            deviceInfo = sampleDeviceInfo,
            batteryInfo = sampleBatteryInfo
        )

        // 3. Assert
        println("Generated String for Battery:\n$resultString")

        assertTrue(resultString.contains("--- باتری ---"))
        assertTrue(resultString.contains("درصد شارژ: 95%"))
        assertTrue(resultString.contains("وضعیت شارژ: در حال شارژ"))
        assertTrue(resultString.contains("دما: 35.0 °C"))
    }
}

