package ir.dekot.kavosh

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * کلاس Application سفارشی برای راه‌اندازی Hilt.
 * انوتیشن @HiltAndroidApp باعث می‌شود Hilt کدهای مورد نیاز
 * برای تزریق وابستگی در سطح اپلیکیشن را تولید کند.
 */
@HiltAndroidApp
class KavoshApp : Application() {
    // در حال حاضر نیازی به کد اضافی در این کلاس نیست.
}