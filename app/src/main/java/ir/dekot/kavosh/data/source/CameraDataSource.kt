package ir.dekot.kavosh.data.source

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.dekot.kavosh.R
import ir.dekot.kavosh.data.model.components.CameraInfo
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class CameraDataSource @Inject constructor(@param:ApplicationContext private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getCameraInfoList(): List<CameraInfo> {
        val cameraList = mutableListOf<CameraInfo>()

        try {
            val cameraIds = cameraManager.cameraIdList
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)

                val facing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> context.getString(R.string.camera_facing_front)
                    CameraCharacteristics.LENS_FACING_BACK -> context.getString(R.string.camera_facing_back)
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> context.getString(R.string.camera_facing_external)
                    else -> context.getString(R.string.camera_facing_unknown)
                }

                val streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val highResSize = streamConfigurationMap?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width * it.height }
                val megapixels = if (highResSize != null) {
                    ((highResSize.width * highResSize.height) / 1_000_000.0).roundToInt()
                } else {
                    0
                }
                val maxResolution = highResSize?.let { "${it.width}x${it.height}" } ?: context.getString(R.string.label_not_available)

                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                val apertures = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                    ?.joinToString(", ") { "f/$it" } ?: context.getString(R.string.label_not_available)
                val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.joinToString(", ") { context.getString(R.string.unit_format_mm, it) } ?: context.getString(R.string.label_not_available)

                val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                    ?.let { context.getString(R.string.unit_format_mm_area, it.width, it.height) } ?: context.getString(R.string.label_not_available)

                cameraList.add(
                    CameraInfo(
                        id = id,
                        name = "$facing (ID: $id)",
                        // *** تغییر کلیدی: استفاده از منبع رشته برای مگاپیکسل ***
                        megapixels = context.getString(R.string.unit_format_mp, megapixels),
                        maxResolution = maxResolution,
                        hasFlash = hasFlash,
                        apertures = apertures,
                        focalLengths = focalLengths,
                        sensorSize = sensorSize
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cameraList
    }
}