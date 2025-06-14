package ir.dekot.kavosh.data.source

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.data.model.components.CameraInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class CameraDataSource @Inject constructor(@ApplicationContext private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getCameraInfoList(): List<CameraInfo> {
        val cameraList = mutableListOf<CameraInfo>()

        try {
            val cameraIds = cameraManager.cameraIdList
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)

                // نام دوربین (پشتی، جلویی و...)
                val facing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "دوربین سلفی (جلویی)"
                    CameraCharacteristics.LENS_FACING_BACK -> "دوربین اصلی (پشتی)"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "دوربین خارجی"
                    else -> "دوربین نامشخص"
                }

                // اطلاعات مربوط به رزولوشن و مگاپیکسل
                val streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val highResSize = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width * it.height }
                val megapixels = if (highResSize != null) {
                    ((highResSize.width * highResSize.height) / 1_000_000.0).roundToInt()
                } else {
                    0
                }
                val maxResolution = if (highResSize != null) "${highResSize.width}x${highResSize.height}" else "N/A"

                // بررسی وجود فلش
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                // دیافراگم‌ها و فواصل کانونی
                val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.joinToString(", ") ?: "N/A"
                val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.joinToString(", ") { "%.2fmm".format(it) } ?: "N/A"

                // اندازه سنسور
                val sensorSize =
                    characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.let { "%.2f x %.2f mm".format(it.width, it.height) } ?: "N/A"

                cameraList.add(
                    CameraInfo(
                        id = id,
                        name = "$facing (ID: $id)",
                        megapixels = "$megapixels MP",
                        maxResolution = maxResolution,
                        hasFlash = hasFlash,
                        apertures = "f/$apertures",
                        focalLengths = focalLengths,
                        sensorSize = sensorSize
                    )
                )
            }
        } catch (e: Exception) {
            // در صورت بروز خطا (مثلاً نبود مجوز)، لیست خالی برمی‌گردانیم
            e.printStackTrace()
        }
        return cameraList
    }
}