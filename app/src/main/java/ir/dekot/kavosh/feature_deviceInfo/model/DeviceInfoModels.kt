package ir.dekot.kavosh.feature_deviceInfo.model

import ir.dekot.kavosh.data.model.components.SystemInfo
import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val cpu: CpuInfo = CpuInfo(),
    val gpu: GpuInfo = GpuInfo(),
    val thermal: List<ThermalInfo> = emptyList(),
    val ram: RamInfo = RamInfo(),
    val storage: StorageInfo = StorageInfo(),
    val display: DisplayInfo = DisplayInfo(),
    val system: SystemInfo = SystemInfo(),
    val sensors: List<SensorInfo> = emptyList(),
    val network: NetworkInfo = NetworkInfo(), // <-- این خط را اضافه کنید
    val cameras: List<CameraInfo> = emptyList(), // <-- این خط را اضافه کنید
    val simCards: List<SimInfo> = emptyList(), // <-- پراپرتی جدید
    val apps: List<AppInfo> = emptyList() // <-- پراپرتی جدید برای لیست برنامه‌ها
)
// این فایل تمام کلاس‌های داده‌ای را که برای نمایش اطلاعات نیاز داریم، نگه می‌دارد.