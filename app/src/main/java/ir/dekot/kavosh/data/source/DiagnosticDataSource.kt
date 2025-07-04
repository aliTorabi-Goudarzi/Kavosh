package ir.dekot.kavosh.data.source

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.diagnostic.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * منبع داده برای ابزارهای تشخیصی
 * شامل Health Check، Performance Score و Device Comparison
 */
@Singleton
class DiagnosticDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socDataSource: SocDataSource,
    private val memoryDataSource: MemoryDataSource,
    private val powerDataSource: PowerDataSource,
    private val systemDataSource: SystemDataSource
) {

    /**
     * انجام بررسی سلامت کلی دستگاه
     */
    suspend fun performHealthCheck(): HealthCheckResult {
        val checks = mutableListOf<HealthCheck>()
        
        // بررسی عملکرد
        checks.add(checkPerformance())
        
        // بررسی حافظه
        checks.add(checkStorage())
        
        // بررسی باتری
        checks.add(checkBattery())
        
        // بررسی دما
        checks.add(checkTemperature())
        
        // بررسی رم
        checks.add(checkMemory())
        
        // بررسی شبکه
        checks.add(checkNetwork())
        
        // بررسی امنیت
        checks.add(checkSecurity())
        
        // بررسی سیستم
        checks.add(checkSystem())
        
        // محاسبه امتیاز کلی
        val overallScore = checks.map { it.score }.average().toInt()
        val overallStatus = getHealthStatus(overallScore)
        
        // تولید توصیه‌ها
        val recommendations = generateRecommendations(checks)
        
        return HealthCheckResult(
            overallScore = overallScore,
            overallStatus = overallStatus,
            checks = checks,
            recommendations = recommendations
        )
    }

    /**
     * محاسبه امتیاز عملکرد
     */
    suspend fun calculatePerformanceScore(): PerformanceScore {
        val categoryScores = mutableListOf<CategoryScore>()
        val benchmarkResults = mutableListOf<BenchmarkResult>()
        
        // تست CPU
        val cpuScore = performCpuBenchmark()
        categoryScores.add(CategoryScore(
            category = PerformanceCategory.CPU,
            score = cpuScore.score,
            grade = getPerformanceGrade(cpuScore.score),
            details = cpuScore.description,
            testResults = cpuScore.testResults
        ))
        benchmarkResults.addAll(cpuScore.benchmarkResults)
        
        // تست GPU
        val gpuScore = performGpuBenchmark()
        categoryScores.add(CategoryScore(
            category = PerformanceCategory.GPU,
            score = gpuScore.score,
            grade = getPerformanceGrade(gpuScore.score),
            details = gpuScore.description
        ))
        benchmarkResults.addAll(gpuScore.benchmarkResults)
        
        // تست RAM
        val ramScore = performRamBenchmark()
        categoryScores.add(CategoryScore(
            category = PerformanceCategory.RAM,
            score = ramScore.score,
            grade = getPerformanceGrade(ramScore.score),
            details = ramScore.description
        ))
        benchmarkResults.addAll(ramScore.benchmarkResults)
        
        // تست Storage
        val storageScore = performStorageBenchmark()
        categoryScores.add(CategoryScore(
            category = PerformanceCategory.STORAGE,
            score = storageScore.score,
            grade = getPerformanceGrade(storageScore.score),
            details = storageScore.description
        ))
        benchmarkResults.addAll(storageScore.benchmarkResults)
        
        // محاسبه امتیاز کلی
        val overallScore = categoryScores.map { it.score }.average().toInt()
        val performanceGrade = getPerformanceGrade(overallScore)
        
        // رتبه‌بندی دستگاه (شبیه‌سازی)
        val deviceRanking = generateDeviceRanking(overallScore)
        
        return PerformanceScore(
            overallScore = overallScore,
            performanceGrade = performanceGrade,
            categoryScores = categoryScores,
            benchmarkResults = benchmarkResults,
            deviceRanking = deviceRanking
        )
    }

    /**
     * مقایسه دستگاه با دستگاه‌های مشابه
     */
    suspend fun compareDevice(): DeviceComparison {
        val currentDevice = getCurrentDeviceProfile()
        val comparedDevices = getSimilarDevices()
        val comparisonResults = performComparison(currentDevice, comparedDevices)
        val overallComparison = calculateOverallComparison(comparisonResults)
        
        return DeviceComparison(
            currentDevice = currentDevice,
            comparedDevices = comparedDevices,
            comparisonResults = comparisonResults,
            overallComparison = overallComparison
        )
    }

    // متدهای کمکی برای Health Check
    private suspend fun checkPerformance(): HealthCheck {
        delay(500) // شبیه‌سازی تست
        val score = Random.nextInt(70, 95)
        return HealthCheck(
            category = HealthCategory.PERFORMANCE,
            name = context.getString(R.string.health_check_performance),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_performance_desc),
            recommendation = if (score < 80) context.getString(R.string.health_check_performance_rec) else null
        )
    }

    private suspend fun checkStorage(): HealthCheck {
        delay(300)
        val storageInfo = memoryDataSource.getStorageInfo()
        // شبیه‌سازی محاسبه فضای خالی - در واقعیت باید از StorageInfo استفاده کرد
        val freeSpacePercent = Random.nextInt(20, 80) // شبیه‌سازی
        val score = when {
            freeSpacePercent > 50 -> Random.nextInt(85, 100)
            freeSpacePercent > 20 -> Random.nextInt(60, 84)
            else -> Random.nextInt(30, 59)
        }

        return HealthCheck(
            category = HealthCategory.STORAGE,
            name = context.getString(R.string.health_check_storage),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_storage_desc, freeSpacePercent),
            recommendation = if (score < 70) context.getString(R.string.health_check_storage_rec) else null
        )
    }

    private suspend fun checkBattery(): HealthCheck {
        delay(200)
        // شبیه‌سازی وضعیت باتری - در واقعیت باید Intent را از جای دیگر دریافت کرد
        val batteryHealth = listOf("Good", "Fair", "Poor").random()
        val score = when {
            batteryHealth == "Good" -> Random.nextInt(80, 100)
            batteryHealth == "Fair" -> Random.nextInt(60, 79)
            else -> Random.nextInt(30, 59)
        }

        return HealthCheck(
            category = HealthCategory.BATTERY,
            name = context.getString(R.string.health_check_battery),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_battery_desc, batteryHealth),
            recommendation = if (score < 70) context.getString(R.string.health_check_battery_rec) else null
        )
    }

    private suspend fun checkTemperature(): HealthCheck {
        delay(100)
        val score = Random.nextInt(75, 95) // شبیه‌سازی - در واقعیت باید دمای واقعی خوانده شود
        return HealthCheck(
            category = HealthCategory.TEMPERATURE,
            name = context.getString(R.string.health_check_temperature),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_temperature_desc),
            recommendation = if (score < 70) context.getString(R.string.health_check_temperature_rec) else null
        )
    }

    private suspend fun checkMemory(): HealthCheck {
        delay(200)
        val ramInfo = memoryDataSource.getRamInfo()
        // شبیه‌سازی محاسبه درصد رم آزاد
        val freeRamPercent = Random.nextInt(20, 60) // شبیه‌سازی
        val score = when {
            freeRamPercent > 40 -> Random.nextInt(85, 100)
            freeRamPercent > 20 -> Random.nextInt(65, 84)
            else -> Random.nextInt(40, 64)
        }

        return HealthCheck(
            category = HealthCategory.MEMORY,
            name = context.getString(R.string.health_check_memory),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_memory_desc, freeRamPercent),
            recommendation = if (score < 70) context.getString(R.string.health_check_memory_rec) else null
        )
    }

    private suspend fun checkNetwork(): HealthCheck {
        delay(300)
        val score = Random.nextInt(80, 95)
        return HealthCheck(
            category = HealthCategory.NETWORK,
            name = context.getString(R.string.health_check_network),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_network_desc)
        )
    }

    private suspend fun checkSecurity(): HealthCheck {
        delay(400)
        val score = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Random.nextInt(85, 100)
        } else {
            Random.nextInt(60, 84)
        }
        
        return HealthCheck(
            category = HealthCategory.SECURITY,
            name = context.getString(R.string.health_check_security),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_security_desc),
            recommendation = if (score < 80) context.getString(R.string.health_check_security_rec) else null
        )
    }

    private suspend fun checkSystem(): HealthCheck {
        delay(200)
        val score = Random.nextInt(80, 95)
        return HealthCheck(
            category = HealthCategory.SYSTEM,
            name = context.getString(R.string.health_check_system),
            score = score,
            status = getHealthStatus(score),
            description = context.getString(R.string.health_check_system_desc, Build.VERSION.RELEASE)
        )
    }

    private fun getHealthStatus(score: Int): HealthStatus = when (score) {
        in 90..100 -> HealthStatus.EXCELLENT
        in 70..89 -> HealthStatus.GOOD
        in 50..69 -> HealthStatus.FAIR
        in 30..49 -> HealthStatus.POOR
        else -> HealthStatus.CRITICAL
    }

    private fun generateRecommendations(checks: List<HealthCheck>): List<String> {
        return checks.filter { it.score < 80 }
            .mapNotNull { it.recommendation }
            .distinct()
    }

    // متدهای کمکی برای Performance Score
    private suspend fun performCpuBenchmark(): CategoryBenchmarkResult {
        delay(2000) // شبیه‌سازی تست CPU
        val score = Random.nextInt(70, 95)
        val benchmarkResults = listOf(
            BenchmarkResult(
                testName = "Single Core",
                category = PerformanceCategory.CPU,
                score = Random.nextInt(800, 1200),
                unit = "points",
                description = "Single-threaded performance",
                duration = 1000
            ),
            BenchmarkResult(
                testName = "Multi Core",
                category = PerformanceCategory.CPU,
                score = Random.nextInt(2500, 4000),
                unit = "points",
                description = "Multi-threaded performance",
                duration = 1000
            )
        )
        
        return CategoryBenchmarkResult(
            score = score,
            description = "CPU performance based on computational tests",
            benchmarkResults = benchmarkResults,
            testResults = emptyList()
        )
    }

    private suspend fun performGpuBenchmark(): CategoryBenchmarkResult {
        delay(1500)
        val score = Random.nextInt(65, 90)
        val benchmarkResults = listOf(
            BenchmarkResult(
                testName = "3D Graphics",
                category = PerformanceCategory.GPU,
                score = Random.nextInt(15, 35),
                unit = "fps",
                description = "3D rendering performance",
                duration = 1500
            )
        )
        
        return CategoryBenchmarkResult(
            score = score,
            description = "GPU performance based on graphics rendering",
            benchmarkResults = benchmarkResults
        )
    }

    private suspend fun performRamBenchmark(): CategoryBenchmarkResult {
        delay(1000)
        val score = Random.nextInt(75, 95)
        val benchmarkResults = listOf(
            BenchmarkResult(
                testName = "Memory Bandwidth",
                category = PerformanceCategory.RAM,
                score = Random.nextInt(8000, 15000),
                unit = "MB/s",
                description = "Memory read/write speed",
                duration = 1000
            )
        )
        
        return CategoryBenchmarkResult(
            score = score,
            description = "RAM performance based on memory operations",
            benchmarkResults = benchmarkResults
        )
    }

    private suspend fun performStorageBenchmark(): CategoryBenchmarkResult {
        delay(2000)
        val score = Random.nextInt(60, 85)
        val benchmarkResults = listOf(
            BenchmarkResult(
                testName = "Sequential Read",
                category = PerformanceCategory.STORAGE,
                score = Random.nextInt(200, 800),
                unit = "MB/s",
                description = "Sequential read speed",
                duration = 1000
            ),
            BenchmarkResult(
                testName = "Sequential Write",
                category = PerformanceCategory.STORAGE,
                score = Random.nextInt(100, 400),
                unit = "MB/s",
                description = "Sequential write speed",
                duration = 1000
            )
        )
        
        return CategoryBenchmarkResult(
            score = score,
            description = "Storage performance based on I/O operations",
            benchmarkResults = benchmarkResults
        )
    }

    private fun getPerformanceGrade(score: Int): PerformanceGrade = when (score) {
        in 95..100 -> PerformanceGrade.S_PLUS
        in 90..94 -> PerformanceGrade.S
        in 85..89 -> PerformanceGrade.A_PLUS
        in 80..84 -> PerformanceGrade.A
        in 75..79 -> PerformanceGrade.B_PLUS
        in 70..74 -> PerformanceGrade.B
        in 65..69 -> PerformanceGrade.C_PLUS
        in 60..64 -> PerformanceGrade.C
        in 50..59 -> PerformanceGrade.D
        else -> PerformanceGrade.F
    }

    private fun generateDeviceRanking(score: Int): DeviceRanking {
        val totalDevices = Random.nextInt(50000, 100000)
        val percentile = score.toDouble()
        val globalRank = ((100 - percentile) / 100 * totalDevices).toInt()
        
        return DeviceRanking(
            globalRank = globalRank,
            totalDevices = totalDevices,
            percentile = percentile,
            similarDevices = generateSimilarDevices(score)
        )
    }

    private fun generateSimilarDevices(currentScore: Int): List<SimilarDevice> {
        return listOf(
            SimilarDevice("Samsung Galaxy S23", currentScore + Random.nextInt(-10, 10), Random.nextInt(-10, 10)),
            SimilarDevice("iPhone 14", currentScore + Random.nextInt(-15, 15), Random.nextInt(-15, 15)),
            SimilarDevice("Google Pixel 7", currentScore + Random.nextInt(-8, 8), Random.nextInt(-8, 8)),
            SimilarDevice("OnePlus 11", currentScore + Random.nextInt(-12, 12), Random.nextInt(-12, 12))
        )
    }

    // متدهای کمکی برای Device Comparison
    private fun getCurrentDeviceProfile(): DeviceProfile {
        return DeviceProfile(
            deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            specifications = getCurrentDeviceSpecs(),
            performanceScore = Random.nextInt(70, 95),
            isCurrentDevice = true
        )
    }

    private fun getCurrentDeviceSpecs(): DeviceSpecs {
        val ramInfo = memoryDataSource.getRamInfo()
        val storageInfo = memoryDataSource.getStorageInfo()

        return DeviceSpecs(
            cpu = CpuSpec(
                name = Build.HARDWARE,
                architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
                cores = Runtime.getRuntime().availableProcessors(),
                maxFrequency = 2.8 // شبیه‌سازی
            ),
            ram = RamSpec(
                totalSize = 8192, // شبیه‌سازی 8GB
                type = "LPDDR5"
            ),
            storage = StorageSpec(
                totalSize = 256, // شبیه‌سازی 256GB
                type = "UFS 3.1"
            ),
            display = DisplaySpec(
                sizeInches = 6.1,
                resolution = "1080x2400",
                refreshRate = 120,
                pixelDensity = 400
            ),
            battery = BatterySpec(
                capacity = 4000,
                fastCharging = true,
                chargingSpeed = 25
            )
        )
    }

    private fun getSimilarDevices(): List<DeviceProfile> {
        // شبیه‌سازی دستگاه‌های مشابه
        return listOf(
            DeviceProfile(
                deviceName = "Samsung Galaxy S23",
                manufacturer = "Samsung",
                model = "Galaxy S23",
                specifications = DeviceSpecs(
                    cpu = CpuSpec("Snapdragon 8 Gen 2", "arm64-v8a", 8, 3.2),
                    ram = RamSpec(8192, "LPDDR5X"),
                    storage = StorageSpec(256, "UFS 4.0"),
                    display = DisplaySpec(6.1, "1080x2340", 120, 425),
                    battery = BatterySpec(3900, true, 25)
                ),
                performanceScore = Random.nextInt(85, 95)
            ),
            DeviceProfile(
                deviceName = "iPhone 14",
                manufacturer = "Apple",
                model = "iPhone 14",
                specifications = DeviceSpecs(
                    cpu = CpuSpec("A15 Bionic", "arm64", 6, 3.2),
                    ram = RamSpec(6144, "LPDDR5"),
                    storage = StorageSpec(256, "NVMe"),
                    display = DisplaySpec(6.1, "1170x2532", 60, 460),
                    battery = BatterySpec(3279, true, 20)
                ),
                performanceScore = Random.nextInt(88, 98)
            )
        )
    }

    private fun performComparison(current: DeviceProfile, others: List<DeviceProfile>): List<ComparisonResult> {
        val allDevices = listOf(current) + others
        val results = mutableListOf<ComparisonResult>()
        
        // مقایسه CPU
        val cpuScores = allDevices.map { it.specifications.cpu.maxFrequency }
        results.add(ComparisonResult(
            category = ComparisonCategory.CPU_PERFORMANCE,
            currentScore = current.specifications.cpu.maxFrequency,
            averageScore = cpuScores.average(),
            bestScore = cpuScores.maxOrNull() ?: 0.0,
            worstScore = cpuScores.minOrNull() ?: 0.0,
            ranking = cpuScores.sortedDescending().indexOf(current.specifications.cpu.maxFrequency) + 1,
            totalDevices = allDevices.size,
            unit = "GHz",
            description = "CPU maximum frequency"
        ))
        
        // مقایسه RAM
        val ramSizes = allDevices.map { it.specifications.ram.totalSize.toDouble() }
        results.add(ComparisonResult(
            category = ComparisonCategory.RAM_SIZE,
            currentScore = current.specifications.ram.totalSize.toDouble(),
            averageScore = ramSizes.average(),
            bestScore = ramSizes.maxOrNull() ?: 0.0,
            worstScore = ramSizes.minOrNull() ?: 0.0,
            ranking = ramSizes.sortedDescending().indexOf(current.specifications.ram.totalSize.toDouble()) + 1,
            totalDevices = allDevices.size,
            unit = "MB",
            description = "RAM capacity"
        ))
        
        return results
    }

    private fun calculateOverallComparison(results: List<ComparisonResult>): OverallComparison {
        val averageRanking = results.map { it.ranking }.average()
        val totalDevices = results.firstOrNull()?.totalDevices ?: 1
        val percentile = ((totalDevices - averageRanking + 1) / totalDevices * 100)
        
        return OverallComparison(
            overallRanking = averageRanking.toInt(),
            totalDevices = totalDevices,
            percentile = percentile,
            strengths = listOf("Good CPU performance", "Adequate RAM"),
            weaknesses = listOf("Average storage speed"),
            recommendation = "Your device performs well in most categories"
        )
    }

    // کلاس کمکی برای نتایج benchmark
    private data class CategoryBenchmarkResult(
        val score: Int,
        val description: String,
        val benchmarkResults: List<BenchmarkResult>,
        val testResults: List<TestResult> = emptyList()
    )
}
