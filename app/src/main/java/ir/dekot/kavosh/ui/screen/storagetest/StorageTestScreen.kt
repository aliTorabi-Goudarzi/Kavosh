package ir.dekot.kavosh.ui.screen.storagetest

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.composables.KavoshTopAppBar
import ir.dekot.kavosh.ui.composables.ProfessionalLoadingIndicator
import ir.dekot.kavosh.ui.viewmodel.StorageTestViewModel

/**
 * صفحه تست سرعت حافظه
 * شامل تست سرعت خواندن و نوشتن حافظه داخلی
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageTestScreen(
    onBackClick: () -> Unit,
    viewModel: StorageTestViewModel = hiltViewModel()
) {
    val testState by viewModel.testState.collectAsState()
    val readSpeed by viewModel.readSpeed.collectAsState()
    val writeSpeed by viewModel.writeSpeed.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val scope = rememberCoroutineScope()

    // --- State های جدید ---
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()
    val currentWriteSpeed by viewModel.currentWriteSpeed.collectAsState()
    val currentReadSpeed by viewModel.currentReadSpeed.collectAsState()
    val writeSpeedHistory by viewModel.writeSpeedHistory.collectAsState()
    val readSpeedHistory by viewModel.readSpeedHistory.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val testHistory by viewModel.testHistory.collectAsState()

    Scaffold(
        topBar = {
            // استفاده از نوار بالایی سفارشی برای یکپارچگی رنگی
            KavoshTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.storage_test_title),
                        fontWeight = FontWeight.Medium
                    )
                },
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
            // کارت توضیحات
            item {
                InfoCard()
            }

            // کارت نتایج
            item {
                ResultsCard(
                    readSpeed = readSpeed,
                    writeSpeed = writeSpeed,
                    testState = testState
                )
            }

            // نمودار زنده سرعت (فقط در حین تست یا پس از تکمیل)
            if (testState == StorageTestState.TESTING || testState == StorageTestState.COMPLETED) {
                item {
                    StorageSpeedChart(
                        writeSpeedHistory = writeSpeedHistory,
                        readSpeedHistory = readSpeedHistory,
                        currentWriteSpeed = currentWriteSpeed,
                        currentReadSpeed = currentReadSpeed
                    )
                }
            }

            // نوار پیشرفت و پیام وضعیت
            if (testState == StorageTestState.TESTING) {
                item {
                    ProgressCard(
                        progress = progress,
                        statusMessage = statusMessage
                    )
                }
            }

            // تاریخچه تست‌ها
            item {
                StorageTestHistoryCard(
                    history = testHistory
                )
            }

            // دکمه شروع تست
            item {
                Button(
                    onClick = {
                        viewModel.requestStartTest()
                    },
                    enabled = testState != StorageTestState.TESTING,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    when (testState) {
                        StorageTestState.IDLE -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.start_test_button),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        StorageTestState.TESTING -> {
                            ProfessionalLoadingIndicator(
                                size = 20.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.testing_in_progress),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        StorageTestState.COMPLETED -> {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.retest),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        StorageTestState.ERROR -> {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.retry),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // دیالوگ درخواست مجوز
    if (showPermissionDialog) {
        StoragePermissionDialog(
            onGrantPermission = {
                viewModel.grantPermissionAndStartTest()
            },
            onDenyPermission = {
                viewModel.denyPermission()
            }
        )
    }
    }


/**
 * کارت اطلاعات درباره تست
 */
@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = stringResource(R.string.storage_test_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.storage_test_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * کارت نمایش نتایج
 */
@Composable
private fun ResultsCard(
    readSpeed: String,
    writeSpeed: String,
    testState: StorageTestState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.test_results),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                SpeedItem(
                    title = stringResource(R.string.write_speed),
                    speed = writeSpeed,
                    icon = Icons.Default.Upload,
                    color = MaterialTheme.colorScheme.primary
                )

                SpeedItem(
                    title = stringResource(R.string.read_speed),
                    speed = readSpeed,
                    icon = Icons.Default.Download,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * آیتم نمایش سرعت
 */
@Composable
private fun SpeedItem(
    title: String,
    speed: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = speed,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * کارت نوار پیشرفت با انیمیشن موجی
 */
@Composable
private fun ProgressCard(
    progress: Float,
    statusMessage: String = ""
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.test_progress),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // نوار پیشرفت دقیق
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            // نمایش پیام وضعیت
            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * وضعیت‌های مختلف تست
 */
enum class StorageTestState {
    IDLE, TESTING, COMPLETED, ERROR
}
