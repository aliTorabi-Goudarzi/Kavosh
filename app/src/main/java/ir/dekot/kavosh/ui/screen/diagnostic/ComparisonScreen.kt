package ir.dekot.kavosh.ui.screen.diagnostic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.diagnostic.ComparisonCategory
import ir.dekot.kavosh.data.model.diagnostic.ComparisonResult
import ir.dekot.kavosh.data.model.diagnostic.DeviceComparison
import ir.dekot.kavosh.data.model.diagnostic.DeviceProfile
import ir.dekot.kavosh.ui.composables.KavoshTopAppBar
import ir.dekot.kavosh.ui.composables.ProfessionalLoadingIndicator
import ir.dekot.kavosh.ui.viewmodel.DiagnosticViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportFormat

/**
 * صفحه مقایسه دستگاه با دستگاه‌های مشابه
 * نمایش مقایسه مشخصات و عملکرد
 */
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    onBackClick: () -> Unit,
    viewModel: DiagnosticViewModel = hiltViewModel()
) {
    // مدیریت export events
    DiagnosticExportHandler()
    val deviceComparison by viewModel.deviceComparison.collectAsState()
    val isLoading by viewModel.isDeviceComparisonLoading.collectAsState()
    val comparisonHistory by viewModel.deviceComparisonHistory.collectAsState()

    var showStartButton by remember { mutableStateOf(deviceComparison == null) }

    Scaffold(
        topBar = {
            // استفاده از نوار بالایی سفارشی برای یکپارچگی رنگی
            KavoshTopAppBar(
                title = stringResource(R.string.device_comparison_title),
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.device_comparison_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // دکمه شروع تست یا نتایج
            if (showStartButton && !isLoading) {
                item {
                    ComparisonStartTestCard(
                        onStartTest = {
                            showStartButton = false
                            viewModel.compareDevice()
                        }
                    )
                }
            }

            if (isLoading) {
                item {
                    ComparisonLoadingCard()
                }
            } else {
                deviceComparison?.let { comparison ->
                    item {
                        CurrentDeviceCard(comparison.currentDevice)
                    }

                    item {
                        OverallComparisonCard(
                            comparison = comparison.overallComparison,
                            onRunNewTest = {
                                showStartButton = false
                                viewModel.compareDevice()
                            }
                        )
                    }

                    items(comparison.comparisonResults) { result ->
                        ComparisonResultCard(result)
                    }

                    if (comparison.comparedDevices.isNotEmpty()) {
                        item {
                            ComparedDevicesCard(comparison.comparedDevices)
                        }
                    }

                    item {
                        ComparisonTestActionsCard(
                            onRunNewTest = {
                                showStartButton = false
                                viewModel.compareDevice()
                            }
                        )
                    }
                }
            }

            // تاریخچه مقایسه‌ها
            if (comparisonHistory.isNotEmpty()) {
                item {
                    ComparisonHistorySection(
                        history = comparisonHistory,
                        onHistoryItemClick = { comparison ->
                            // TODO: نمایش جزئیات مقایسه قبلی
                        }
                    )
                }
            }
        }
    }
}

/**
 * کارت شروع مقایسه
 */
@Composable
private fun ComparisonStartTestCard(
    onStartTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Compare,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.device_comparison_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.device_comparison_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartTest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.device_comparison_start_test))
            }
        }
    }
}

/**
 * کارت بارگذاری مقایسه با انیمیشن حرفه‌ای
 */
@Composable
private fun ComparisonLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // استفاده از انیمیشن بارگذاری حرفه‌ای
            ProfessionalLoadingIndicator(
                size = 64.dp,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.device_comparison_comparing),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.device_comparison_analyzing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * کارت دستگاه فعلی
 */
@Composable
private fun CurrentDeviceCard(device: DeviceProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(R.string.comparison_current_device),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = device.deviceName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // مشخصات کلیدی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecItem(
                    label = "CPU",
                    value = "${device.specifications.cpu.cores} cores",
                    icon = Icons.Default.Memory
                )
                SpecItem(
                    label = "RAM",
                    value = "${device.specifications.ram.totalSize / 1024} GB",
                    icon = Icons.Default.Storage
                )
                SpecItem(
                    label = "Score",
                    value = "${device.performanceScore}",
                    icon = Icons.Default.Speed
                )
            }
        }
    }
}

/**
 * کارت مقایسه کلی
 */
@Composable
private fun OverallComparisonCard(
    comparison: ir.dekot.kavosh.data.model.diagnostic.OverallComparison,
    onRunNewTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.comparison_overall_comparison),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.comparison_ranking, comparison.overallRanking, comparison.totalDevices),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.comparison_percentile, (100 - comparison.percentile).toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // نقاط قوت
            if (comparison.strengths.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.comparison_strengths),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(4.dp))
                comparison.strengths.forEach { strength ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strength,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // نقاط ضعف
            if (comparison.weaknesses.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.comparison_weaknesses),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(4.dp))
                comparison.weaknesses.forEach { weakness ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = weakness,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // توصیه
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = comparison.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // دکمه مقایسه جدید در کارت اصلی
            OutlinedButton(
                onClick = onRunNewTest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("مقایسه جدید")
            }
        }
    }
}

/**
 * کارت نتیجه مقایسه هر دسته
 */
