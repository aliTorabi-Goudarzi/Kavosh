package ir.dekot.kavosh.ui.screen.stresstest

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.composables.InfoRow
import ir.dekot.kavosh.ui.viewmodel.CpuStressTestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuStressTestScreen(
    viewModel: CpuStressTestViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val isTesting by viewModel.isTesting.collectAsState()
    val cpuInfo by viewModel.cpuInfo.collectAsState()
    val liveFrequencies by viewModel.liveFrequencies.collectAsState()

    // **اصلاح کلیدی: خواندن رشته در محیط Composable**
    val sleepingText = stringResource(R.string.label_sleeping)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cpu_stress_test_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.onTestToggle() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTesting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(
                    imageVector = if (isTesting) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isTesting) stringResource(R.string.stop_test) else stringResource(R.string.start_test)
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = if (isTesting) stringResource(R.string.stop_test) else stringResource(R.string.start_test))
            }

            Spacer(modifier = Modifier.height(24.dp))

            InfoRow(label = stringResource(R.string.cpu_model), value = cpuInfo.model)
            InfoRow(label = stringResource(R.string.cpu_core_count), value = cpuInfo.coreCount.toString())

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    // **استفاده از متغیر به جای فراخوانی Composable**
                    items = if (isTesting) liveFrequencies else List(cpuInfo.coreCount) { sleepingText },
                    key = { index, _ -> index }
                ) { index, freq ->
                    CoreFrequencyItem(
                        coreIndex = index,
                        frequency = freq,
                        maxFrequencyKhz = cpuInfo.maxFrequenciesKhz.getOrElse(index) { 0L }
                    )
                }
            }
        }
    }
}

// تابع CoreFrequencyItem بدون تغییر باقی می‌ماند
@Composable
private fun CoreFrequencyItem(
    coreIndex: Int,
    frequency: String,
    maxFrequencyKhz: Long
) {
    val currentFreqMhz = frequency.substringBefore(" ").toLongOrNull() ?: 0L
    val maxFreqMhz = maxFrequencyKhz / 1000
    val progress = if (maxFreqMhz > 0) (currentFreqMhz.toFloat() / maxFreqMhz) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "CoreProgress$coreIndex")
    val progressColor = when {
        animatedProgress > 0.85f -> Color.Red
        animatedProgress > 0.6f -> Color(0xFFFFA500) // Orange
        else -> MaterialTheme.colorScheme.primary
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.cpu_core_prefix, coreIndex),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = frequency,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = progressColor
        )
    }
}