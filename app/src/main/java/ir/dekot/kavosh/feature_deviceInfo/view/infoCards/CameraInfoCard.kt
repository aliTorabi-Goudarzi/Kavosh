package ir.dekot.kavosh.feature_deviceInfo.view.infoCards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ir.dekot.kavosh.R
import ir.dekot.kavosh.feature_deviceInfo.view.InfoCard
import ir.dekot.kavosh.feature_deviceInfo.view.InfoRow
import ir.dekot.kavosh.feature_deviceInfo.model.CameraInfo

@Composable
fun CameraInfoCard(info: CameraInfo) {
    InfoCard(title = info.name) {
        InfoRow(stringResource(R.string.camera_megapixels), info.megapixels)
        InfoRow(stringResource(R.string.camera_max_resolution), info.maxResolution)
        InfoRow(
            stringResource(R.string.camera_flash_support),
            stringResource(if (info.hasFlash) R.string.label_yes else R.string.label_no)
        )
        InfoRow(stringResource(R.string.camera_apertures), info.apertures)
        InfoRow(stringResource(R.string.camera_focal_lengths), info.focalLengths)
        InfoRow(stringResource(R.string.camera_sensor_size), info.sensorSize)
    }
}