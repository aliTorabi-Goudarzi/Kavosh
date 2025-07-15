package ir.dekot.kavosh.ui.screen.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.ui.viewmodel.DashboardViewModel

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDashboardScreen(
    viewModel: DashboardViewModel, // <-- استفاده از ViewModel جدید
    onBackClick: () -> Unit
) {
    val dashboardItems by viewModel.dashboardItems.collectAsState()

    // وقتی از این صفحه خارج میشیم، ترتیب جدید باید ذخیره بشه
    // که این کار به صورت خودکار در onDashboardItemVisibilityChanged انجام میشه.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_dashboard_items)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dashboardItems, key = { it.category.name }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(id = item.titleResId),
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text(text = stringResource(id = item.titleResId), style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(
                            checked = item.isVisible,
                            onCheckedChange = { isChecked ->
                                // فراخوانی متد از ViewModel صحیح
                                viewModel.onDashboardItemVisibilityChanged(item.category, isChecked)
                            }
                        )
                    }
                }
            }
        }
    }
}