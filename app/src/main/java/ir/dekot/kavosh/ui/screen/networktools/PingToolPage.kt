package ir.dekot.kavosh.ui.screen.networktools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.viewmodel.NetworkToolsViewModel
import kotlinx.coroutines.launch

@Composable
fun PingToolPage(viewModel: NetworkToolsViewModel) {
    var host by remember { mutableStateOf("google.com") }
    val pingResult by viewModel.pingResult.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // اسکرول خودکار به پایین با هر خط جدید
    LaunchedEffect(pingResult.outputLines.size) {
        if (pingResult.outputLines.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(pingResult.outputLines.size - 1)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text(stringResource(R.string.host_or_ip_address)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !pingResult.isPinging
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (pingResult.isPinging) viewModel.stopPing() else viewModel.startPing(host)
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (pingResult.isPinging) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.start_stop_ping)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            items(pingResult.outputLines) { line ->
                Text(
                    text = line,
                    fontFamily = FontFamily.Monospace,
                    color = if (line.startsWith(stringResource(R.string.error_prefix))) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            }
            if (pingResult.isPinging) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}