package ir.dekot.kavosh.ui.screen.diagnostic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.diagnostic.HealthCheck
import ir.dekot.kavosh.data.model.diagnostic.HealthCheckResult
import ir.dekot.kavosh.data.model.diagnostic.HealthCheckSummary
import ir.dekot.kavosh.data.model.diagnostic.HealthStatus
import ir.dekot.kavosh.ui.viewmodel.DiagnosticViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportFormat

/**
 * صفحه بررسی سلامت دستگاه
 * نمایش تجزیه و تحلیل جامع سلامت سخت‌افزار و نرم‌افزار
 */
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCheckScreen(
    onBackClick: () -> Unit,
    viewModel: DiagnosticViewModel = hiltViewModel()
) {
    // مدیریت export events
    DiagnosticExportHandler()
    val healthCheckResult by viewModel.healthCheckResult.collectAsState()
    val isLoading by viewModel.isHealthCheckLoading.collectAsState()
    val healthCheckHistory by viewModel.healthCheckHistory.collectAsState()

    var showStartButton by remember { mutableStateOf(healthCheckResult == null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.health_check_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    text = stringResource(R.string.health_check_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // دکمه شروع تست یا نتایج
            if (showStartButton && !isLoading) {
                item {
                    StartTestCard(
                        onStartTest = {
                            showStartButton = false
                            viewModel.performHealthCheck()
                        }
                    )
                }
            }

            if (isLoading) {
                item {
                    LoadingCard()
                }
            } else {
                healthCheckResult?.let { result ->
                    item {
                        OverallHealthCard(
                            result = result,
                            onRunNewTest = {
                                viewModel.performHealthCheck()
                            }
                        )
                    }

                    items(result.checks) { check ->
                        HealthCheckCard(check)
                    }

                    if (result.recommendations.isNotEmpty()) {
                        item {
                            RecommendationsCard(result.recommendations)
                        }
                    }
                }
            }

            // تاریخچه تست‌ها
            if (healthCheckHistory.isNotEmpty()) {
                item {
                    TestHistorySection(
                        history = healthCheckHistory,
                        onHistoryItemClick = { summary ->
                            // TODO: نمایش جزئیات تست قبلی
                        }
                    )
                }
            }
        }
    }
}

/**
 * کارت شروع تست
 */
@Composable
private fun StartTestCard(
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
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.health_check_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.health_check_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                Text(stringResource(R.string.health_check_start_test))
            }
        }
    }
}

/**
 * کارت بارگذاری
 */
@Composable
private fun LoadingCard() {
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
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.testing),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.health_check_analyzing),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * کارت امتیاز کلی سلامت
 */
@Composable
private fun OverallHealthCard(
    result: HealthCheckResult,
    onRunNewTest: () -> Unit
) {
    val animatedScore by animateFloatAsState(
        targetValue = result.overallScore.toFloat(),
        animationSpec = tween(1500),
        label = "score"
    )

    // استخراج رنگ و متن خارج از Canvas
    val statusColor = getHealthStatusColor(result.overallStatus)
    val statusText = getHealthStatusText(result.overallStatus)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = size.center

                    // Background circle
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    val sweepAngle = (animatedScore / 100f) * 360f
                    drawArc(
                        color = statusColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${animatedScore.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.health_check_overall_health),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // دکمه تست جدید در کارت اصلی
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
                Text("تست جدید")
            }
        }
    }
}

/**
 * کارت هر بررسی جداگانه
 */
