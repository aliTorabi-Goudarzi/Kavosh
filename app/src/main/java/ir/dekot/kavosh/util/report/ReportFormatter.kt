package ir.dekot.kavosh.util.report

import android.content.Context
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.ui.viewmodel.getTitle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object ReportFormatter {

    /**
     * یک گزارش متنی کامل از تمام اطلاعات دستگاه تولید می‌کند.
     */
    fun formatFullReport(context: Context, deviceInfo: DeviceInfo, batteryInfo: BatteryInfo): String {
        val builder = StringBuilder()
        builder.appendLine(context.getString(R.string.full_report_title))
        builder.appendLine("========================================")
        builder.appendLine()

        InfoCategory.entries.forEach { category ->
            builder.appendLine("--- ${category.getTitle(context)} ---")
            val body = getCategoryData(context, category, deviceInfo, batteryInfo)
                .joinToString(separator = "\n") { (label, value) ->
                    if (value.isEmpty()) label else "$label: $value"
                }
            builder.appendLine(body)
            builder.appendLine("\n----------------------------------------\n")
        }

        return builder.toString()
    }

    /**
     * یک گزارش JSON از تمام اطلاعات دستگاه تولید می‌کند.
     */
    fun formatJsonReport(deviceInfo: DeviceInfo, batteryInfo: BatteryInfo): String {
        val jsonObject = buildJsonObject {
            put("device_info", buildJsonObject {
                put("cpu", buildJsonObject {
                    put("model", deviceInfo.cpu.model)
                    put("architecture", deviceInfo.cpu.architecture)
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
                put("display", buildJsonObject {
                    put("resolution", deviceInfo.display.resolution)
                    put("density", deviceInfo.display.density)
                    put("refresh_rate", deviceInfo.display.refreshRate)
                })
                put("storage", buildJsonObject {
                    put("total", deviceInfo.storage.total)
                    put("available", deviceInfo.storage.available)
                })
                put("system", buildJsonObject {
                    put("android_version", deviceInfo.system.androidVersion)
                    put("sdk_level", deviceInfo.system.sdkLevel)
                    put("build_number", deviceInfo.system.buildNumber)
                    put("is_rooted", deviceInfo.system.isRooted)
                })
                put("network", buildJsonObject {
                    put("type", deviceInfo.network.networkType)
                    put("ipv4", deviceInfo.network.ipAddressV4)
                    put("ipv6", deviceInfo.network.ipAddressV6)
                    put("ssid", deviceInfo.network.ssid)
                    put("dns1", deviceInfo.network.dns1)
                })
            })
            put("battery_info", buildJsonObject {
                put("health", batteryInfo.health)
                put("level", batteryInfo.level)
                put("status", batteryInfo.status)
                put("technology", batteryInfo.technology)
                put("temperature", batteryInfo.temperature)
                put("voltage", batteryInfo.voltage)
            })
            put("timestamp", System.currentTimeMillis())
        }

        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), jsonObject)
    }

    fun getCategoryData(
        context: Context,
        category: InfoCategory,
        deviceInfo: DeviceInfo,
        batteryInfo: BatteryInfo
    ): List<Pair<String, String>> {
        return when (category) {
            InfoCategory.SOC -> listOf(
                context.getString(R.string.cpu_model) to deviceInfo.cpu.model,
                context.getString(R.string.cpu_architecture) to deviceInfo.cpu.architecture,
                context.getString(R.string.cpu_topology) to deviceInfo.cpu.topology,
                "--- ${context.getString(R.string.gpu_title)} ---" to "",
                context.getString(R.string.gpu_model) to deviceInfo.gpu.model,
                context.getString(R.string.gpu_vendor) to deviceInfo.gpu.vendor,
                "--- ${context.getString(R.string.ram_title)} ---" to "",
                context.getString(R.string.ram_total) to deviceInfo.ram.total,
                context.getString(R.string.ram_available) to deviceInfo.ram.available
            )
            InfoCategory.DEVICE -> listOf(
                "--- ${context.getString(R.string.display_title)} ---" to "",
                context.getString(R.string.display_resolution) to deviceInfo.display.resolution,
                context.getString(R.string.display_density) to deviceInfo.display.density,
                context.getString(R.string.display_refresh_rate) to deviceInfo.display.refreshRate,
                "--- ${context.getString(R.string.storage_title)} ---" to "",
                context.getString(R.string.storage_total) to deviceInfo.storage.total,
                context.getString(R.string.storage_available) to deviceInfo.storage.available
            )
            InfoCategory.SYSTEM -> listOf(
                context.getString(R.string.system_android_version) to deviceInfo.system.androidVersion,
                context.getString(R.string.system_sdk_level) to deviceInfo.system.sdkLevel,
                context.getString(R.string.system_build_number) to deviceInfo.system.buildNumber,
                context.getString(R.string.system_root_status) to context.getString(
                    if (deviceInfo.system.isRooted) R.string.label_rooted else R.string.label_not_rooted
                )
            )
            InfoCategory.BATTERY -> listOf(
                context.getString(R.string.battery_health) to batteryInfo.health,
                context.getString(R.string.battery_level) to context.getString(R.string.unit_format_percent, batteryInfo.level),
                context.getString(R.string.battery_status) to batteryInfo.status,
                context.getString(R.string.battery_technology) to batteryInfo.technology,
                context.getString(R.string.battery_temperature) to batteryInfo.temperature,
                context.getString(R.string.battery_voltage) to batteryInfo.voltage
            )
            InfoCategory.SENSORS ->
                deviceInfo.sensors.map { "- ${it.name}" to "(${context.getString(R.string.sensor_vendor, it.vendor)})" }
                    .ifEmpty { listOf(context.getString(R.string.category_sensors) to "Not found") }
            InfoCategory.THERMAL ->
                deviceInfo.thermal.map { it.type to it.temperature }
                    .ifEmpty { listOf(context.getString(R.string.category_thermal) to context.getString(R.string.label_not_available)) }
            InfoCategory.NETWORK ->
                buildList {
                    add(context.getString(R.string.network_connection_type) to deviceInfo.network.networkType)
                    add(context.getString(R.string.network_ipv4) to deviceInfo.network.ipAddressV4)
                    add(context.getString(R.string.network_ipv6) to deviceInfo.network.ipAddressV6)
                    if (deviceInfo.network.networkType == "Wi-Fi") {
                        add(context.getString(R.string.network_ssid) to deviceInfo.network.ssid)
                        add(context.getString(R.string.network_dns1) to deviceInfo.network.dns1)
                    }
                }
            InfoCategory.CAMERA ->
                if (deviceInfo.cameras.isEmpty()) {
                    listOf(context.getString(R.string.category_camera) to context.getString(R.string.label_not_available))
                } else {
                    deviceInfo.cameras.flatMap { camera ->
                        listOf(
                            "[ ${camera.name} ]" to "",
                            context.getString(R.string.camera_megapixels) to camera.megapixels,
                            context.getString(R.string.camera_max_resolution) to camera.maxResolution,
                            context.getString(R.string.camera_flash_support) to context.getString(if (camera.hasFlash) R.string.label_yes else R.string.label_no),
                            context.getString(R.string.camera_apertures) to camera.apertures,
                            context.getString(R.string.camera_focal_lengths) to camera.focalLengths,
                            context.getString(R.string.camera_sensor_size) to camera.sensorSize,
                            "" to ""
                        )
                    }
                }
            // **اصلاح کلیدی: افزودن branch برای دسته‌بندی سیم‌کارت**
            InfoCategory.SIM ->
                if (deviceInfo.simCards.isEmpty()) {
                    listOf(context.getString(R.string.category_sim) to context.getString(R.string.label_not_available))
                } else {
                    deviceInfo.simCards.flatMap { sim ->
                        listOf(
                            "[ ${context.getString(R.string.sim_slot_index, sim.slotIndex + 1)} ]" to "",
                            context.getString(R.string.sim_carrier) to sim.carrierName,
                            context.getString(R.string.sim_country_iso) to sim.countryIso,
                            context.getString(R.string.sim_country_code) to sim.mobileCountryCode,
                            context.getString(R.string.sim_network_code) to sim.mobileNetworkCode,
                            context.getString(R.string.sim_is_roaming) to context.getString(if (sim.isRoaming) R.string.label_on else R.string.label_off),
                            context.getString(R.string.sim_data_roaming) to sim.dataRoaming,
                            "" to ""
                        )
                    }
                }

            InfoCategory.APPS ->
                if (deviceInfo.apps.isEmpty()) {
                    listOf("اپلیکیشن‌های نصب شده" to "در دسترس نیست")
                } else {
                    deviceInfo.apps.take(10).map { app: ir.dekot.kavosh.data.model.components.AppInfo ->
                        app.appName to "نسخه ${app.versionName}"
                    }
                }
        }.filter { it.first.isNotBlank() || it.second.isNotBlank() }
    }
}