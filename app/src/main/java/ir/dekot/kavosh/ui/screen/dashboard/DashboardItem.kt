package ir.dekot.kavosh.ui.screen.dashboard

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

data class DashboardItem(
    val category: InfoCategory,
    // *** تغییر کلیدی: ذخیره آیدی منبع به جای متن ثابت ***
    @StringRes val titleResId: Int,
    val icon: ImageVector,
    val isVisible: Boolean = true
)