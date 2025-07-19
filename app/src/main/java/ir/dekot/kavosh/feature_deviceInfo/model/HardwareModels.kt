package ir.dekot.kavosh.feature_deviceInfo.model

/**
 * فایل مدل‌های سخت‌افزار
 * شامل تمام کلاس‌های داده مربوط به اجزای سخت‌افزاری دستگاه
 * CPU، GPU، RAM، Storage، Display و اطلاعات حرارتی
 */

// Re-export hardware-related data classes for better organization
// These classes are defined in their individual files but grouped here for convenience

// CPU Models - defined in CpuInfo.kt
// data class CpuInfo(...)

// GPU Models - defined in GpuInfo.kt  
// data class GpuInfo(...)

// Memory Models - defined in RamInfo.kt and StorageInfo.kt
// data class RamInfo(...)
// data class StorageInfo(...)

// Display Models - defined in DisplayInfo.kt
// data class DisplayInfo(...)

// Thermal Models - defined in ThermalInfo.kt
// data class ThermalInfo(...)

// Battery Models - defined in BatteryInfo.kt
// data class BatteryInfo(...)

/**
 * این فایل به عنوان یک نقطه مرجع برای تمام مدل‌های سخت‌افزاری عمل می‌کند.
 * کلاس‌های داده واقعی در فایل‌های جداگانه تعریف شده‌اند:
 * 
 * - CpuInfo.kt: اطلاعات پردازنده
 * - GpuInfo.kt: اطلاعات کارت گرافیک
 * - RamInfo.kt: اطلاعات حافظه RAM
 * - StorageInfo.kt: اطلاعات ذخیره‌سازی
 * - DisplayInfo.kt: اطلاعات نمایشگر
 * - ThermalInfo.kt: اطلاعات حرارتی
 * - BatteryInfo.kt: اطلاعات باتری
 */
