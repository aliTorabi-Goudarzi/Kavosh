package ir.dekot.kavosh.feature_dashboard.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory

data class DashboardItem(
    val category: InfoCategory,
    // *** تغییر کلیدی: ذخیره آیدی منبع به جای متن ثابت ***
    @param:StringRes val titleResId: Int,
    val icon: ImageVector,
    val isVisible: Boolean = true
)