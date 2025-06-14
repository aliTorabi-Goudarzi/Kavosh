package ir.dekot.kavosh.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * این فایل شامل کامپوزبل‌های اشتراکی است که در صفحات مختلف استفاده می‌شوند.
 */

/**
 * یک تابع کمکی برای نمایش عنوان‌های داخل کارت‌های اطلاعاتی.
 * این تابع حالا عمومی است و از هر فایلی قابل دسترسی است.
 */
@Composable
fun SectionTitleInCard(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}