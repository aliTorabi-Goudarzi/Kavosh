package ir.dekot.kavosh.ui.screen.share

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TextSnippet
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
                },
                onQrCodeShare = {
                    exportViewModel.onQrCodeShareRequested()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // بخش ذخیره اطلاعات دستگاه
            Text(
                text = stringResource(R.string.save_device_info),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // کارت انتخاب فرمت و ذخیره
            ExportFormatSelector(
                exportViewModel = exportViewModel
            )
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
    onQuickShare: () -> Unit,
    onQrCodeShare: () -> Unit
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

            // دکمه اشتراک‌گذاری متنی
            Button(
                onClick = onQuickShare,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("اشتراک‌گذاری متنی")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // دکمه اشتراک‌گذاری QR Code
            OutlinedButton(
                onClick = onQrCodeShare,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("اشتراک‌گذاری QR Code")
            }
        }
    }
}

/**
 * کامپوننت انتخاب فرمت و ذخیره
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportFormatSelector(
    exportViewModel: ExportViewModel
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // آیکون و عنوان
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "ذخیره گزارش کامل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "تمام اطلاعات دستگاه را در فرمت دلخواه ذخیره کنید",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // منوی انتخاب فرمت
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = getFormatDisplayName(selectedFormat),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("انتخاب فرمت") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    getExportOptions().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(option.titleResId),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = option.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedFormat = option.format
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // دکمه ذخیره
            Button(
                onClick = {
                    exportViewModel.onExportRequested(selectedFormat)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره فایل")
            }
        }
    }
}

/**
 * دریافت نام نمایشی فرمت
 */
@Composable
private fun getFormatDisplayName(format: ExportFormat): String {
    return when (format) {
        ExportFormat.TXT -> stringResource(R.string.export_format_txt)
        ExportFormat.PDF -> stringResource(R.string.export_format_pdf)
        ExportFormat.JSON -> stringResource(R.string.export_format_json)
        ExportFormat.HTML -> stringResource(R.string.export_format_html)
        ExportFormat.EXCEL -> stringResource(R.string.export_format_excel)
        ExportFormat.QR_CODE -> stringResource(R.string.export_format_qr_code)
    }
}

/**
 * کارت هر گزینه خروجی (حفظ شده برای سازگاری)
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
        format = ExportFormat.PDF,
        titleResId = R.string.export_format_pdf,
        description = "سند PDF با فرمت‌بندی زیبا و حرفه‌ای",
        icon = Icons.Default.PictureAsPdf
    ),
    ExportOption(
        format = ExportFormat.HTML,
        titleResId = R.string.export_format_html,
        description = "گزارش وب زیبا قابل مشاهده در مرورگر",
        icon = Icons.Default.Language
    ),
    ExportOption(
        format = ExportFormat.EXCEL,
        titleResId = R.string.export_format_excel,
        description = "فایل اکسل با جداول و فرمت‌بندی",
        icon = Icons.Default.TableChart
    ),
    ExportOption(
        format = ExportFormat.JSON,
        titleResId = R.string.export_format_json,
        description = "داده ساختاریافته برای توسعه‌دهندگان",
        icon = Icons.Default.Code
    ),
    ExportOption(
        format = ExportFormat.TXT,
        titleResId = R.string.export_format_txt,
        description = "فایل متنی ساده برای مشاهده سریع",
        icon = Icons.AutoMirrored.Filled.TextSnippet
    ),
    ExportOption(
        format = ExportFormat.QR_CODE,
        titleResId = R.string.export_format_qr_code,
        description = "کد QR برای اشتراک‌گذاری سریع",
        icon = Icons.Default.QrCode
    )
)