@Composable
private fun ComparisonResultCard(result: ComparisonResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = getCategoryDisplayName(result.category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = result.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "#${result.ranking}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = getRankingColor(result.ranking, result.totalDevices)
                    )
                    Text(
                        text = "of ${result.totalDevices}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // نوار مقایسه
            ComparisonBar(
                currentValue = result.currentScore,
                averageValue = result.averageScore,
                bestValue = result.bestScore,
                worstValue = result.worstScore,
                unit = result.unit
            )
        }
    }
}

/**
 * نوار مقایسه
 */
@Composable
private fun ComparisonBar(
    currentValue: Double,
    averageValue: Double,
    bestValue: Double,
    worstValue: Double,
    unit: String
) {
    val range = bestValue - worstValue
    val currentPosition = if (range > 0) ((currentValue - worstValue) / range).coerceIn(0.0, 1.0) else 0.5
    val averagePosition = if (range > 0) ((averageValue - worstValue) / range).coerceIn(0.0, 1.0) else 0.5

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${currentValue.toInt()} $unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Avg: ${averageValue.toInt()} $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            // نوار پیشرفت تا موقعیت فعلی
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(currentPosition.toFloat())
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            // نشانگر میانگین
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .offset(x = (averagePosition * 300).dp) // تقریبی
                    .background(Color.Red)
            )
        }
    }
}

/**
 * کارت دستگاه‌های مقایسه شده
 */
@Composable
private fun ComparedDevicesCard(devices: List<DeviceProfile>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.comparison_compared_devices),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            devices.forEach { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = device.deviceName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = device.manufacturer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${device.performanceScore}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (device != devices.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

/**
 * آیتم مشخصات
 */
@Composable
private fun SpecItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// توابع کمکی
private fun getCategoryDisplayName(category: ComparisonCategory): String = when (category) {
    ComparisonCategory.CPU_PERFORMANCE -> "CPU Performance"
    ComparisonCategory.GPU_PERFORMANCE -> "GPU Performance"
    ComparisonCategory.RAM_SIZE -> "RAM Size"
    ComparisonCategory.STORAGE_SIZE -> "Storage Size"
    ComparisonCategory.STORAGE_SPEED -> "Storage Speed"
    ComparisonCategory.DISPLAY_QUALITY -> "Display Quality"
    ComparisonCategory.BATTERY_CAPACITY -> "Battery Capacity"
    ComparisonCategory.CAMERA_QUALITY -> "Camera Quality"
    ComparisonCategory.OVERALL_PERFORMANCE -> "Overall Performance"
    ComparisonCategory.PRICE_PERFORMANCE -> "Price/Performance"
}

@Composable
private fun getRankingColor(ranking: Int, total: Int): Color {
    val percentile = (total - ranking + 1).toFloat() / total
    return when {
        percentile >= 0.8f -> Color(0xFF4CAF50) // Top 20%
        percentile >= 0.6f -> Color(0xFF8BC34A) // Top 40%
        percentile >= 0.4f -> Color(0xFFFFEB3B) // Top 60%
        percentile >= 0.2f -> Color(0xFFFF9800) // Top 80%
        else -> Color(0xFFFF5722) // Bottom 20%
    }
}

/**
 * کارت عملیات مقایسه
 */
@Composable
private fun ComparisonTestActionsCard(
    onRunNewTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = onRunNewTest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("مقایسه جدید")
            }
        }
    }
}

/**
 * بخش تاریخچه مقایسه‌ها
 */
@Composable
private fun ComparisonHistorySection(
    history: List<DeviceComparison>,
    onHistoryItemClick: (DeviceComparison) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.comparison_history_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                if (history.isEmpty()) {
                    Text(
                        text = stringResource(R.string.comparison_no_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    history.take(5).forEach { comparison -> // نمایش 5 مقایسه آخر
                        ComparisonHistoryItemCard(
                            comparison = comparison,
                            viewModel = viewModel(),
                            onClick = { onHistoryItemClick(comparison) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (history.isEmpty()) {
                        stringResource(R.string.comparison_no_history)
                    } else {
                        stringResource(R.string.comparison_expand_details)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * کارت هر آیتم تاریخچه مقایسه - قابل باز شدن
 */
@Composable
private fun ComparisonHistoryItemCard(
    comparison: DeviceComparison,
    viewModel: DiagnosticViewModel,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
    val rankingColor = getRankingColor(
        comparison.overallComparison.overallRanking,
        comparison.overallComparison.totalDevices
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header - قابل کلیک برای باز/بسته کردن
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // نمودار کوچک رتبه
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(rankingColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${comparison.overallComparison.overallRanking}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = rankingColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.comparison_test_date, dateFormat.format(java.util.Date(comparison.lastUpdateTime))),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${comparison.currentDevice.deviceName} • ${comparison.comparedDevices.size} دستگاه مقایسه شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "رتبه ${comparison.overallComparison.overallRanking}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = rankingColor
                        )
                        Text(
                            text = "از ${comparison.overallComparison.totalDevices}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // محتوای باز شده
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // نمایش نقاط قوت و ضعف
                if (comparison.overallComparison.strengths.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.comparison_strengths),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    comparison.overallComparison.strengths.forEach { strength ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strength,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (comparison.overallComparison.weaknesses.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.comparison_weaknesses),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    comparison.overallComparison.weaknesses.forEach { weakness ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = weakness,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // دکمه خروجی گزارش
                OutlinedButton(
                    onClick = {
                        viewModel.exportDeviceComparisonHistoryReport(comparison, ExportFormat.TXT)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.comparison_export_report))
                }
            }
        }
    }
}
