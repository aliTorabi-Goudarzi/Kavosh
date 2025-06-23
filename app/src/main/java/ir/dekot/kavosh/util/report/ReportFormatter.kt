package ir.dekot.kavosh.util.report

import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory

object ReportFormatter {

    /**
     * یک گزارش متنی از یک دسته‌بندی خاص، مناسب برای اشتراک‌گذاری، تولید می‌کند.
     */
    fun formatInfoForSharing(
        category: InfoCategory,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): String {
        val body = getCategoryData(category, deviceInfo, batteryInfo)
            .joinToString(separator = "\n") { (label, value) ->
                // برای هدرهای داخلی، فقط لیبل را نمایش بده
                if (value.isEmpty()) label else "$label: $value"
            }

        return """
        --- ${category.title} ---
        $body

        ارسال شده توسط اپلیکیشن کاوش
        """.trimIndent()
    }

    /**
     * یک گزارش کامل متنی از تمام اطلاعات دستگاه تولید می‌کند.
     */
    fun formatFullReport(deviceInfo: DeviceInfo, batteryInfo: BatteryInfo): String {
        val builder = StringBuilder()
        builder.appendLine("گزارش کامل مشخصات دستگاه - اپلیکیشن کاوش")
        builder.appendLine("========================================")
        builder.appendLine()

        InfoCategory.entries.forEach { category ->
            builder.appendLine("--- ${category.title} ---")
            val body = getCategoryData(category, deviceInfo, batteryInfo)
                .joinToString(separator = "\n") { (label, value) ->
                    if (value.isEmpty()) label else "$label: $value"
                }
            builder.appendLine(body)
            builder.appendLine("\n----------------------------------------\n")
        }

        return builder.toString()
    }

    /**
     * داده‌های یک دسته‌بندی را به صورت یک لیست ساختاریافته از جفت‌های (برچسب، مقدار) برمی‌گرداند.
     * این تابع، هسته اصلی منطق برای دیالوگ انتخاب کپی است.
     * برای هدرهای داخلی (مثل GPU)، مقدار خالی در نظر گرفته می‌شود.
     */
    fun getCategoryData(
        category: InfoCategory,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): List<Pair<String, String>> {
        return when (category) {
            InfoCategory.SOC -> listOf(
                "مدل CPU" to deviceInfo.cpu.model,
                "معماری" to deviceInfo.cpu.architecture,
                "توپولوژی" to deviceInfo.cpu.topology,
                "--- پردازنده گرافیکی (GPU) ---" to "",
                "مدل GPU" to deviceInfo.gpu.model,
                "سازنده" to deviceInfo.gpu.vendor,
                "--- حافظه RAM ---" to "",
                "کل حافظه" to deviceInfo.ram.total,
                "حافظه در دسترس" to deviceInfo.ram.available
            )

            InfoCategory.DEVICE -> listOf(
                "--- صفحه نمایش ---" to "",
                "رزولوشن" to deviceInfo.display.resolution,
                "تراکم پیکسلی" to deviceInfo.display.density,
                "نرخ نوسازی" to deviceInfo.display.refreshRate,
                "--- حافظه داخلی ---" to "",
                "کل حافظه" to deviceInfo.storage.total,
                "حافظه در دسترس" to deviceInfo.storage.available
            )

            InfoCategory.SYSTEM -> listOf(
                "نسخه اندروید" to deviceInfo.system.androidVersion,
                "سطح API" to deviceInfo.system.sdkLevel,
                "بیلد نامبر" to deviceInfo.system.buildNumber,
                "وضعیت روت" to if (deviceInfo.system.isRooted) "روت شده" else "روت نشده"
            )

            InfoCategory.BATTERY -> listOf(
                "سلامت" to batteryInfo.health,
                "درصد شارژ" to "${batteryInfo.level}%",
                "وضعیت شارژ" to batteryInfo.status,
                "تکنولوژی" to batteryInfo.technology,
                "دما" to batteryInfo.temperature,
                "ولتاژ" to batteryInfo.voltage
            )

            InfoCategory.SENSORS ->
                deviceInfo.sensors.map { "- ${it.name}" to "(${it.vendor})" }
                    .ifEmpty { listOf("سنسور" to "سنسوری یافت نشد.") }

            InfoCategory.THERMAL ->
                deviceInfo.thermal.map { it.type to it.temperature }
                    .ifEmpty { listOf("دما" to "اطلاعات دما در دسترس نیست.") }

            InfoCategory.NETWORK ->
                buildList {
                    add("نوع اتصال" to deviceInfo.network.networkType)
                    add("آدرس IPv4" to deviceInfo.network.ipAddressV4)
                    add("آدرس IPv6" to deviceInfo.network.ipAddressV6)
                    if (deviceInfo.network.networkType == "Wi-Fi") {
                        add("SSID" to deviceInfo.network.ssid)
                        add("DNS 1" to deviceInfo.network.dns1)
                    }
                }

            InfoCategory.CAMERA ->
                if (deviceInfo.cameras.isEmpty()) {
                    listOf("دوربین" to "دوربینی یافت نشد یا دسترسی ممکن نیست.")
                } else {
                    deviceInfo.cameras.flatMap { camera ->
                        listOf(
                            "[ ${camera.name} ]" to "",
                            "مگاپیکسل" to camera.megapixels,
                            "حداکثر رزولوشن" to camera.maxResolution,
                            "فلش" to if (camera.hasFlash) "دارد" else "ندارد",
                            "دیافراگم‌ها" to camera.apertures,
                            "فاصله کانونی" to camera.focalLengths,
                            "اندازه سنسور" to camera.sensorSize,
                            "" to "" // برای ایجاد یک خط خالی بین دوربین‌ها
                        )
                    }
                }
        }.filter { it.first.isNotBlank() || it.second.isNotBlank() } // حذف خطوط کاملا خالی
    }
}