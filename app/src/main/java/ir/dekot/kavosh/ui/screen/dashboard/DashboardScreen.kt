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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ir.dekot.kavosh.ui.viewmodel.DashboardViewModel
import ir.dekot.kavosh.ui.viewmodel.ExportFormat
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                                    exportViewModel.onExportRequested(ExportFormat.TXT)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("خروجی PDF") },
                                onClick = {
                                    exportViewModel.onExportRequested(ExportFormat.PDF)
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
}