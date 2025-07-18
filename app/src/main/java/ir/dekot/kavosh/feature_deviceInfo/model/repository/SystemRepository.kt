package ir.dekot.kavosh.feature_deviceInfo.model.repository

import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.feature_deviceInfo.model.SystemDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن اطلاعات سیستم - مسئول مدیریت اطلاعات مربوط به سیستم‌عامل و برنامه
 * شامل اطلاعات سیستم‌عامل، نسخه برنامه و سایر اطلاعات سیستمی
 */
@Singleton
class SystemRepository @Inject constructor(
    private val systemDataSource: SystemDataSource
) {

    /**
     * دریافت اطلاعات سیستم‌عامل
     * @return اطلاعات کامل سیستم شامل نسخه اندروید، مدل دستگاه و سازنده
     */
    fun getSystemInfo(): SystemInfo = systemDataSource.getSystemInfo()

    /**
     * دریافت نسخه برنامه
     * @return رشته نسخه برنامه فعلی
     */
    fun getAppVersion(): String = systemDataSource.getAppVersion()
}
