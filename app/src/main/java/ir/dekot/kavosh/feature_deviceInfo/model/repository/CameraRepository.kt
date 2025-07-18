package ir.dekot.kavosh.feature_deviceInfo.model.repository

import ir.dekot.kavosh.feature_deviceInfo.model.CameraDataSource
import ir.dekot.kavosh.feature_deviceInfo.model.CameraInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مخزن اطلاعات دوربین - مسئول مدیریت اطلاعات مربوط به دوربین‌های دستگاه
 * شامل مشخصات دوربین‌ها، رزولوشن و قابلیت‌های پشتیبانی شده
 */
@Singleton
class CameraRepository @Inject constructor(
    private val cameraDataSource: CameraDataSource
) {

    /**
     * دریافت لیست اطلاعات دوربین‌ها
     * @return لیست کامل اطلاعات دوربین‌های موجود در دستگاه
     */
    fun getCameraInfoList(): List<CameraInfo> = cameraDataSource.getCameraInfoList()
}
