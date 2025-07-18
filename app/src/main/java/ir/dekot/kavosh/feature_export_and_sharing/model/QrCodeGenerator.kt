package ir.dekot.kavosh.feature_export_and_sharing.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import ir.dekot.kavosh.feature_deviceInfo.model.BatteryInfo
import ir.dekot.kavosh.feature_deviceInfo.model.DeviceInfo
import java.io.FileOutputStream

/**
 * کلاس تولید QR کد برای اشتراک‌گذاری اطلاعات دستگاه
 */
object QrCodeGenerator {

    private const val QR_CODE_SIZE = 800
    private const val MARGIN = 50
    private const val TEXT_SIZE = 24f

    /**
     * تولید QR کد کامل برای اشتراک‌گذاری
     */
    fun generateQuickShareQrCode(
        context: Context,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): Bitmap {
        val completeInfo = buildString {
            append("📱 اطلاعات دستگاه کاوش\n")
            append("═══════════════════════\n")
            append("🏷️ برند: ${Build.MANUFACTURER}\n")
            append("📱 مدل: ${Build.MODEL}\n")
            append("🤖 اندروید: ${deviceInfo.system.androidVersion}\n")
            append("🔢 SDK: ${deviceInfo.system.sdkLevel}\n")
            append("🔋 باتری: ${batteryInfo.level}% (${batteryInfo.status})\n")
            append("🌡️ دمای باتری: ${batteryInfo.temperature}\n")
            append("⚡ ولتاژ: ${batteryInfo.voltage}\n")
            append("🧠 پردازنده: ${deviceInfo.cpu.model}\n")
            append("🏗️ معماری: ${deviceInfo.cpu.architecture}\n")
            append("💾 رم: ${deviceInfo.ram.total}\n")
            append("💿 حافظه: ${deviceInfo.storage.total}\n")
            append("📺 نمایشگر: ${deviceInfo.display.resolution}\n")
            append("🔄 نرخ تازه‌سازی: ${deviceInfo.display.refreshRate}\n")
            append("🎯 تراکم پیکسل: ${deviceInfo.display.density}\n")
            append("🎮 GPU: ${deviceInfo.gpu.model}\n")
            if (deviceInfo.cameras.isNotEmpty()) {
                append("📷 دوربین‌ها: ${deviceInfo.cameras.size} عدد\n")
            }
            if (deviceInfo.simCards.isNotEmpty()) {
                append("📶 سیم‌کارت‌ها: ${deviceInfo.simCards.size} عدد\n")
            }
            append("📦 برنامه‌ها: ${deviceInfo.apps.size} عدد\n")
            append("═══════════════════════\n")
            append("🚀 تولید شده با اپلیکیشن کاوش")
        }

        return createStyledQrCode(context, completeInfo, "اطلاعات کامل دستگاه")
    }

    /**
     * تولید QR کد برای اشتراک‌گذاری آنلاین
     */
    fun generateShareableQrCode(
        context: Context,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): Bitmap {
        val jsonData = createDeviceInfoJson(deviceInfo, batteryInfo)
        val shareUrl = generateShareUrl(jsonData)
        return generateShareUrlQrCode(context, shareUrl)
    }

    /**
     * ایجاد QR کد ساده
     */
    fun createSimpleQrCode(data: String): Bitmap {
        val writer = QRCodeWriter()
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
        }
        
