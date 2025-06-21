package ir.dekot.kavosh.util.report

import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

/**
 * یک آبجکت کمکی برای تبدیل اطلاعات دستگاه به یک رشته قابل اشتراک‌گذاری.
 */
object ReportFormatter {

    /**
     * یک گزارش کامل متنی از تمام اطلاعات دستگاه تولید می‌کند.
     */
    fun formatFullReport(deviceInfo: DeviceInfo, batteryInfo: BatteryInfo): String {
        val builder = StringBuilder()
        builder.appendLine("گزارش کامل مشخصات دستگاه - اپلیکیشن کاوش")
        builder.appendLine("========================================")
        builder.appendLine()

        // تمام دسته‌بندی‌ها را به ترتیب به گزارش اضافه می‌کنیم
        InfoCategory.entries.forEach { category ->
            // استفاده مستقیم از خصوصیت title در enum
            builder.appendLine("--- ${category.title} ---")
            val sectionText = formatInfoForSharing(category, deviceInfo, batteryInfo)
                .lines()
                .drop(1)
                .dropLast(2)
                .joinToString(separator = "\n")

            builder.appendLine(sectionText)
            builder.appendLine("\n----------------------------------------\n")
        }

        return builder.toString()
    }

    fun formatInfoForSharing(
        category: InfoCategory,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): String {
        val builder = StringBuilder()

        when (category) {
            InfoCategory.SOC -> {
                builder.appendLine("--- پردازنده (SOC) ---")
                builder.appendLine("مدل CPU: ${deviceInfo.cpu.model}")
                builder.appendLine("معماری: ${deviceInfo.cpu.architecture}")
                builder.appendLine("توپولوژی: ${deviceInfo.cpu.topology}")
                builder.appendLine("\n--- پردازنده گرافیکی (GPU) ---")
                builder.appendLine("مدل GPU: ${deviceInfo.gpu.model}")
                builder.appendLine("سازنده: ${deviceInfo.gpu.vendor}")
                builder.appendLine("\n--- حافظه RAM ---")
                builder.appendLine("کل حافظه: ${deviceInfo.ram.total}")
                builder.appendLine("حافظه در دسترس: ${deviceInfo.ram.available}")
            }

            InfoCategory.DEVICE -> {
                builder.appendLine("--- صفحه نمایش ---")
                builder.appendLine("رزولوشن: ${deviceInfo.display.resolution}")
                builder.appendLine("تراکم پیکسلی: ${deviceInfo.display.density}")
                builder.appendLine("نرخ نوسازی: ${deviceInfo.display.refreshRate}")
                builder.appendLine("\n--- حافظه داخلی ---")
                builder.appendLine("کل حافظه: ${deviceInfo.storage.total}")
                builder.appendLine("حافظه در دسترس: ${deviceInfo.storage.available}")
            }

            InfoCategory.SYSTEM -> {
                builder.appendLine("--- سیستم عامل ---")
                builder.appendLine("نسخه اندروید: ${deviceInfo.system.androidVersion}")
                builder.appendLine("سطح API: ${deviceInfo.system.sdkLevel}")
                builder.appendLine("بیلد نامبر: ${deviceInfo.system.buildNumber}")
                builder.appendLine("وضعیت روت: ${if (deviceInfo.system.isRooted) "روت شده" else "روت نشده"}")
            }

            InfoCategory.BATTERY -> {
                builder.appendLine("--- باتری ---")
                builder.appendLine("سلامت: ${batteryInfo.health}")
                builder.appendLine("درصد شارژ: ${batteryInfo.level}%")
                builder.appendLine("وضعیت شارژ: ${batteryInfo.status}")
                builder.appendLine("تکنولوژی: ${batteryInfo.technology}")
                builder.appendLine("دما: ${batteryInfo.temperature}")
                builder.appendLine("ولتاژ: ${batteryInfo.voltage}")
            }

            InfoCategory.SENSORS -> {
                builder.appendLine("--- سنسورها ---")
                deviceInfo.sensors.forEach { sensor ->
                    builder.appendLine("- ${sensor.name} (${sensor.vendor})")
                }
            }

            InfoCategory.THERMAL -> {
                builder.appendLine("--- دما (Thermal) ---")
                deviceInfo.thermal.forEach { thermal ->
                    builder.appendLine("${thermal.type}: ${thermal.temperature}")
                }
            }

            InfoCategory.NETWORK -> {
                builder.appendLine("--- شبکه ---")
                builder.appendLine("نوع اتصال: ${deviceInfo.network.networkType}")
                builder.appendLine("آدرس IPv4: ${deviceInfo.network.ipAddressV4}")
                builder.appendLine("آدرس IPv6: ${deviceInfo.network.ipAddressV6}")
                if (deviceInfo.network.networkType == "Wi-Fi") {
                    builder.appendLine("SSID: ${deviceInfo.network.ssid}")
                    builder.appendLine("DNS 1: ${deviceInfo.network.dns1}")
                }
            }
            InfoCategory.CAMERA -> {
                builder.appendLine("--- اطلاعات دوربین ---")
                if (deviceInfo.cameras.isEmpty()) {
                    builder.appendLine("دوربینی یافت نشد یا دسترسی ممکن نیست.")
                } else {
                    deviceInfo.cameras.forEach { camera ->
                        builder.appendLine("\n[ ${camera.name} ]")
                        builder.appendLine("  مگاپیکسل: ${camera.megapixels}")
                        builder.appendLine("  حداکثر رزولوشن: ${camera.maxResolution}")
                        builder.appendLine("  فلش: ${if (camera.hasFlash) "دارد" else "ندارد"}")
                        builder.appendLine("  دیافراگم‌ها: ${camera.apertures}")
                        builder.appendLine("  فاصله کانونی: ${camera.focalLengths}")
                        builder.appendLine("  اندازه سنسور: ${camera.sensorSize}")
                    }
                }
            }
        }
        builder.appendLine("\n\nارسال شده توسط اپلیکیشن کاوش")
        return builder.toString()
    }
}