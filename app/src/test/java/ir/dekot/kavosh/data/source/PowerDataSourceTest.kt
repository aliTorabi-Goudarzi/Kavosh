package ir.dekot.kavosh.data.source

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import io.mockk.every
import io.mockk.mockk
import ir.dekot.kavosh.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PowerDataSourceTest {

    private lateinit var mockContext: Context
    private lateinit var powerDataSource: PowerDataSource

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        powerDataSource = PowerDataSource(mockContext)

        every { mockContext.getString(R.string.battery_health_good) } returns "Good"
        every { mockContext.getString(R.string.battery_status_charging) } returns "Charging"
        every { mockContext.getString(R.string.unit_format_celsius, 35.5f) } returns "35.5 °C"
        every { mockContext.getString(R.string.unit_format_volt, 4.2f) } returns "4.20 V"
        every { mockContext.getString(R.string.label_undefined) } returns "Undefined"
    }

    @Test
    fun `getBatteryInfo should correctly parse data from intent`() {
        // 1. Arrange
        // *** تغییر کلیدی: ساختن یک Intent ساختگی (Mock) ***
        val mockIntent = mockk<Intent>()

        // به Intent ساختگی یاد می‌دهیم که در پاسخ به هر فراخوانی چه مقداری را برگرداند
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) } returns 95
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) } returns BatteryManager.BATTERY_HEALTH_GOOD
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns BatteryManager.BATTERY_STATUS_CHARGING
        every { mockIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) } returns "Li-ion"
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) } returns 355
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) } returns 4200

        // 2. Act
        val batteryInfo = powerDataSource.getBatteryInfo(mockIntent)

        // 3. Assert
        assertEquals(95, batteryInfo.level)
        assertEquals("Good", batteryInfo.health)
        assertEquals("Charging", batteryInfo.status)
        assertEquals("Li-ion", batteryInfo.technology)
        assertEquals("35.5 °C", batteryInfo.temperature)
        assertEquals("4.20 V", batteryInfo.voltage)
    }

    @Test
    fun `getBatteryInfo should handle missing data gracefully`() {
        // 1. Arrange
        val mockIntent = mockk<Intent>()

        // این بار، مقادیر پیش‌فرض را برمی‌گردانیم
        every { mockIntent.getIntExtra(any(), any()) } returns -1 // برای هر کلیدی، -1 برگردان
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) } returns 0
        every { mockIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) } returns -1
        every { mockIntent.getStringExtra(any()) } returns null // برای هر کلیدی، null برگردان

        // 2. Act
        val batteryInfo = powerDataSource.getBatteryInfo(mockIntent)

        // 3. Assert
        assertEquals(-1, batteryInfo.level)
        assertEquals("Undefined", batteryInfo.health)
        assertEquals("Undefined", batteryInfo.status)
        assertEquals("Undefined", batteryInfo.technology)
    }
}