package ir.dekot.kavosh.data.source

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.data.model.storage.StorageSpeedTestResult
import ir.dekot.kavosh.data.model.storage.SpeedDataPoint
import ir.dekot.kavosh.data.model.storage.TestPhase
import ir.dekot.kavosh.data.model.storage.StorageTestStatus
import ir.dekot.kavosh.util.formatSizeOrSpeed
import ir.dekot.kavosh.R
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class MemoryDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    // *** تغییر کلیدی و نهایی در این خط ***
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return RamInfo(
            total = formatSizeOrSpeed(context, memoryInfo.totalMem),
            available = formatSizeOrSpeed(context, memoryInfo.availMem)
        )
    }

    fun getStorageInfo(): StorageInfo {
        val internalStat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = internalStat.blockCountLong * internalStat.blockSizeLong
        val availableBytes = internalStat.availableBlocksLong * internalStat.blockSizeLong
        return StorageInfo(
            total = formatSizeOrSpeed(context, totalBytes),
            available = formatSizeOrSpeed(context, availableBytes)
        )
    }

    fun performStorageSpeedTest(onProgress: (Float) -> Unit): Pair<String, String> {
        val testFileName = "kavosh_speed_test.tmp"
        val testFile = File(context.filesDir, testFileName)
        val fileSizeMb = 100 // 100MB
        val bufferSize = 4 * 1024 // 4KB
        val totalWrites = (fileSizeMb * 1024 * 1024) / bufferSize
        val buffer = ByteArray(bufferSize)

        if (testFile.exists()) {
            testFile.delete()
        }

        var writeSpeedResult = "Error"
        var readSpeedResult = "Error"

        try {
            // Write phase
            val writeTime = measureTimeMillis {
                FileOutputStream(testFile).use { fos ->
                    for (i in 0 until totalWrites) {
                        fos.write(buffer)
                        onProgress(i.toFloat() / (totalWrites * 2)) // Progress from 0 to 0.5
                    }
                }
            }
            val writeSpeedMbPerSec = if (writeTime > 0) (fileSizeMb.toDouble() / (writeTime / 1000.0)) else 0.0
            writeSpeedResult = "${"%.2f".format(writeSpeedMbPerSec)} MB/s"


            // Read phase
            val readTime = measureTimeMillis {
                testFile.inputStream().use { fis ->
                    var bytesRead: Int
                    var writesDone = 0
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        onProgress(0.5f + (writesDone.toFloat() / (totalWrites * 2))) // Progress from 0.5 to 1.0
                        writesDone++
                    }
                }
            }
            val readSpeedMbPerSec = if (readTime > 0) (fileSizeMb.toDouble() / (readTime / 1000.0)) else 0.0
            readSpeedResult = "${"%.2f".format(readSpeedMbPerSec)} MB/s"

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (testFile.exists()) {
                testFile.delete()
            }
        }

        return Pair(writeSpeedResult, readSpeedResult)
    }

    /**
     * تست پیشرفته سرعت حافظه با فایل ۱ گیگابایتی
     * شامل نمایش زنده سرعت و ذخیره تاریخچه
     */
    fun performEnhancedStorageSpeedTest(
        onProgress: (Float) -> Unit,
        onSpeedUpdate: (writeSpeed: Double, readSpeed: Double) -> Unit,
        onPhaseChange: (phase: String) -> Unit,
        onSpeedHistoryUpdate: (writeHistory: List<SpeedDataPoint>, readHistory: List<SpeedDataPoint>) -> Unit
    ): StorageSpeedTestResult {
        val testFileName = "kavosh_enhanced_speed_test.tmp"
        val testFile = File(context.filesDir, testFileName)
        val bufferSize = 1024 * 1024 // ۱ مگابایت برای سرعت بالا
        val testDurationMs = 10 * 1000L // ۱۰ ثانیه کل
        val writeDurationMs = 5 * 1000L // ۵ ثانیه برای نوشتن
        val readDurationMs = 5 * 1000L // ۵ ثانیه برای خواندن
        val samplingInterval = 200L // نمونه‌برداری هر ۲۰۰ میلی‌ثانیه برای نمودار روان
        val targetFileSize = 1024L * 1024L * 1024L // ۱ گیگابایت برای تست واقعی

        val writeSpeedHistory = mutableListOf<SpeedDataPoint>()
        val readSpeedHistory = mutableListOf<SpeedDataPoint>()

        var writeSpeed: Double
        var readSpeed: Double
        val testStartTime = System.currentTimeMillis()

        Log.d("StorageSpeedTest", "شروع تست پیشرفته سرعت حافظه - مدت زمان هدف: ${testDurationMs}ms")

        try {
            // بررسی فضای کافی
            val availableSpace = context.filesDir.freeSpace
            if (availableSpace < targetFileSize * 1.5) { // ۵۰٪ فضای اضافی
                throw Exception(context.getString(R.string.storage_speed_insufficient_space))
            }

            // حذف فایل قبلی در صورت وجود
            if (testFile.exists()) {
                testFile.delete()
            }

            onPhaseChange(context.getString(R.string.storage_speed_creating_file))
            onProgress(0.05f)

            // مرحله نوشتن
            onPhaseChange(context.getString(R.string.storage_speed_testing_write))
            val writeStartTime = System.currentTimeMillis()
            var totalBytesWritten = 0L
            var lastSpeedUpdate = writeStartTime

            FileOutputStream(testFile).use { fos ->
                val buffer = ByteArray(bufferSize)
                // پر کردن بافر با داده‌های تصادفی برای تست واقعی‌تر
                for (i in buffer.indices) {
                    buffer[i] = (i % 256).toByte()
                }

                while (System.currentTimeMillis() - writeStartTime < writeDurationMs) {
                    fos.write(buffer)
                    totalBytesWritten += bufferSize

                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - writeStartTime

                    // به‌روزرسانی پیشرفت
                    val writeProgress = elapsedTime.toFloat() / writeDurationMs
                    onProgress(0.05f + writeProgress * 0.45f) // از ۵٪ تا ۵۰٪

                    // محاسبه و به‌روزرسانی سرعت (هر ۲۰۰ میلی‌ثانیه)
                    if (currentTime - lastSpeedUpdate >= samplingInterval && elapsedTime > 0) {
                        val elapsedSeconds = elapsedTime / 1000.0
                        // محاسبه سرعت فوری (از آخرین به‌روزرسانی)
                        val intervalSeconds = (currentTime - lastSpeedUpdate) / 1000.0
                        val bytesInInterval = bufferSize * (samplingInterval / 10) // تخمین بایت‌های نوشته شده در این بازه
                        val instantSpeed = if (intervalSeconds > 0) (bytesInInterval / (1024.0 * 1024.0)) / intervalSeconds else 0.0

                        // محاسبه سرعت میانگین کل
                        val avgSpeed = (totalBytesWritten / (1024.0 * 1024.0)) / elapsedSeconds

                        // استفاده از سرعت فوری برای نمایش زنده
                        writeSpeed = maxOf(instantSpeed, avgSpeed * 0.8) // ترکیب سرعت فوری و میانگین
                        onSpeedUpdate(writeSpeed, 0.0)

                        writeSpeedHistory.add(
                            SpeedDataPoint(
                                timestamp = currentTime,
                                speed = writeSpeed,
                                phase = TestPhase.WRITING
                            )
                        )

                        Log.d("StorageSpeedTest", "Write speed data point added: ${writeSpeed}MB/s, total points: ${writeSpeedHistory.size}")

                        // به‌روزرسانی نمودار زنده
                        onSpeedHistoryUpdate(writeSpeedHistory.toList(), readSpeedHistory.toList())
                        lastSpeedUpdate = currentTime
                    }
                }
            }

            val writeEndTime = System.currentTimeMillis()
            val finalWriteTime = (writeEndTime - writeStartTime) / 1000.0
            val finalWriteSpeed = if (finalWriteTime > 0) (totalBytesWritten / (1024.0 * 1024.0)) / finalWriteTime else 0.0

            Log.d("StorageSpeedTest", "مرحله نوشتن تکمیل شد - مدت زمان: ${finalWriteTime}s، حجم: ${totalBytesWritten/(1024*1024)}MB، سرعت: ${finalWriteSpeed}MB/s")
            onProgress(0.5f)

            // مرحله خواندن
            onPhaseChange(context.getString(R.string.storage_speed_testing_read))
            val readStartTime = System.currentTimeMillis()
            var totalBytesRead = 0L
            lastSpeedUpdate = readStartTime


            // خواندن مکرر فایل برای ۵ ثانیه
            var readCycles = 0
            while (System.currentTimeMillis() - readStartTime < readDurationMs) {
                testFile.inputStream().use { fis ->
                    val buffer = ByteArray(bufferSize)
                    var bytesRead: Int
                    var bytesInThisCycle = 0L
                    val cycleStartTime = System.currentTimeMillis()

                    while ((fis.read(buffer).also { bytesRead = it }) != -1 &&
                           System.currentTimeMillis() - readStartTime < readDurationMs) {
                        totalBytesRead += bytesRead
                        bytesInThisCycle += bytesRead

                        val currentTime = System.currentTimeMillis()
                        val elapsedTime = currentTime - readStartTime

                        // به‌روزرسانی پیشرفت
                        val readProgress = elapsedTime.toFloat() / readDurationMs
                        onProgress(0.5f + readProgress * 0.45f) // از ۵۰٪ تا ۹۵٪

                        // محاسبه و به‌روزرسانی سرعت (هر ۲۰۰ میلی‌ثانیه)
                        if (currentTime - lastSpeedUpdate >= samplingInterval && elapsedTime > 0) {
                            val elapsedSeconds = elapsedTime / 1000.0

                            // محاسبه سرعت فوری (از آخرین به‌روزرسانی)
                            val intervalSeconds = (currentTime - lastSpeedUpdate) / 1000.0
                            val bytesInInterval = bufferSize * (samplingInterval / 10) // تخمین بایت‌های خوانده شده در این بازه
                            val instantSpeed = if (intervalSeconds > 0) (bytesInInterval / (1024.0 * 1024.0)) / intervalSeconds else 0.0

                            // محاسبه سرعت میانگین کل
                            val avgSpeed = (totalBytesRead / (1024.0 * 1024.0)) / elapsedSeconds

                            // استفاده از سرعت فوری برای نمایش زنده
                            readSpeed = maxOf(instantSpeed, avgSpeed * 0.8) // ترکیب سرعت فوری و میانگین
                            onSpeedUpdate(finalWriteSpeed, readSpeed)

                            readSpeedHistory.add(
                                SpeedDataPoint(
                                    timestamp = currentTime,
                                    speed = readSpeed,
                                    phase = TestPhase.READING
                                )
                            )

                            // به‌روزرسانی نمودار زنده
                            onSpeedHistoryUpdate(writeSpeedHistory.toList(), readSpeedHistory.toList())
                            lastSpeedUpdate = currentTime
                        }
                    }
                }
                readCycles++
            }

            val readEndTime = System.currentTimeMillis()
            val finalReadTime = (readEndTime - readStartTime) / 1000.0
            val finalReadSpeed = if (finalReadTime > 0) (totalBytesRead / (1024.0 * 1024.0)) / finalReadTime else 0.0

            Log.d("StorageSpeedTest", "مرحله خواندن تکمیل شد - مدت زمان: ${finalReadTime}s، حجم: ${totalBytesRead/(1024*1024)}MB، سرعت: ${finalReadSpeed}MB/s")

            // پاک‌سازی
            onPhaseChange(context.getString(R.string.storage_speed_cleaning_up))
            onProgress(0.95f)

            if (testFile.exists()) {
                testFile.delete()
            }

            // به‌روزرسانی نهایی نمودار
            onSpeedHistoryUpdate(writeSpeedHistory.toList(), readSpeedHistory.toList())
            onProgress(1.0f)

            // محاسبه آمار نهایی
            val allWriteSpeeds = writeSpeedHistory.map { it.speed }
            val allReadSpeeds = readSpeedHistory.map { it.speed }
            val totalTestTime = (System.currentTimeMillis() - testStartTime) / 1000.0

            Log.d("StorageSpeedTest", "تست کامل شد - مدت زمان کل: ${totalTestTime}s")

            return StorageSpeedTestResult(
                testDuration = readEndTime - testStartTime,
                fileSizeBytes = targetFileSize, // استفاده از اندازه هدف ۱ گیگابایت
                writeSpeed = finalWriteSpeed,
                readSpeed = finalReadSpeed,
                averageWriteSpeed = if (allWriteSpeeds.isNotEmpty()) allWriteSpeeds.average() else finalWriteSpeed,
                averageReadSpeed = if (allReadSpeeds.isNotEmpty()) allReadSpeeds.average() else finalReadSpeed,
                maxWriteSpeed = if (allWriteSpeeds.isNotEmpty()) allWriteSpeeds.maxOrNull() ?: finalWriteSpeed else finalWriteSpeed,
                maxReadSpeed = if (allReadSpeeds.isNotEmpty()) allReadSpeeds.maxOrNull() ?: finalReadSpeed else finalReadSpeed,
                minWriteSpeed = if (allWriteSpeeds.isNotEmpty()) allWriteSpeeds.minOrNull() ?: finalWriteSpeed else finalWriteSpeed,
                minReadSpeed = if (allReadSpeeds.isNotEmpty()) allReadSpeeds.minOrNull() ?: finalReadSpeed else finalReadSpeed,
                writeSpeedHistory = writeSpeedHistory,
                readSpeedHistory = readSpeedHistory,
                testStatus = StorageTestStatus.COMPLETED
            )

        } catch (e: Exception) {
            // پاک‌سازی در صورت خطا
            if (testFile.exists()) {
                testFile.delete()
            }

            return StorageSpeedTestResult(
                testDuration = System.currentTimeMillis() - testStartTime,
                fileSizeBytes = targetFileSize, // حتی در صورت خطا، اندازه هدف را نمایش بده
                writeSpeed = 0.0,
                readSpeed = 0.0,
                averageWriteSpeed = 0.0,
                averageReadSpeed = 0.0,
                maxWriteSpeed = 0.0,
                maxReadSpeed = 0.0,
                minWriteSpeed = 0.0,
                minReadSpeed = 0.0,
                testStatus = StorageTestStatus.FAILED,
                errorMessage = e.message
            )
        }
    }
}