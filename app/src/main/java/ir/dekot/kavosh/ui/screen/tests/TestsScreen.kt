package ir.dekot.kavosh.ui.screen.tests

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

/**
 * صفحه تست‌های سخت‌افزار
 * شامل لیستی از ابزارهای تست مختلف
 */
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TestsScreen(
    onCpuStressTestClick: () -> Unit,
    onStorageTestClick: () -> Unit,
    onDisplayTestClick: () -> Unit,
    onNetworkToolsClick: () -> Unit,
    onHealthCheckClick: () -> Unit,
    onPerformanceScoreClick: () -> Unit,
    onDeviceComparisonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // هدر صفحه
        Text(
            text = stringResource(R.string.tests_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = stringResource(R.string.tests_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // لیست تست‌ها
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getTestItems()) { testItem ->
                TestCard(
                    title = stringResource(testItem.titleResId),
                    description = stringResource(testItem.descriptionResId),
                    icon = testItem.icon,
                    onClick = when (testItem.type) {
                        TestType.CPU_STRESS -> onCpuStressTestClick
                        TestType.STORAGE_SPEED -> onStorageTestClick
                        TestType.DISPLAY -> onDisplayTestClick
                        TestType.NETWORK_TOOLS -> onNetworkToolsClick
                        TestType.HEALTH_CHECK -> onHealthCheckClick
                        TestType.PERFORMANCE_SCORE -> onPerformanceScoreClick
                        TestType.DEVICE_COMPARISON -> onDeviceComparisonClick
                    }
                )
            }
        }
    }
}

/**
 * کارت هر تست - بدون افکت‌های بصری کلیک
 */
@Composable
private fun TestCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // حذف کامل افکت‌های بصری کلیک
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * انواع تست‌های موجود
 */
private enum class TestType {
    CPU_STRESS, STORAGE_SPEED, DISPLAY, NETWORK_TOOLS, HEALTH_CHECK, PERFORMANCE_SCORE, DEVICE_COMPARISON
}

/**
 * مدل داده برای هر تست
 */
private data class TestItem(
    val type: TestType,
    val titleResId: Int,
    val descriptionResId: Int,
    val icon: ImageVector
)

/**
 * لیست تست‌های موجود
 */
private fun getTestItems(): List<TestItem> = listOf(
    TestItem(
        type = TestType.CPU_STRESS,
        titleResId = R.string.test_cpu_stress,
        descriptionResId = R.string.test_cpu_stress_desc,
        icon = Icons.Default.Memory
    ),
    TestItem(
        type = TestType.STORAGE_SPEED,
        titleResId = R.string.test_storage_speed,
        descriptionResId = R.string.test_storage_speed_desc,
        icon = Icons.Default.Storage
    ),
    TestItem(
        type = TestType.DISPLAY,
        titleResId = R.string.test_display,
        descriptionResId = R.string.test_display_desc,
        icon = Icons.Default.Monitor
    ),
    TestItem(
        type = TestType.NETWORK_TOOLS,
        titleResId = R.string.test_network_tools,
        descriptionResId = R.string.test_network_tools_desc,
        icon = Icons.Default.Wifi
    ),
    TestItem(
        type = TestType.HEALTH_CHECK,
        titleResId = R.string.test_health_check,
        descriptionResId = R.string.test_health_check_desc,
        icon = Icons.Default.HealthAndSafety
    ),
    TestItem(
        type = TestType.PERFORMANCE_SCORE,
        titleResId = R.string.test_performance_score,
        descriptionResId = R.string.test_performance_score_desc,
        icon = Icons.Default.Speed
    ),
    TestItem(
        type = TestType.DEVICE_COMPARISON,
        titleResId = R.string.test_device_comparison,
        descriptionResId = R.string.test_device_comparison_desc,
        icon = Icons.Default.Compare
    )
)
