package ir.dekot.kavosh.util.report

import android.annotation.SuppressLint
import android.content.Context
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.diagnostic.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.*

/**
 * فرمت‌کننده گزارش‌های تشخیصی
 * برای تولید گزارش‌های متنی و JSON از نتایج تست‌های تشخیصی
 */
object DiagnosticReportFormatter {

    @SuppressLint("ConstantLocale")
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

    /**
     * تولید گزارش کامل بررسی سلامت
     */
    fun formatHealthCheckReport(context: Context, result: HealthCheckResult): String {
        val builder = StringBuilder()
        
        // عنوان گزارش
        builder.appendLine("=".repeat(50))
        builder.appendLine(context.getString(R.string.health_check_title))
        builder.appendLine("=".repeat(50))
        builder.appendLine()
        
        // اطلاعات کلی
        builder.appendLine("📊 ${context.getString(R.string.health_check_overall_health)}")
        builder.appendLine("امتیاز کلی: ${result.overallScore}/100")
        builder.appendLine("وضعیت: ${getHealthStatusText(context, result.overallStatus)}")
        builder.appendLine("تاریخ تست: ${dateFormat.format(Date(result.lastCheckTime))}")
        builder.appendLine()
        
        // جزئیات هر بررسی
        builder.appendLine("📋 جزئیات بررسی‌ها:")
        builder.appendLine("-".repeat(30))
        
        result.checks.forEach { check ->
            builder.appendLine()
            builder.appendLine("🔍 ${check.name}")
            builder.appendLine("   امتیاز: ${check.score}/100")
            builder.appendLine("   وضعیت: ${getHealthStatusText(context, check.status)}")
            builder.appendLine("   توضیحات: ${check.description}")
            check.details?.let { details ->
                builder.appendLine("   جزئیات: $details")
            }
            check.recommendation?.let { rec ->
                builder.appendLine("   💡 توصیه: $rec")
            }
        }
        
        // توصیه‌های کلی
        if (result.recommendations.isNotEmpty()) {
            builder.appendLine()
            builder.appendLine("💡 توصیه‌های کلی:")
            builder.appendLine("-".repeat(20))
            result.recommendations.forEachIndexed { index, recommendation ->
                builder.appendLine("${index + 1}. $recommendation")
            }
        }
        
        builder.appendLine()
        builder.appendLine("=".repeat(50))
        builder.appendLine("گزارش تولید شده توسط کاوش - ${dateFormat.format(Date())}")
        
        return builder.toString()
    }

    /**
     * تولید گزارش کامل امتیاز عملکرد
     */
    fun formatPerformanceScoreReport(context: Context, score: PerformanceScore): String {
        val builder = StringBuilder()
        
        // عنوان گزارش
        builder.appendLine("=".repeat(50))
        builder.appendLine(context.getString(R.string.performance_score_title))
        builder.appendLine("=".repeat(50))
        builder.appendLine()
        
        // اطلاعات کلی
        builder.appendLine("🏆 ${context.getString(R.string.performance_score_overall_score)}")
        builder.appendLine("امتیاز کلی: ${score.overallScore}/100")
        builder.appendLine("رتبه: ${getPerformanceGradeText(context, score.performanceGrade)}")
        builder.appendLine("تاریخ تست: ${dateFormat.format(Date(score.lastTestTime))}")
        builder.appendLine()
        
        // رتبه‌بندی جهانی
        score.deviceRanking?.let { ranking ->
            builder.appendLine("🌍 رتبه‌بندی جهانی:")
            builder.appendLine("   رتبه: #${ranking.globalRank} از ${ranking.totalDevices}")
            builder.appendLine("   درصدک: ${(100 - ranking.percentile).toInt()}%")
            builder.appendLine()
        }
        
        // نتایج دسته‌بندی‌ها
        builder.appendLine("📊 نتایج دسته‌بندی‌ها:")
        builder.appendLine("-".repeat(30))
        
        score.categoryScores.forEach { categoryScore ->
            builder.appendLine()
            builder.appendLine("🔧 ${getCategoryName(context, categoryScore.category)}")
            builder.appendLine("   امتیاز: ${categoryScore.score}/100")
            builder.appendLine("   رتبه: ${getPerformanceGradeText(context, categoryScore.grade)}")
            builder.appendLine("   جزئیات: ${categoryScore.details}")
        }
        
        // نتایج benchmark
        if (score.benchmarkResults.isNotEmpty()) {
            builder.appendLine()
            builder.appendLine("⚡ نتایج Benchmark:")
            builder.appendLine("-".repeat(25))
            
            score.benchmarkResults.forEach { benchmark ->
                builder.appendLine()
                builder.appendLine("🧪 ${benchmark.testName}")
                builder.appendLine("   نتیجه: ${benchmark.score} ${benchmark.unit}")
                builder.appendLine("   توضیحات: ${benchmark.description}")
                builder.appendLine("   مدت زمان: ${benchmark.duration}ms")
            }
        }
        
        builder.appendLine()
        builder.appendLine("=".repeat(50))
        builder.appendLine("گزارش تولید شده توسط کاوش - ${dateFormat.format(Date())}")
        
        return builder.toString()
    }

