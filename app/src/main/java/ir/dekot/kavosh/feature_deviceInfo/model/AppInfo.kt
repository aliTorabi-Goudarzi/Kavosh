// Path: app/src/main/java/ir/dekot/kavosh/data/model/components/AppInfo.kt
package ir.dekot.kavosh.feature_deviceInfo.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AppInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val installTime: Long,
    val isSystemApp: Boolean,
    val permissions: List<String>,
)
//یک data class به نام AppInfo برای مدل‌سازی اطلاعات یک برنامه نصب‌شده ایجاد شد.
//
//از انوتیشن @Transient برای icon استفاده شد تا در فرآیند سریال‌سازی (Serialization) که برای کش کردن اطلاعات استفاده می‌شود، نادیده گرفته شود، زیرا Drawable قابل سریال‌سازی نیست.