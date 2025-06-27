package ir.dekot.kavosh.data.model.components

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable /**
 * مدل داده برای نگهداری مشخصات یک دوربین.
 */
data class CameraInfo(
    val id: String,
    val name: String, // e.g., "دوربین اصلی (پشتی)"
    val megapixels: String,
    val maxResolution: String,
    val hasFlash: Boolean,
    val apertures: String,
    val focalLengths: String,
    val sensorSize: String
)