        return try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints)
            createBitmapFromBitMatrix(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            createErrorBitmap()
        }
    }

    /**
     * ایجاد QR کد با استایل حرفه‌ای
     */
    fun createStyledQrCode(context: Context, data: String, title: String): Bitmap {
        val qrBitmap = createSimpleQrCode(data)
        
        // ایجاد bitmap نهایی با فضای اضافی برای عنوان
        val finalHeight = QR_CODE_SIZE + MARGIN * 3 + TEXT_SIZE.toInt() * 2
        val finalBitmap = createBitmap(QR_CODE_SIZE + MARGIN * 2, finalHeight)
        val canvas = Canvas(finalBitmap)
        
        // پس‌زمینه سفید
        canvas.drawColor(Color.WHITE)
        
        // رسم QR کد
        canvas.drawBitmap(qrBitmap, MARGIN.toFloat(), (MARGIN + TEXT_SIZE.toInt() + MARGIN / 2).toFloat(), null)
        
        // رسم عنوان
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = TEXT_SIZE
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        canvas.drawText(
            title,
            (QR_CODE_SIZE + MARGIN * 2) / 2f,
            MARGIN + TEXT_SIZE,
            titlePaint
        )
        
        // رسم متن توضیحی
        val descPaint = Paint().apply {
            color = Color.GRAY
            textSize = TEXT_SIZE * 0.6f
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        canvas.drawText(
            "اسکن کنید تا اطلاعات را مشاهده کنید",
            (QR_CODE_SIZE + MARGIN * 2) / 2f,
            finalHeight - MARGIN.toFloat(),
            descPaint
        )
        
        return finalBitmap
    }

    /**
     * تبدیل BitMatrix به Bitmap
     */
    private fun createBitmapFromBitMatrix(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        
        return bitmap
    }

    /**
     * ایجاد bitmap خطا
     */
    private fun createErrorBitmap(): Bitmap {
        val bitmap = createBitmap(QR_CODE_SIZE, QR_CODE_SIZE)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            color = Color.RED
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        canvas.drawText("خطا در تولید QR کد", QR_CODE_SIZE / 2f, QR_CODE_SIZE / 2f, paint)
        return bitmap
    }

    /**
     * تولید URL برای اشتراک‌گذاری آنلاین
     */
    fun generateShareUrl(data: String): String {
        val encodedData = Base64.encodeToString(
            data.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP
        )
        
        return "https://kavosh.app/share?data=$encodedData"
    }

    /**
     * تولید QR کد برای URL اشتراک‌گذاری
     */
    fun generateShareUrlQrCode(context: Context, shareUrl: String): Bitmap {
        return createStyledQrCode(context, shareUrl, "لینک اشتراک‌گذاری")
    }

    /**
     * ذخیره QR کد به عنوان فایل PNG
     */
    fun saveQrCodeAsPng(bitmap: Bitmap, outputStream: FileOutputStream) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    /**
     * ایجاد JSON کامل برای اطلاعات دستگاه
     */
    private fun createDeviceInfoJson(deviceInfo: DeviceInfo, batteryInfo: BatteryInfo): String {
        return buildJsonObject {
            put("type", "kavosh_device_info")
            put("timestamp", System.currentTimeMillis())
            put("device", buildJsonObject {
                put("manufacturer", Build.MANUFACTURER)
                put("model", Build.MODEL)
                put("brand", Build.BRAND)
            })
            put("system", buildJsonObject {
                put("android_version", deviceInfo.system.androidVersion)
                put("sdk_level", deviceInfo.system.sdkLevel)
                put("build_number", deviceInfo.system.buildNumber)
                put("is_rooted", deviceInfo.system.isRooted)
            })
            put("hardware", buildJsonObject {
                put("cpu", buildJsonObject {
                    put("model", deviceInfo.cpu.model)
                    put("architecture", deviceInfo.cpu.architecture)
                    put("core_count", deviceInfo.cpu.coreCount)
                    put("topology", deviceInfo.cpu.topology)
                })
                put("gpu", buildJsonObject {
                    put("model", deviceInfo.gpu.model)
                    put("vendor", deviceInfo.gpu.vendor)
                })
                put("ram", buildJsonObject {
                    put("total", deviceInfo.ram.total)
                    put("available", deviceInfo.ram.available)
                })
                put("storage", buildJsonObject {
                    put("total", deviceInfo.storage.total)
                    put("available", deviceInfo.storage.available)
                })
                put("display", buildJsonObject {
                    put("resolution", deviceInfo.display.resolution)
                    put("density", deviceInfo.display.density)
                    put("refresh_rate", deviceInfo.display.refreshRate)
                })
            })
            put("battery", buildJsonObject {
                put("level", batteryInfo.level)
                put("status", batteryInfo.status)
                put("health", batteryInfo.health)
                put("technology", batteryInfo.technology)
                put("temperature", batteryInfo.temperature)
                put("voltage", batteryInfo.voltage)
            })
            put("counts", buildJsonObject {
                put("cameras", deviceInfo.cameras.size)
                put("sim_cards", deviceInfo.simCards.size)
                put("sensors", deviceInfo.sensors.size)
                put("apps", deviceInfo.apps.size)
            })
        }.toString()
    }
}
