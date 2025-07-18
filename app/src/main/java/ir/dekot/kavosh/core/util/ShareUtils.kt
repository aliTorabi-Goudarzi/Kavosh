package ir.dekot.kavosh.core.util

import android.content.Context
import android.content.Intent

/**
 * یک تابع کمکی برای اجرای Intent اشتراک‌گذاری متن.
 * @param context برای اجرای Intent به Context نیاز داریم.
 * @param text متنی که قرار است به اشتراک گذاشته شود.
 */
fun shareText(context: Context, text: String) {
    // یک Intent از نوع ACTION_SEND می‌سازیم
    val sendIntent: Intent = Intent().apply {
        setAction(Intent.ACTION_SEND)
// محتوای متنی را در Intent قرار می‌دهیم
        putExtra(Intent.EXTRA_TEXT, text)
// نوع محتوا را مشخص می‌کنیم
        setType("text/plain")
    }
    // یک Intent Chooser می‌سازیم تا کاربر بتواند اپلیکیشن مقصد را انتخاب کند
    val shareIntent = Intent.createChooser(sendIntent, "اشتراک‌گذاری اطلاعات از طریق...")
    // Intent را اجرا می‌کنیم
    context.startActivity(shareIntent)
}

