package ir.dekot.kavosh.util

import java.util.Locale

/**
 * یک تابع عمومی و بهینه برای فرمت کردن سایز (بایت) یا سرعت (بایت بر ثانیه).
 *
 * @param number عدد ورودی (سایز یا سرعت).
 * @param perSecond اگر true باشد، واحدهای سرعت (e.g., KB/s) استفاده می‌شود.
 * @return یک رشته فرمت شده.
 */
fun formatSizeOrSpeed(number: Long, perSecond: Boolean = false): String {
    if (number < 0) return if (perSecond) "0 B/s" else "0 B"
    val baseUnits = arrayOf("B", "KB", "MB", "GB", "TB")
    val units = if (perSecond) baseUnits.map { "$it/s" }.toTypedArray() else baseUnits

    var value = number.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.size - 1) {
        value /= 1024.0
        unitIndex++
    }
    // رشته نهایی را با یک رقم اعشار فرمت می‌کند
    return "%.1f %s".format(Locale.US, value, units[unitIndex])
}