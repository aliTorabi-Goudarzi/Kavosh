package ir.dekot.kavosh.ui.screen.infoCards

import androidx.compose.runtime.Composable
import ir.dekot.kavosh.data.model.components.CameraInfo
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoCard
import ir.dekot.kavosh.ui.screen.infoCards.used_compose.InfoRow

@Composable
fun CameraInfoCard(info: CameraInfo) {
    // عنوان کارت نام دوربین خواهد بود
    InfoCard(title = info.name) {
        InfoRow("مگاپیکسل", info.megapixels)
        InfoRow("حداکثر رزولوشن", info.maxResolution)
        InfoRow("پشتیبانی از فلش", if (info.hasFlash) "دارد" else "ندارد")
        InfoRow("اندازه دیافراگم", info.apertures)
        InfoRow("فاصله کانونی", info.focalLengths)
        InfoRow("اندازه سنسور", info.sensorSize)
    }
}