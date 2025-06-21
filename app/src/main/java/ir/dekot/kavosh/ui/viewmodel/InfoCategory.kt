package ir.dekot.kavosh.ui.viewmodel

/**
 * دسته‌بندی‌های اطلاعات
 * عنوان فارسی هر دسته به عنوان یک خصوصیت (property) به خود آن اضافه شده است.
 */
enum class InfoCategory(val title: String) {
    SOC("پردازنده مرکزی و حافظه"),
    DEVICE("مشخصات دستگاه"),
    SYSTEM("سیستم عامل"),
    BATTERY("باتری"),
    SENSORS("سنسورها"),
    THERMAL("دما (Thermal)"),
    NETWORK("اطلاعات شبکه"),
    CAMERA("اطلاعات دوربین")
}