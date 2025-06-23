package ir.dekot.kavosh.ui.screen.shared // <-- تغییر در این خط

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * این فایل شامل کامپوزبل‌های اشتراکی است که در صفحات مختلف استفاده می‌شوند.
 */

/**
 * یک تابع کمکی برای نمایش عنوان‌های داخل کارت‌های اطلاعاتی.
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

/**
 * یک کامپوننت برای نمایش یک پیام متنی در مرکز صفحه، مناسب برای وضعیت‌های خالی.
 */
@Composable
fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}