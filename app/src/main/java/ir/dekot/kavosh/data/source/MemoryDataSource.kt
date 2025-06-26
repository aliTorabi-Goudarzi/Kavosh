package ir.dekot.kavosh.data.source

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.util.formatSizeOrSpeed
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.measureTimeMillis

@Singleton
class MemoryDataSource @Inject constructor(@ApplicationContext private val context: Context) {

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
}