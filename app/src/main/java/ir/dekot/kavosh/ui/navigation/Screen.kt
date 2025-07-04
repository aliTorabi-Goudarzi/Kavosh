package ir.dekot.kavosh.ui.navigation // <-- تغییر پکیج

import ir.dekot.kavosh.ui.viewmodel.InfoCategory

sealed class Screen {
    data object Splash : Screen()
    data object Dashboard : Screen()
    data object Settings : Screen()
    data object EditDashboard : Screen()
    data object About : Screen()
    data class Detail(val category: InfoCategory) : Screen()
    data class SensorDetail(val sensorType: Int) : Screen()

    data object CpuStressTest : Screen() // <-- صفحه جدید

    data object NetworkTools : Screen() // <-- صفحه جدید

    data object DisplayTest : Screen() // <-- صفحه جدید

    data object StorageTest : Screen() // <-- صفحه تست سرعت حافظه

    // صفحات ابزارهای تشخیصی جدید
    data object HealthCheck : Screen() // <-- صفحه بررسی سلامت
    data object PerformanceScore : Screen() // <-- صفحه امتیاز عملکرد
    data object DeviceComparison : Screen() // <-- صفحه مقایسه دستگاه
}