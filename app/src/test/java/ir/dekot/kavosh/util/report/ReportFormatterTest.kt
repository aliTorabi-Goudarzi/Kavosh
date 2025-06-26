package ir.dekot.kavosh.util.report

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * این کلاس شامل تست‌های واحد برای آبجکت ReportFormatter است.
 */
class ReportFormatterTest {

    // ما به یک Context ساختگی (Mock) برای تست نیاز داریم
    private lateinit var mockContext: Context

    // این تابع قبل از هر تست اجرا می‌شود و Context ساختگی را آماده می‌کند
    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        // به Context ساختگی یاد می‌دهیم که وقتی از او رشته‌ای خواسته شد، چه چیزی را برگرداند
        every { mockContext.getString(R.string.cpu_model) } returns "Model"
        every { mockContext.getString(R.string.cpu_architecture) } returns "Architecture"
        // ... می‌توانیم برای تمام رشته‌های دیگر هم همین کار را بکنیم
    }


    /**
     * هر متدی که با انوتیشن @Test مشخص شود، به عنوان یک سناریوی تست مجزا اجرا می‌شود.
     * نام متد باید به وضوح بیان کند که چه چیزی را تست می‌کند.
     */
    @Test
    fun getCategoryData_forSocCategory_returnsCorrectLabelsAndValues() {
        // 1. Arrange (آماده‌سازی)
        // در این بخش، ما داده‌های ساختگی و ورودی‌های لازم برای تست را آماده می‌کنیم.
        val sampleDeviceInfo = DeviceInfo(
            cpu = CpuInfo(model = "Test CPU", architecture = "ARMv9"),
        )
        val sampleBatteryInfo = BatteryInfo() // برای این تست به اطلاعات باتری نیازی نداریم

        // 2. Act (اجرا)
        // متدی که می‌خواهیم آن را تست کنیم، با داده‌های ساختگی فراخوانی می‌کنیم.
        val resultData = ReportFormatter.getCategoryData(
            context = mockContext,
            category = InfoCategory.SOC,
            deviceInfo = sampleDeviceInfo,
            batteryInfo = sampleBatteryInfo
        )

        // 3. Assert (بررسی صحت)
        // در این بخش، بررسی می‌کنیم که آیا خروجی به دست آمده با چیزی که انتظار داشتیم، مطابقت دارد یا نه.
        val resultString = resultData.joinToString("\n") { "${it.first}: ${it.second}" }
        println("Generated Data:\n$resultString") // این خط برای دیباگ کردن و دیدن خروجی در کنسول است

        assertTrue(resultString.contains("Model: Test CPU"))
        assertTrue(resultString.contains("Architecture: ARMv9"))
    }
}