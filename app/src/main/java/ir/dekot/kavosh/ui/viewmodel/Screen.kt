package ir.dekot.kavosh.ui.viewmodel

sealed class Screen {
    object Splash : Screen()
    object Dashboard : Screen()
    object Settings : Screen()
    object EditDashboard : Screen()
    object About : Screen()
    data class Detail(val category: InfoCategory) : Screen()
    // *** صفحه جدید برای جزئیات سنسور ***
    data class SensorDetail(val sensorType: Int) : Screen()
}