package ir.dekot.kavosh.domain

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

/**
 * کلاسی برای مدیریت و اجرای تست استرس بر روی هسته‌های پردازنده.
 */
class CpuStresser {

    // یک استخر از تردها برای مدیریت پردازش‌های سنگین
    private var executor: ExecutorService? = null
    private var coreCount: Int = 0

    /**
     * تست استرس را با ایجاد یک ترد به ازای هر هسته پردازنده آغاز می‌کند.
     * @param cores تعداد هسته‌های پردازنده.
     */
    fun start(cores: Int) {
        // اگر تست در حال اجراست، ابتدا آن را متوقف کن
        if (executor != null) {
            stop()
        }
        coreCount = cores
        // ایجاد یک استخر ترد با تعداد ثابت، برابر با تعداد هسته‌ها
        executor = Executors.newFixedThreadPool(coreCount)
        for (i in 0 until coreCount) {
            executor?.submit(StressRunnable())
        }
    }

    /**
     * تمام تردهای در حال اجرا را متوقف کرده و استخر تردها را خاموش می‌کند.
     */
    fun stop() {
        executor?.shutdownNow()
        executor = null
    }

    /**
     * یک Runnable که یک حلقه بی‌نهایت از محاسبات ریاضی سنگین را اجرا می‌کند
     * تا هسته پردازنده را به طور کامل درگیر کند.
     */
    private class StressRunnable : Runnable {
        override fun run() {
            var number = 1.0
            try {
                // این حلقه تا زمانی که ترد متوقف (interrupt) شود، ادامه می‌یابد
                while (!Thread.currentThread().isInterrupted) {
                    // یک عملیات سنگین ریاضی برای درگیر کردن CPU
                    number = sqrt(number * 1.01)
                    if (number.isInfinite()) {
                        number = 1.0
                    }
                }
            } catch (_: InterruptedException) {
                // با دریافت این خطا، ترد به صورت امن متوقف می‌شود
                Thread.currentThread().interrupt()
            }
        }
    }
}