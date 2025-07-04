package ir.dekot.kavosh.ui.screen.share

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.viewmodel.ExportFormat
import ir.dekot.kavosh.ui.viewmodel.ExportViewModel
import kotlinx.coroutines.launch

/**
 * صفحه اشتراک‌گذاری و خروجی
 * شامل گزینه‌های مختلف برای اشتراک‌گذاری اطلاعات دستگاه
 */
@Composable
fun ShareScreen(
    exportViewModel: ExportViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // مشاهده نتایج خروجی
    LaunchedEffect(Unit) {
        exportViewModel.exportResult.collect { result ->
            val message = when (result) {
                is ir.dekot.kavosh.ui.viewmodel.ExportResult.Success -> result.message
                is ir.dekot.kavosh.ui.viewmodel.ExportResult.Failure -> result.message
            }
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // هدر صفحه
            Text(
                text = stringResource(R.string.share_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = stringResource(R.string.share_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // بخش اشتراک‌گذاری سریع
            QuickShareSection(
                onQuickShare = {
                    exportViewModel.onQuickShareRequested()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // بخش خروجی فایل
            Text(
                text = "فرمت‌های خروجی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // لیست فرمت‌های خروجی
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(getExportOptions()) { option ->
                    ExportOptionCard(
                        title = stringResource(option.titleResId),
                        description = option.description,
                        icon = option.icon,
                        onClick = {
                            exportViewModel.onExportRequested(option.format)
                        }
                    )
                }
            }
        }
        
        // Snackbar برای نمایش پیام‌ها
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * بخش اشتراک‌گذاری سریع
 */
@Composable
private fun QuickShareSection(
    onQuickShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = stringResource(R.string.quick_share),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = stringResource(R.string.quick_share_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onQuickShare,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("اشتراک‌گذاری سریع")
            }
        }
    }
}

/**
 * کارت هر گزینه خروجی
 */
@Composable
private fun ExportOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * مدل داده برای گزینه‌های خروجی
 */
private data class ExportOption(
    val format: ExportFormat,
    val titleResId: Int,
    val description: String,
    val icon: ImageVector
)

/**
 * لیست گزینه‌های خروجی
 */
private fun getExportOptions(): List<ExportOption> = listOf(
    ExportOption(
        format = ExportFormat.TXT,
        titleResId = R.string.export_format_txt,
        description = "فایل متنی ساده برای مشاهده در هر ویرایشگر",
        icon = Icons.Default.TextSnippet
    ),
    ExportOption(
        format = ExportFormat.PDF,
        titleResId = R.string.export_format_pdf,
        description = "سند PDF با فرمت‌بندی زیبا",
        icon = Icons.Default.PictureAsPdf
    ),
    ExportOption(
        format = ExportFormat.JSON,
        titleResId = R.string.export_format_json,
        description = "داده ساختاریافته برای توسعه‌دهندگان",
        icon = Icons.Default.Code
    )
)
