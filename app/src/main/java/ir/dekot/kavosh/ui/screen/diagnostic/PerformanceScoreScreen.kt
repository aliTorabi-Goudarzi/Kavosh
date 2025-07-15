package ir.dekot.kavosh.ui.screen.diagnostic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.diagnostic.CategoryScore
import ir.dekot.kavosh.data.model.diagnostic.PerformanceCategory
import ir.dekot.kavosh.data.model.diagnostic.PerformanceGrade
import ir.dekot.kavosh.data.model.diagnostic.PerformanceScore
import ir.dekot.kavosh.ui.composables.KavoshTopAppBar
import ir.dekot.kavosh.ui.composables.ProfessionalLoadingIndicator
import ir.dekot.kavosh.ui.viewmodel.DiagnosticViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportFormat

/**
 * صفحه امتیاز عملکرد دستگاه
 * نمایش نتایج benchmark و رتبه‌بندی عملکرد
 */
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScoreScreen(
    onBackClick: () -> Unit,
    viewModel: DiagnosticViewModel = hiltViewModel()
) {
    // مدیریت export events
    DiagnosticExportHandler()
    val performanceScore by viewModel.performanceScore.collectAsState()
    val isLoading by viewModel.isPerformanceScoreLoading.collectAsState()
    val performanceHistory by viewModel.performanceScoreHistory.collectAsState()

    var showStartButton by remember { mutableStateOf(performanceScore == null) }

    Scaffold(
        topBar = {
            // استفاده از نوار بالایی سفارشی برای یکپارچگی رنگی
            KavoshTopAppBar(
                title = stringResource(R.string.performance_score_title),
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
                    text = stringResource(R.string.performance_score_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // دکمه شروع تست یا نتایج
            if (showStartButton && !isLoading) {
                item {
                    PerformanceStartTestCard(
                        onStartTest = {
                            showStartButton = false
                            viewModel.calculatePerformanceScore()
                        }
                    )
                }
            }

            if (isLoading) {
                item {
                    BenchmarkLoadingCard()
                }
            } else {
                performanceScore?.let { score ->
                    item {
                        OverallScoreCard(
                            score = score,
                            onRunNewTest = {
                                viewModel.calculatePerformanceScore()
                            }
                        )
                    }

                    score.deviceRanking?.let { ranking ->
                        item {
                            RankingCard(ranking)
                        }
                    }

                    items(score.categoryScores) { categoryScore ->
                        CategoryScoreCard(categoryScore)
                    }

                    if (score.benchmarkResults.isNotEmpty()) {
                        item {
                            BenchmarkResultsCard(score.benchmarkResults)
                        }
                    }
                }
            }

            // تاریخچه تست‌ها
            if (performanceHistory.isNotEmpty()) {
                item {
                    PerformanceHistorySection(
                        history = performanceHistory,
                        onHistoryItemClick = { score ->
                            // TODO: نمایش جزئیات تست قبلی
                        }
                    )
                }
            }
        }
    }
}

/**
 * کارت شروع تست عملکرد
 */
@Composable
private fun PerformanceStartTestCard(
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
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.performance_score_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.performance_score_subtitle),
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
                Text(stringResource(R.string.performance_score_start_test))
            }
        }
    }
}

/**
 * کارت بارگذاری benchmark با انیمیشن حرفه‌ای
 */
@Composable
private fun BenchmarkLoadingCard() {
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
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.performance_score_running_benchmarks),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.performance_score_this_may_take),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * کارت امتیاز کلی
 */
@Composable
private fun OverallScoreCard(
    score: PerformanceScore,
    onRunNewTest: () -> Unit
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.overallScore.toFloat(),
        animationSpec = tween(2000),
        label = "overallScore"
    )

    // استخراج رنگ و متن خارج از Canvas
    val gradeColor = getGradeColor(score.performanceGrade)
    val gradeText = getGradeText(score.performanceGrade)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = gradeColor.copy(alpha = 0.1f)
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
                modifier = Modifier.size(140.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val strokeWidth = 16.dp.toPx()
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
                        color = gradeColor,
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
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor
                    )
                    Text(
                        text = gradeText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = gradeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.performance_score_overall_score),
                style = MaterialTheme.typography.headlineSmall,
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
 * کارت رتبه‌بندی
 */