    /**
     * تولید گزارش کامل مقایسه دستگاه
     */
    fun formatDeviceComparisonReport(context: Context, comparison: DeviceComparison): String {
        val builder = StringBuilder()
        
        // عنوان گزارش
        builder.appendLine("=".repeat(50))
        builder.appendLine(context.getString(R.string.device_comparison_title))
        builder.appendLine("=".repeat(50))
        builder.appendLine()
        
        // اطلاعات دستگاه فعلی
        builder.appendLine("📱 دستگاه فعلی:")
        builder.appendLine("   نام: ${comparison.currentDevice.deviceName}")
        builder.appendLine("   مدل: ${comparison.currentDevice.model}")
        builder.appendLine("   برند: ${comparison.currentDevice.manufacturer}")
        builder.appendLine("   سال تولید: ${comparison.currentDevice.releaseYear ?: "نامشخص"}")
        builder.appendLine()
        
        // مقایسه کلی
        builder.appendLine("📊 مقایسه کلی:")
        builder.appendLine("   امتیاز کلی: ${comparison.currentDevice.performanceScore}/100")
        builder.appendLine("   رتبه: ${comparison.overallComparison.overallRanking}/${comparison.overallComparison.totalDevices}")
        builder.appendLine("   درصدک: ${comparison.overallComparison.percentile.toInt()}%")
        builder.appendLine()
        
        // نتایج مقایسه دسته‌بندی‌ها
        builder.appendLine("🔍 نتایج مقایسه:")
        builder.appendLine("-".repeat(25))
        
        comparison.comparisonResults.forEach { result ->
            builder.appendLine()
            builder.appendLine("⚙️ ${getCategoryName(context, result.category)}")
            builder.appendLine("   امتیاز شما: ${result.currentScore} ${result.unit}")
            builder.appendLine("   میانگین: ${result.averageScore.toInt()} ${result.unit}")
            builder.appendLine("   بهترین: ${result.bestScore.toInt()} ${result.unit}")
            builder.appendLine("   بدترین: ${result.worstScore.toInt()} ${result.unit}")
            builder.appendLine("   رتبه: ${result.ranking}/${result.totalDevices}")
            builder.appendLine("   توضیحات: ${result.description}")
        }
        
        // دستگاه‌های مقایسه شده
        if (comparison.comparedDevices.isNotEmpty()) {
            builder.appendLine()
            builder.appendLine("📋 دستگاه‌های مقایسه شده:")
            builder.appendLine("-".repeat(30))
            
            comparison.comparedDevices.forEachIndexed { index, device ->
                builder.appendLine("${index + 1}. ${device.manufacturer} ${device.deviceName} (${device.releaseYear ?: "نامشخص"})")
            }
        }
        
        builder.appendLine()
        builder.appendLine("=".repeat(50))
        builder.appendLine("گزارش تولید شده توسط کاوش - ${dateFormat.format(Date())}")
        
        return builder.toString()
    }

