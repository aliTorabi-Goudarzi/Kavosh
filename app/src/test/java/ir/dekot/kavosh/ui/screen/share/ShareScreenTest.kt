package ir.dekot.kavosh.ui.screen.share

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ir.dekot.kavosh.ui.viewmodel.ExportViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * تست برای ShareScreen
 */
@RunWith(AndroidJUnit4::class)
class ShareScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shareScreen_displaysCorrectTitle() {
        val mockExportViewModel = mockk<ExportViewModel>(relaxed = true)

        composeTestRule.setContent {
            ShareScreen(exportViewModel = mockExportViewModel)
        }

        composeTestRule
            .onNodeWithText("خروجی و اشتراک‌گذاری")
            .assertIsDisplayed()
    }

    @Test
    fun shareScreen_displaysDeviceSaveSection() {
        val mockExportViewModel = mockk<ExportViewModel>(relaxed = true)

        composeTestRule.setContent {
            ShareScreen(exportViewModel = mockExportViewModel)
        }

        composeTestRule
            .onNodeWithText("ذخیره اطلاعات دستگاه")
            .assertIsDisplayed()
    }

    @Test
    fun shareScreen_displaysQuickShareButtons() {
        val mockExportViewModel = mockk<ExportViewModel>(relaxed = true)

        composeTestRule.setContent {
            ShareScreen(exportViewModel = mockExportViewModel)
        }

        composeTestRule
            .onNodeWithText("اشتراک‌گذاری متنی")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("اشتراک‌گذاری QR Code")
            .assertIsDisplayed()
    }

    @Test
    fun shareScreen_displaysSaveButton() {
        val mockExportViewModel = mockk<ExportViewModel>(relaxed = true)

        composeTestRule.setContent {
            ShareScreen(exportViewModel = mockExportViewModel)
        }

        composeTestRule
            .onNodeWithText("ذخیره فایل")
            .assertIsDisplayed()
    }
}
