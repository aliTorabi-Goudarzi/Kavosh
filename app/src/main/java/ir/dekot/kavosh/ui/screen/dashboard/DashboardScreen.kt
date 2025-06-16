package ir.dekot.kavosh.ui.screen.dashboard

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportFormat
import ir.dekot.kavosh.ui.viewmodel.ExportResult
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    deviceInfoViewModel: DeviceInfoViewModel,
    onCategoryClick: (InfoCategory, Context) -> Unit,
    onSettingsClick: () -> Unit,
    onEditDashboardClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val dashboardItems by deviceInfoViewModel.dashboardItems.collectAsState()
    // دریافت وضعیت قابلیت جابجایی از ViewModel
    val isReorderingEnabled by deviceInfoViewModel.isReorderingEnabled.collectAsState()
    val context = LocalContext.current

    var localItems by remember { mutableStateOf<List<DashboardItem>>(emptyList()) }
    // --- اصلاح کلیدی: به جای ایندکس، کلید (نام دسته) را ذخیره می‌کنیم ---
    var draggedItemKey by remember { mutableStateOf<Any?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    var showMenu by remember { mutableStateOf(false) }



    LaunchedEffect(dashboardItems) {
        if (draggedItemKey == null) {
            localItems = dashboardItems.filter { it.isVisible }
        }
    }

    // --- گوش دادن به نتیجه عملیات خروجی برای نمایش Snackbar ---
    LaunchedEffect(Unit) {
        deviceInfoViewModel.exportResult.collectLatest { result ->
            val message = when (result) {
                is ExportResult.Success -> result.message
                is ExportResult.Failure -> result.message
            }
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val gridState = rememberLazyGridState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // افزودن SnackbarHost
        topBar = {
            TopAppBar(
                title = { Text("کاوش") },
                actions = {
                    IconButton(onClick = onEditDashboardClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Dashboard Visibility")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    // --- دکمه جدید برای منوی خروجی ---
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("خروجی متنی (TXT)") },
                                onClick = {
                                    deviceInfoViewModel.onExportRequested(ExportFormat.TXT)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("خروجی PDF") },
                                onClick = {
                                    deviceInfoViewModel.onExportRequested(ExportFormat.PDF)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(localItems, key = { it.category.name }) { item ->
                // --- اصلاح کلیدی: وضعیت isDragging را بر اساس کلید بررسی می‌کنیم ---
                val isDragging = item.category.name == draggedItemKey

                val scale by animateFloatAsState(if (isDragging) 1.1f else 1f, label = "scale")
                val elevation by animateFloatAsState(if (isDragging) 12.dp.value else 1.dp.value, label = "elevation")

                Box(
                    modifier = Modifier
                        .animateItem()
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            shadowElevation = elevation
                            if (isDragging) {
                                translationX = dragOffset.x
                                translationY = dragOffset.y
                            }
                        }
                        .then(
                            if (isReorderingEnabled) {
                                Modifier.pointerInput(localItems) { // کلید را هم آپدیت می‌کنیم
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { draggedItemKey = item.category.name },
                                        onDragEnd = {
                                            deviceInfoViewModel.saveDashboardOrder(localItems.map { it.category })
                                            draggedItemKey = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggedItemKey = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            val currentDraggingItemIndex = localItems.indexOfFirst { it.category.name == draggedItemKey }
                                            if (currentDraggingItemIndex == -1) return@detectDragGesturesAfterLongPress

                                            val currentItemInfo = gridState.layoutInfo.visibleItemsInfo
                                                .getOrNull(currentDraggingItemIndex) ?: return@detectDragGesturesAfterLongPress

                                            val draggedItemCenter = currentItemInfo.offset.toOffset() + (currentItemInfo.size.toSize().center) + dragOffset

                                            val targetItemInfo = gridState.layoutInfo.visibleItemsInfo.find {
                                                it.key != draggedItemKey && draggedItemCenter in it.bounds()
                                            }

                                            if (targetItemInfo != null) {
                                                val from = currentDraggingItemIndex
                                                val to = targetItemInfo.index
                                                if (from != to) {
                                                    localItems = localItems.toMutableList().apply {
                                                        add(to, removeAt(from))
                                                    }
                                                    dragOffset = Offset.Zero
                                                }
                                            }
                                        }
                                    )
                                }
                            } else {
                                Modifier // اگر غیرفعال بود، هیچ قابلیتی اضافه نکن
                            }
                        )
                ) {
                    DashboardTile(
                        item = item,
                        onClick = { onCategoryClick(item.category, context) }
                    )
                }
            }
        }
    }
}

// توابع کمکی
private fun LazyGridItemInfo.bounds(): Rect {
    return Rect(offset = this.offset.toOffset(), size = this.size.toSize())
}

private fun IntOffset.toOffset() = Offset(x.toFloat(), y.toFloat())

private fun IntSize.toSize() = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())