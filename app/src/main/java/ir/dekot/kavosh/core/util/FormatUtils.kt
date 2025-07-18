package ir.dekot.kavosh.core.util

import android.content.Context
import ir.dekot.kavosh.R
import java.util.Locale

fun formatSizeOrSpeed(context: Context, number: Long, perSecond: Boolean = false): String {
    if (number < 0) return if (perSecond) "0 B/s" else "0 B"

    val baseUnits = arrayOf("B", "KB", "MB", "GB", "TB")
    val units = if (perSecond) baseUnits.map { "$it/s" }.toTypedArray() else baseUnits

    var value = number.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.size - 1) {
        value /= 1024.0
        unitIndex++
    }

    val formatString = context.getString(R.string.unit_format_size_or_speed)
    return String.format(Locale.US, formatString, value, units[unitIndex])
}