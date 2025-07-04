package ir.dekot.kavosh.ui.screen.dashboard

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ir.dekot.kavosh.ui.viewmodel.DashboardViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportResult
import ir.dekot.kavosh.ui.viewmodel.ExportViewModel
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    exportViewModel: ExportViewModel,
    onCategoryClick: (InfoCategory, Context) -> Unit,
    onSettingsClick: () -> Unit,
    onEditDashboardClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val dashboardItems by dashboardViewModel.dashboardItems.collectAsState()
    val isReorderingEnabled by settingsViewModel.isReorderingEnabled.collectAsState()

    val gridState = rememberLazyGridState()
    val dragState = rememberDashboardDragState(
        gridState = gridState,
        onOrderChanged = { newOrder -> dashboardViewModel.saveDashboardOrder(newOrder) }
    )

    LaunchedEffect(dashboardItems) {
        dragState.updateItems(dashboardItems.filter { it.isVisible })
    }

    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        exportViewModel.exportResult.collectLatest { result ->
            val message = when (result) {
                is ExportResult.Success -> result.message
                is ExportResult.Failure -> result.message
            }
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dragState.localItems, key = { it.category.name }) { item ->
                val isDragging = dragState.draggedItemKey == item.category.name
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
                                translationX = dragState.dragOffset.x
                                translationY = dragState.dragOffset.y
                            }
                        }
                        .then(
                            if (isReorderingEnabled) {
                                Modifier.pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { dragState.onDragStart(item) },
                                        onDragEnd = { dragState.onDragEnd() },
                                        onDragCancel = { dragState.onDragCancel() },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            dragState.onDrag(amount)
                                        }
                                    )
                                }
                            } else {
                                Modifier
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

        // Snackbar برای نمایش پیام‌ها
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        )
    }
}

/**
 * محتوای داشبورد بدون TopAppBar برای استفاده در MainScreen
 */
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardContent(
    settingsViewModel: SettingsViewModel,
    dashboardViewModel: DashboardViewModel,
    onCategoryClick: (InfoCategory, Context) -> Unit
) {
    val context = LocalContext.current
    val dashboardItems by dashboardViewModel.dashboardItems.collectAsState()
    val isReorderingEnabled by settingsViewModel.isReorderingEnabled.collectAsState()

    val gridState = rememberLazyGridState()
    val dragState = rememberDashboardDragState(
        gridState = gridState,
        onOrderChanged = { newOrder -> dashboardViewModel.saveDashboardOrder(newOrder) }
    )

    LaunchedEffect(dashboardItems) {
        dragState.updateItems(dashboardItems.filter { it.isVisible })
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(dragState.localItems, key = { it.category.name }) { item ->
            val isDragging = dragState.draggedItemKey == item.category.name
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
                            translationX = dragState.dragOffset.x
                            translationY = dragState.dragOffset.y
                        }
                    }
                    .then(
                        if (isReorderingEnabled) {
                            Modifier.pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { dragState.onDragStart(item) },
                                    onDragEnd = { dragState.onDragEnd() },
                                    onDragCancel = { dragState.onDragCancel() },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragState.onDrag(amount)
                                    }
                                )
                            }
                        } else {
                            Modifier
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