package ir.dekot.kavosh.ui.viewmodel

// تعریف صفحات مختلف برنامه برای ناوبری
sealed class Screen {
    object Splash : Screen()
    object Dashboard : Screen()
    object Settings : Screen()
    object EditDashboard : Screen() // <-- صفحه جدید برای ویرایش داشبورد
    data class Detail(val category: InfoCategory) : Screen()
}