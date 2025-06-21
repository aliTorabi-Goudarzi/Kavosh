package ir.dekot.kavosh.util.report

import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

/**
 * یک آبجکت کمکی برای تبدیل اطلاعات دستگاه به یک رشته قابل اشتراک‌گذاری.
 * این کلاس برای خوانایی و نگهداری بهتر بازسازی شده است.
 */
object ReportFormatter {

    /**
     * گزارشی برای یک دسته‌بندی خاص، مناسب برای اشتراک‌گذاری، تولید می‌کند.
     * این تابع یک هدر و فوتر استاندارد به گزارش اضافه می‌کند.
     */
    fun formatInfoForSharing(
        category: InfoCategory,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): String {
        val body = formatCategoryBody(category, deviceInfo, batteryInfo)
        return """
        --- ${category.title} ---
        $body

        ارسال شده توسط اپلیکیشن کاوش
        """.trimIndent()
    }

    /**
     * یک گزارش کامل متنی از تمام اطلاعات دستگاه تولید می‌کند.
     * این تابع حالا بسیار خواناتر و بهینه‌تر شده است.
     */
    fun formatFullReport(deviceInfo: DeviceInfo, batteryInfo: BatteryInfo): String {
        val builder = StringBuilder()
        builder.appendLine("گزارش کامل مشخصات دستگاه - اپلیکیشن کاوش")
        builder.appendLine("========================================")
        builder.appendLine()

        InfoCategory.entries.forEach { category ->
            builder.appendLine("--- ${category.title} ---")
            // دیگر نیازی به دستکاری رشته نیست
            builder.appendLine(formatCategoryBody(category, deviceInfo, batteryInfo))
            builder.appendLine("\n----------------------------------------\n")
        }

        return builder.toString()
    }

    /**
     * بدنه اصلی گزارش را برای یک دسته‌بندی مشخص تولید می‌کند.
     * این تابع داخلی است و فقط توسط توابع دیگر این کلاس استفاده می‌شود.
     */
    internal fun formatCategoryBody(
        category: InfoCategory,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): String {
        return when (category) {
            InfoCategory.SOC -> """
                مدل CPU: ${deviceInfo.cpu.model}
                معماری: ${deviceInfo.cpu.architecture}
                توپولوژی: ${deviceInfo.cpu.topology}
                
                --- پردازنده گرافیکی (GPU) ---
                مدل GPU: ${deviceInfo.gpu.model}
                سازنده: ${deviceInfo.gpu.vendor}
                
                --- حافظه RAM ---
                کل حافظه: ${deviceInfo.ram.total}
                حافظه در دسترس: ${deviceInfo.ram.available}
            """.trimIndent()

            InfoCategory.DEVICE -> """
                --- صفحه نمایش ---
                رزولوشن: ${deviceInfo.display.resolution}
                تراکم پیکسلی: ${deviceInfo.display.density}
                نرخ نوسازی: ${deviceInfo.display.refreshRate}
                
                --- حافظه داخلی ---
                کل حافظه: ${deviceInfo.storage.total}
                حافظه در دسترس: ${deviceInfo.storage.available}
            """.trimIndent()

            InfoCategory.SYSTEM -> """
                نسخه اندروید: ${deviceInfo.system.androidVersion}
                سطح API: ${deviceInfo.system.sdkLevel}
                بیلد نامبر: ${deviceInfo.system.buildNumber}
                وضعیت روت: ${if (deviceInfo.system.isRooted) "روت شده" else "روت نشده"}
            """.trimIndent()

            InfoCategory.BATTERY -> """
                سلامت: ${batteryInfo.health}
                درصد شارژ: ${batteryInfo.level}%
                وضعیت شارژ: ${batteryInfo.status}
                تکنولوژی: ${batteryInfo.technology}
                دما: ${batteryInfo.temperature}
                ولتاژ: ${batteryInfo.voltage}
            """.trimIndent()

            InfoCategory.SENSORS ->
                deviceInfo.sensors.joinToString(separator = "\n") { sensor ->
                    "- ${sensor.name} (${sensor.vendor})"
                }.ifEmpty { "سنسوری یافت نشد." }


            InfoCategory.THERMAL ->
                deviceInfo.thermal.joinToString(separator = "\n") { thermal ->
                    "${thermal.type}: ${thermal.temperature}"
                }.ifEmpty { "اطلاعات دما در دسترس نیست." }


            InfoCategory.NETWORK ->
                buildString {
                    appendLine("نوع اتصال: ${deviceInfo.network.networkType}")
                    appendLine("آدرس IPv4: ${deviceInfo.network.ipAddressV4}")
                    appendLine("آدرس IPv6: ${deviceInfo.network.ipAddressV6}")
                    if (deviceInfo.network.networkType == "Wi-Fi") {
                        appendLine("SSID: ${deviceInfo.network.ssid}")
                        appendLine("DNS 1: ${deviceInfo.network.dns1}")
                    }
                }.trim()

            InfoCategory.CAMERA ->
                if (deviceInfo.cameras.isEmpty()) {
                    "دوربینی یافت نشد یا دسترسی ممکن نیست."
                } else {
                    deviceInfo.cameras.joinToString(separator = "\n\n") { camera ->
                        """
                        [ ${camera.name} ]
                          مگاپیکسل: ${camera.megapixels}
                          حداکثر رزولوشن: ${camera.maxResolution}
                          فلش: ${if (camera.hasFlash) "دارد" else "ندارد"}
                          دیافراگم‌ها: ${camera.apertures}
                          فاصله کانونی: ${camera.focalLengths}
                          اندازه سنسور: ${camera.sensorSize}
                        """.trimIndent()
                    }
                }
        }
    }
}