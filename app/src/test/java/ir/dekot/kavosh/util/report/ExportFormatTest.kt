package ir.dekot.kavosh.util.report

import ir.dekot.kavosh.ui.viewmodel.ExportFormat
import org.junit.Test
import org.junit.Assert.*

/**
 * تست برای فرمت‌های جدید export
 */
class ExportFormatTest {

    @Test
    fun `test all export formats have correct extensions`() {
        assertEquals("txt", ExportFormat.TXT.extension)
        assertEquals("pdf", ExportFormat.PDF.extension)
        assertEquals("json", ExportFormat.JSON.extension)
        assertEquals("html", ExportFormat.HTML.extension)
        assertEquals("xlsx", ExportFormat.EXCEL.extension)
        assertEquals("png", ExportFormat.QR_CODE.extension)
    }

    @Test
    fun `test all export formats have correct mime types`() {
        assertEquals("text/plain", ExportFormat.TXT.mimeType)
        assertEquals("application/pdf", ExportFormat.PDF.mimeType)
        assertEquals("application/json", ExportFormat.JSON.mimeType)
        assertEquals("text/html", ExportFormat.HTML.mimeType)
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ExportFormat.EXCEL.mimeType)
        assertEquals("image/png", ExportFormat.QR_CODE.mimeType)
    }

    @Test
    fun `test export format count`() {
        assertEquals(6, ExportFormat.entries.size)
    }
}
