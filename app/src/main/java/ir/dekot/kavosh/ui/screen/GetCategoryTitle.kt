package ir.dekot.kavosh.ui.screen

import ir.dekot.kavosh.ui.viewmodel.InfoCategory

fun getCategoryTitle(category: InfoCategory): String {
    return when (category) {
        InfoCategory.SOC -> "پردازنده مرکزی و حافظه"
        InfoCategory.DEVICE -> "مشخصات دستگاه"
        InfoCategory.SYSTEM -> "سیستم عامل"
        InfoCategory.BATTERY -> "باتری"
        InfoCategory.SENSORS -> "سنسورها"
        InfoCategory.THERMAL -> "دما (Thermal)" // <-- این خط را اضافه کنید
        InfoCategory.NETWORK -> "اطلاعات شبکه" // <-- عنوان جدید
        InfoCategory.CAMERA -> "اطلاعات دوربین" // <-- عنوان جدید
    }
}