@Composable
private fun HealthCheckCard(check: HealthCheck) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getHealthCategoryIcon(check.category),
                contentDescription = null,
                tint = getHealthStatusColor(check.status),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = check.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = check.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                check.details?.let { details ->
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${check.score}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = getHealthStatusColor(check.status)
                )

                Text(
                    text = getHealthStatusText(check.status),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * کارت توصیه‌ها
 */
@Composable
private fun RecommendationsCard(recommendations: List<String>) {
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
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// توابع کمکی
@Composable
private fun getHealthStatusColor(status: HealthStatus): Color = when (status) {
    HealthStatus.EXCELLENT -> Color(0xFF4CAF50)
    HealthStatus.GOOD -> Color(0xFF8BC34A)
    HealthStatus.FAIR -> Color(0xFFFF9800)
    HealthStatus.POOR -> Color(0xFFFF5722)
    HealthStatus.CRITICAL -> Color(0xFFF44336)
}

@Composable
private fun getHealthStatusText(status: HealthStatus): String = when (status) {
    HealthStatus.EXCELLENT -> stringResource(R.string.health_status_excellent)
    HealthStatus.GOOD -> stringResource(R.string.health_status_good)
    HealthStatus.FAIR -> stringResource(R.string.health_status_fair)
    HealthStatus.POOR -> stringResource(R.string.health_status_poor)
    HealthStatus.CRITICAL -> stringResource(R.string.health_status_critical)
}

private fun getHealthCategoryIcon(category: ir.dekot.kavosh.data.model.diagnostic.HealthCategory): ImageVector = when (category) {
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.PERFORMANCE -> Icons.Default.Speed
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.STORAGE -> Icons.Default.Storage
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.BATTERY -> Icons.Default.Battery6Bar
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.TEMPERATURE -> Icons.Default.Thermostat
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.MEMORY -> Icons.Default.Memory
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.NETWORK -> Icons.Default.Wifi
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.SECURITY -> Icons.Default.Security
    ir.dekot.kavosh.data.model.diagnostic.HealthCategory.SYSTEM -> Icons.Default.Settings
}

/**
 * کارت عملیات تست
 */
@Composable
private fun TestActionsCard(
    onRunNewTest: () -> Unit,
    onExportReport: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRunNewTest,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تست جدید")
                }

                OutlinedButton(
                    onClick = onExportReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.health_check_export_report))
                }
            }
        }
    }
}

/**
 * بخش تاریخچه تست‌ها
 */
@Composable
private fun TestHistorySection(
    history: List<HealthCheckSummary>,
    onHistoryItemClick: (HealthCheckSummary) -> Unit
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
                        text = stringResource(R.string.health_check_history_title),
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
                        text = stringResource(R.string.health_check_no_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    history.take(5).forEach { summary -> // نمایش 5 تست آخر
                        HistoryItemCard(
                            summary = summary,
                            viewModel = viewModel(),
                            onClick = { onHistoryItemClick(summary) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (history.isEmpty()) {
                        stringResource(R.string.health_check_no_history)
                    } else {
                        stringResource(R.string.health_check_expand_details)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * کارت هر آیتم تاریخچه - قابل باز شدن
 */
@Composable
private fun HistoryItemCard(
    summary: HealthCheckSummary,
    viewModel: DiagnosticViewModel,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val statusColor = getHealthStatusColor(summary.overallStatus)
    val statusText = getHealthStatusText(summary.overallStatus)

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
                // نمودار کوچک امتیاز
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${summary.overallScore}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.health_check_test_date, dateFormat.format(Date(summary.timestamp))),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${summary.deviceName} • Android ${summary.androidVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )

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

                // نمایش جزئیات تست (شبیه‌سازی)
                Text(
                    text = "جزئیات تست:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // شبیه‌سازی نتایج تست
                val mockCategories = listOf(
                    "عملکرد" to Random.nextInt(70, 100),
                    "حافظه" to Random.nextInt(60, 95),
                    "باتری" to Random.nextInt(75, 100),
                    "دما" to Random.nextInt(80, 100)
                )

                mockCategories.forEach { (category, score) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = getHealthStatusColor(
                                when {
                                    score >= 90 -> HealthStatus.EXCELLENT
                                    score >= 70 -> HealthStatus.GOOD
                                    score >= 50 -> HealthStatus.FAIR
                                    else -> HealthStatus.POOR
                                }
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // دکمه خروجی گزارش
                OutlinedButton(
                    onClick = {
                        viewModel.exportHealthCheckHistoryReport(summary, ExportFormat.TXT)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.health_check_export_report))
                }
            }
        }
    }
}
