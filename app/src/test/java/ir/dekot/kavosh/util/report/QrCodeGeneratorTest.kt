package ir.dekot.kavosh.util.report

import android.content.Context
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.*
import io.mockk.mockk
import org.junit.Test
import org.junit.Assert.*

/**
 * تست برای QrCodeGenerator
 */
class QrCodeGeneratorTest {

    private val mockContext = mockk<Context>(relaxed = true)

    @Test
    fun `test createSimpleQrCode returns valid bitmap`() {
        val testData = "Test QR Code Data"
        val bitmap = QrCodeGenerator.createSimpleQrCode(testData)
        
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun `test generateQuickShareQrCode with complete device info`() {
        val deviceInfo = DeviceInfo(
            cpu = CpuInfo(model = "Test CPU"),
            gpu = GpuInfo(model = "Test GPU"),
            ram = RamInfo(total = "8 GB"),
            storage = StorageInfo(total = "128 GB"),
            display = DisplayInfo(resolution = "1080x2400"),
            system = SystemInfo(androidVersion = "14")
        )
        
        val batteryInfo = BatteryInfo(
            level = 85,
            status = "Charging"
        )

        val bitmap = QrCodeGenerator.generateQuickShareQrCode(mockContext, deviceInfo, batteryInfo)
        
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }

    @Test
    fun `test generateShareUrl creates valid URL`() {
        val testData = "Test data for URL generation"
        val url = QrCodeGenerator.generateShareUrl(testData)
        
        assertTrue(url.startsWith("https://kavosh.app/share?data="))
        assertTrue(url.contains("data="))
    }

    @Test
    fun `test generateShareableQrCode returns valid bitmap`() {
        val deviceInfo = DeviceInfo()
        val batteryInfo = BatteryInfo()

        val bitmap = QrCodeGenerator.generateShareableQrCode(mockContext, deviceInfo, batteryInfo)
        
        assertNotNull(bitmap)
        assertTrue(bitmap.width > 0)
        assertTrue(bitmap.height > 0)
    }
}