@Composable
private fun RankingCard(ranking: ir.dekot.kavosh.data.model.diagnostic.DeviceRanking) {
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
                    text = stringResource(R.string.performance_score_global_ranking),
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
                        text = "#${ranking.globalRank}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "of ${ranking.totalDevices}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Top ${(100 - ranking.percentile).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Percentile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * کارت امتیاز هر دسته
 */
@Composable
private fun CategoryScoreCard(categoryScore: CategoryScore) {
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
                imageVector = getCategoryIcon(categoryScore.category),
                contentDescription = null,
                tint = getGradeColor(categoryScore.grade),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getCategoryName(categoryScore.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = categoryScore.details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${categoryScore.score}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = getGradeColor(categoryScore.grade)
                )

                Text(
                    text = getGradeText(categoryScore.grade),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * کارت نتایج benchmark
 */
@Composable
private fun BenchmarkResultsCard(results: List<ir.dekot.kavosh.data.model.diagnostic.BenchmarkResult>) {
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
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.performance_score_benchmark_results),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            results.forEach { result ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = result.testName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = result.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${result.score} ${result.unit}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (result != results.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// توابع کمکی
@Composable
private fun getGradeColor(grade: PerformanceGrade): Color = when (grade) {
    PerformanceGrade.S_PLUS, PerformanceGrade.S -> Color(0xFF4CAF50)
    PerformanceGrade.A_PLUS, PerformanceGrade.A -> Color(0xFF8BC34A)
    PerformanceGrade.B_PLUS, PerformanceGrade.B -> Color(0xFFFFEB3B)
    PerformanceGrade.C_PLUS, PerformanceGrade.C -> Color(0xFFFF9800)
    PerformanceGrade.D -> Color(0xFFFF5722)
    PerformanceGrade.F -> Color(0xFFF44336)
}

@Composable
private fun getGradeText(grade: PerformanceGrade): String = when (grade) {
    PerformanceGrade.S_PLUS -> stringResource(R.string.performance_grade_s_plus)
    PerformanceGrade.S -> stringResource(R.string.performance_grade_s)
    PerformanceGrade.A_PLUS -> stringResource(R.string.performance_grade_a_plus)
    PerformanceGrade.A -> stringResource(R.string.performance_grade_a)
    PerformanceGrade.B_PLUS -> stringResource(R.string.performance_grade_b_plus)
    PerformanceGrade.B -> stringResource(R.string.performance_grade_b)
    PerformanceGrade.C_PLUS -> stringResource(R.string.performance_grade_c_plus)
    PerformanceGrade.C -> stringResource(R.string.performance_grade_c)
    PerformanceGrade.D -> stringResource(R.string.performance_grade_d)
    PerformanceGrade.F -> stringResource(R.string.performance_grade_f)
}

@Composable
private fun getCategoryName(category: PerformanceCategory): String = when (category) {
    PerformanceCategory.CPU -> stringResource(R.string.performance_category_cpu)
    PerformanceCategory.GPU -> stringResource(R.string.performance_category_gpu)
    PerformanceCategory.RAM -> stringResource(R.string.performance_category_ram)
    PerformanceCategory.STORAGE -> stringResource(R.string.performance_category_storage)
    PerformanceCategory.NETWORK -> stringResource(R.string.performance_category_network)
    PerformanceCategory.BATTERY -> stringResource(R.string.performance_category_battery)
    PerformanceCategory.THERMAL -> stringResource(R.string.performance_category_thermal)
}

private fun getCategoryIcon(category: PerformanceCategory): ImageVector = when (category) {
    PerformanceCategory.CPU -> Icons.Default.Memory
    PerformanceCategory.GPU -> Icons.Default.GraphicEq
    PerformanceCategory.RAM -> Icons.Default.Storage
    PerformanceCategory.STORAGE -> Icons.Default.Folder
    PerformanceCategory.NETWORK -> Icons.Default.Wifi
    PerformanceCategory.BATTERY -> Icons.Default.Battery6Bar
    PerformanceCategory.THERMAL -> Icons.Default.Thermostat
}

/**
 * کارت عملیات تست عملکرد
 */
@Composable
private fun PerformanceTestActionsCard(
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
                    Text("خروجی گزارش")
                }
            }
        }
    }
}

/**
 * بخش تاریخچه تست‌های عملکرد
 */
@Composable
private fun PerformanceHistorySection(
    history: List<PerformanceScore>,
    onHistoryItemClick: (PerformanceScore) -> Unit
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
                        text = "تاریخچه تست‌ها",
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
                        text = "هیچ تست قبلی یافت نشد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    history.take(5).forEach { score -> // نمایش 5 تست آخر
                        PerformanceHistoryItemCard(
                            score = score,
                            viewModel = viewModel(),
                            onClick = { onHistoryItemClick(score) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (history.isEmpty()) {
                        "هیچ تست قبلی یافت نشد"
                    } else {
                        "برای مشاهده جزئیات ضربه بزنید"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * کارت هر آیتم تاریخچه عملکرد - قابل باز شدن
 */
@Composable
private fun PerformanceHistoryItemCard(
    score: PerformanceScore,
    viewModel: DiagnosticViewModel,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
    val gradeColor = getGradeColor(score.performanceGrade)
    val gradeText = getGradeText(score.performanceGrade)

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
                        .background(gradeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${score.overallScore}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "تاریخ تست: ${dateFormat.format(java.util.Date(score.lastTestTime))}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${score.categoryScores.size} دسته تست شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gradeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = gradeColor
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

                // نمایش نتایج دسته‌ها
                Text(
                    text = "نتایج تست‌ها:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                score.categoryScores.forEach { categoryScore ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getCategoryName(categoryScore.category),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${categoryScore.score}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = getGradeColor(categoryScore.grade)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = getGradeText(categoryScore.grade),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // دکمه خروجی گزارش
                OutlinedButton(
                    onClick = {
                        viewModel.exportPerformanceScoreHistoryReport(score, ExportFormat.TXT)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خروجی گزارش")
                }
            }
        }
    }
}
