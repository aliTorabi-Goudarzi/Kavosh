package ir.dekot.kavosh.feature_deviceInfo.model

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R

/**
 * دسته‌بندی‌های اطلاعات
 */
enum class InfoCategory {
    SOC,
    DEVICE,
    SYSTEM,
    BATTERY,
    SENSORS,
    THERMAL,
    NETWORK,
    CAMERA,

    SIM,
    APPS
}

/**
 * یک تابع الحاقی Composable برای گرفتن عنوان محلی‌شده در UI.
 */
@Composable
fun InfoCategory.localizedTitle(): String {
    val resId = when (this) {
        InfoCategory.SOC -> R.string.category_soc
        InfoCategory.DEVICE -> R.string.category_device
        InfoCategory.SYSTEM -> R.string.category_system
        InfoCategory.BATTERY -> R.string.category_battery
        InfoCategory.SENSORS -> R.string.category_sensors
        InfoCategory.THERMAL -> R.string.category_thermal
        InfoCategory.NETWORK -> R.string.category_network
        InfoCategory.CAMERA -> R.string.category_camera
        InfoCategory.SIM -> R.string.category_sim
        InfoCategory.APPS -> R.string.category_apps // <-- اضافه شد
    }
    return stringResource(id = resId)
}

/**
 * *** تابع جدید: ***
 * یک تابع الحاقی غیر-Composable برای گرفتن عنوان در خارج از UI (مثلا در ViewModel یا Repository).
 */
fun InfoCategory.getTitle(context: Context): String {
    val resId = when (this) {
        InfoCategory.SOC -> R.string.category_soc
        InfoCategory.DEVICE -> R.string.category_device
        InfoCategory.SYSTEM -> R.string.category_system
        InfoCategory.BATTERY -> R.string.category_battery
        InfoCategory.SENSORS -> R.string.category_sensors
        InfoCategory.THERMAL -> R.string.category_thermal
        InfoCategory.NETWORK -> R.string.category_network
        InfoCategory.CAMERA -> R.string.category_camera
        InfoCategory.SIM -> R.string.category_sim
        InfoCategory.APPS -> R.string.category_apps // <-- اضافه شد
    }
    return context.getString(resId)
}