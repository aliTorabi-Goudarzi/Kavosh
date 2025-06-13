package ir.dekot.kavosh.ui.screen.dashboard

import androidx.compose.ui.graphics.vector.ImageVector
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

data class DashboardItem(
    val category: InfoCategory,
    val title: String,
    val icon: ImageVector
)