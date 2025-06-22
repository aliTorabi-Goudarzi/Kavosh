package ir.dekot.kavosh.ui.screen.dashboard

import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

/**
 * یک کلاس نگهدارنده وضعیت (State Holder) برای مدیریت کامل منطق و وضعیت
 * مربوط به عملیات جابجایی (Drag and Drop) در داشبورد.
 */
class DashboardDragState(
    private val gridState: LazyGridState,
    private val onOrderChanged: (List<InfoCategory>) -> Unit
) {
    var localItems by mutableStateOf<List<DashboardItem>>(emptyList())
        private set

    // این خصوصیت‌ها حالا از بیرون کلاس قابل خواندن هستند
    var draggedItemKey by mutableStateOf<Any?>(null)
        private set
    var dragOffset by mutableStateOf(Offset.Zero)
        private set

    /**
     * لیست آیتم‌ها را از منبع اصلی (ViewModel) به‌روزرسانی می‌کند.
     */
    fun updateItems(newItems: List<DashboardItem>) {
        if (draggedItemKey == null) {
            localItems = newItems
        }
    }

    fun onDragStart(item: DashboardItem) {
        draggedItemKey = item.category.name
    }

    fun onDragEnd() {
        onOrderChanged(localItems.map { it.category })
        draggedItemKey = null
        dragOffset = Offset.Zero
    }

    fun onDragCancel() {
        draggedItemKey = null
        dragOffset = Offset.Zero
    }

    fun onDrag(dragAmount: Offset) {
        dragOffset += dragAmount

        val currentDraggingItemIndex = localItems.indexOfFirst { it.category.name == draggedItemKey }
        if (currentDraggingItemIndex == -1) return

        val layoutInfo = gridState.layoutInfo
        val currentItemInfo = layoutInfo.visibleItemsInfo.getOrNull(currentDraggingItemIndex) ?: return

        val draggedItemCenter = currentItemInfo.offset.toOffset() + (currentItemInfo.size.toSize().center) + dragOffset

        val targetItemInfo = layoutInfo.visibleItemsInfo.find {
            it.key != draggedItemKey && draggedItemCenter in it.bounds()
        }

        if (targetItemInfo != null) {
            val from = currentDraggingItemIndex
            val to = targetItemInfo.index
            if (from != to) {
                localItems = localItems.toMutableList().apply { add(to, removeAt(from)) }
                dragOffset = Offset.Zero
            }
        }
    }
}

/**
 * یک تابع Composable برای ساختن و به خاطر سپردن نمونه‌ای از DashboardDragState.
 */
@Composable
fun rememberDashboardDragState(
    gridState: LazyGridState,
    onOrderChanged: (List<InfoCategory>) -> Unit
): DashboardDragState {
    return remember { DashboardDragState(gridState, onOrderChanged) }
}

private fun LazyGridItemInfo.bounds(): Rect {
    return Rect(offset = this.offset.toOffset(), size = this.size.toSize())
}

private fun IntOffset.toOffset() = Offset(x.toFloat(), y.toFloat())

private fun IntSize.toSize() = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())