
package ir.dekot.kavosh.feature_deviceInfo.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.view.infoCards.AppInfoCard
import ir.dekot.kavosh.feature_deviceInfo.viewModel.AppsLoadingState
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AppsPage(viewModel: DeviceInfoViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadAppsListIfNeeded()
    }

    val userApps by viewModel.userApps.collectAsState()
    val systemApps by viewModel.systemApps.collectAsState()
    val loadingState by viewModel.appsLoadingState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CustomSlidingTab(
                items = listOf(
                    stringResource(R.string.apps_user_apps, userApps.size),
                    stringResource(R.string.apps_system_apps, systemApps.size)
                ),
                selectedIndex = selectedTabIndex,
                onSelectIndex = { selectedTabIndex = it }
            )
        }

        // ... (بقیه کد بدون تغییر)
        when (loadingState) {
            AppsLoadingState.IDLE, AppsLoadingState.LOADING -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            AppsLoadingState.LOADED -> {
                val listToShow = if (selectedTabIndex == 0) userApps else systemApps
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = listToShow, key = { it.packageName }) { app ->
                        AppInfoCard(info = app)
                    }
                }
            }
        }
    }
}

/**
 * کامنت: این نسخه نهایی، حاشیه خاکستری و نوار رنگی دور نشانگر را پیاده‌سازی می‌کند.
 */
@Composable
private fun CustomSlidingTab(
    items: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val density = LocalDensity.current
    val tabWidths = remember { mutableStateMapOf<Int, Dp>() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) // پس‌زمینه خاکستری اصلی
            .height(IntrinsicSize.Min)
            .padding(top = 6.dp, bottom = 6.dp, start = 6.dp, end = 6.dp)
    ) {
        // نشانگر لغزشی
        val indicatorOffset by animateDpAsState(
            targetValue = with(density) {
                val previousTabsWidth = (0 until selectedIndex).sumOf { tabWidths[it]?.toPx()?.toDouble() ?: 0.0 }
                (previousTabsWidth).toFloat().toDp()
            },
            label = "indicatorOffset"
        )
        val indicatorWidth by animateDpAsState(
            targetValue = tabWidths.getOrElse(selectedIndex) { 0.dp },
            label = "indicatorWidth"
        )

        // **تغییر کلیدی: افزودن border به نشانگر**
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(indicatorWidth)
                .fillMaxHeight()
                .border( // نوار رنگی دور نشانگر
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .background( // پس‌زمینه ملایم داخل نشانگر
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        Row{
            items.forEachIndexed { index, text ->
                val contentColor by animateColorAsState(
                    targetValue = if (selectedIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "contentColorAnim"
                )
                Box(
                    modifier = Modifier
                        .onSizeChanged {
                            with(density) { tabWidths[index] = it.width.toDp() }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectIndex(index) }
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text, color = contentColor, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}