    /**
     * تولید گزارش JSON برای بررسی سلامت
     */
    fun formatHealthCheckJsonReport(result: HealthCheckResult): String {
        val jsonObject = buildJsonObject {
            put("report_type", "health_check")
            put("timestamp", System.currentTimeMillis())
            put("overall_score", result.overallScore)
            put("overall_status", result.overallStatus.name)
            put("test_date", result.lastCheckTime)
            
            put("checks", buildJsonObject {
                result.checks.forEach { check ->
                    put(check.category.name.lowercase(), buildJsonObject {
                        put("name", check.name)
                        put("score", check.score)
                        put("status", check.status.name)
                        put("description", check.description)
                        check.details?.let { put("details", it) }
                        check.recommendation?.let { put("recommendation", it) }
                    })
                }
            })
            
            put("recommendations", buildJsonObject {
                result.recommendations.forEachIndexed { index, rec ->
                    put("recommendation_${index + 1}", rec)
                }
            })
        }
        
        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), jsonObject)
    }

    /**
     * تولید گزارش JSON برای امتیاز عملکرد
     */
    fun formatPerformanceScoreJsonReport(score: PerformanceScore): String {
        val jsonObject = buildJsonObject {
            put("report_type", "performance_score")
            put("timestamp", System.currentTimeMillis())
            put("overall_score", score.overallScore)
            put("performance_grade", score.performanceGrade.name)
            put("test_date", score.lastTestTime)
            
            score.deviceRanking?.let { ranking ->
                put("device_ranking", buildJsonObject {
                    put("global_rank", ranking.globalRank)
                    put("total_devices", ranking.totalDevices)
                    put("percentile", ranking.percentile)
                })
            }
            
            put("category_scores", buildJsonObject {
                score.categoryScores.forEach { categoryScore ->
                    put(categoryScore.category.name.lowercase(), buildJsonObject {
                        put("score", categoryScore.score)
                        put("grade", categoryScore.grade.name)
                        put("details", categoryScore.details)
                    })
                }
            })
            
            put("benchmark_results", buildJsonObject {
                score.benchmarkResults.forEachIndexed { index, benchmark ->
                    put("benchmark_$index", buildJsonObject {
                        put("test_name", benchmark.testName)
                        put("score", benchmark.score)
                        put("unit", benchmark.unit)
                        put("description", benchmark.description)
                        put("duration", benchmark.duration)
                    })
                }
            })
        }
        
        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), jsonObject)
    }

    /**
     * تولید گزارش JSON برای مقایسه دستگاه
     */
    fun formatDeviceComparisonJsonReport(comparison: DeviceComparison): String {
        val jsonObject = buildJsonObject {
            put("report_type", "device_comparison")
            put("timestamp", System.currentTimeMillis())
            
            put("current_device", buildJsonObject {
                put("name", comparison.currentDevice.deviceName)
                put("model", comparison.currentDevice.model)
                put("brand", comparison.currentDevice.manufacturer)
                put("release_year", comparison.currentDevice.releaseYear ?: 0)
            })
            
            put("overall_comparison", buildJsonObject {
                put("current_score", comparison.currentDevice.performanceScore)
                put("ranking", comparison.overallComparison.overallRanking)
                put("total_devices", comparison.overallComparison.totalDevices)
                put("percentile", comparison.overallComparison.percentile)
            })
            
            put("comparison_results", buildJsonObject {
                comparison.comparisonResults.forEach { result ->
                    put(result.category.name.lowercase(), buildJsonObject {
                        put("current_score", result.currentScore)
                        put("average_score", result.averageScore)
                        put("best_score", result.bestScore)
                        put("worst_score", result.worstScore)
                        put("ranking", result.ranking)
                        put("total_devices", result.totalDevices)
                        put("unit", result.unit)
                        put("description", result.description)
                    })
                }
            })
            
            put("compared_devices", buildJsonObject {
                comparison.comparedDevices.forEachIndexed { index, device ->
                    put("device_$index", buildJsonObject {
                        put("name", device.deviceName)
                        put("brand", device.manufacturer)
                        put("release_year", device.releaseYear ?: 0)
                    })
                }
            })
        }
        
        return Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), jsonObject)
    }

    // توابع کمکی
    private fun getHealthStatusText(context: Context, status: HealthStatus): String = when (status) {
        HealthStatus.EXCELLENT -> context.getString(R.string.health_status_excellent)
        HealthStatus.GOOD -> context.getString(R.string.health_status_good)
        HealthStatus.FAIR -> context.getString(R.string.health_status_fair)
        HealthStatus.POOR -> context.getString(R.string.health_status_poor)
        HealthStatus.CRITICAL -> context.getString(R.string.health_status_critical)
    }

    private fun getPerformanceGradeText(context: Context, grade: PerformanceGrade): String = when (grade) {
        PerformanceGrade.S_PLUS -> context.getString(R.string.performance_grade_s_plus)
        PerformanceGrade.S -> context.getString(R.string.performance_grade_s)
        PerformanceGrade.A_PLUS -> context.getString(R.string.performance_grade_a_plus)
        PerformanceGrade.A -> context.getString(R.string.performance_grade_a)
        PerformanceGrade.B_PLUS -> context.getString(R.string.performance_grade_b_plus)
        PerformanceGrade.B -> context.getString(R.string.performance_grade_b)
        PerformanceGrade.C_PLUS -> context.getString(R.string.performance_grade_c_plus)
        PerformanceGrade.C -> context.getString(R.string.performance_grade_c)
        PerformanceGrade.D -> context.getString(R.string.performance_grade_d)
        PerformanceGrade.F -> context.getString(R.string.performance_grade_f)
    }

    private fun getCategoryName(context: Context, category: PerformanceCategory): String = when (category) {
        PerformanceCategory.CPU -> context.getString(R.string.performance_category_cpu)
        PerformanceCategory.GPU -> context.getString(R.string.performance_category_gpu)
        PerformanceCategory.RAM -> context.getString(R.string.performance_category_ram)
        PerformanceCategory.STORAGE -> context.getString(R.string.performance_category_storage)
        PerformanceCategory.NETWORK -> context.getString(R.string.performance_category_network)
        PerformanceCategory.BATTERY -> context.getString(R.string.performance_category_battery)
        PerformanceCategory.THERMAL -> context.getString(R.string.performance_category_thermal)
    }

    private fun getCategoryName(context: Context, category: ComparisonCategory): String = when (category) {
        ComparisonCategory.CPU_PERFORMANCE -> context.getString(R.string.performance_category_cpu)
        ComparisonCategory.GPU_PERFORMANCE -> context.getString(R.string.performance_category_gpu)
        ComparisonCategory.RAM_SIZE -> context.getString(R.string.performance_category_ram)
        ComparisonCategory.STORAGE_SIZE -> context.getString(R.string.performance_category_storage)
        ComparisonCategory.STORAGE_SPEED -> "سرعت حافظه"
        ComparisonCategory.DISPLAY_QUALITY -> "کیفیت نمایشگر"
        ComparisonCategory.BATTERY_CAPACITY -> context.getString(R.string.performance_category_battery)
        ComparisonCategory.CAMERA_QUALITY -> "کیفیت دوربین"
        ComparisonCategory.OVERALL_PERFORMANCE -> "عملکرد کلی"
        ComparisonCategory.PRICE_PERFORMANCE -> "ارزش قیمت"
    }
}
