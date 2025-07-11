package ir.dekot.kavosh.data.model

import ir.dekot.kavosh.data.model.components.CpuInfo
import ir.dekot.kavosh.data.model.components.DisplayInfo
import ir.dekot.kavosh.data.model.components.GpuInfo
import ir.dekot.kavosh.data.model.components.RamInfo
import ir.dekot.kavosh.data.model.components.SensorInfo
import ir.dekot.kavosh.data.model.components.StorageInfo
import ir.dekot.kavosh.data.model.components.SystemInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.data.model.components.CameraInfo // <-- ایمپورت جدید
// ... (imports)
import ir.dekot.kavosh.data.model.components.NetworkInfo // <-- ایمپورت جدید
import ir.dekot.kavosh.data.model.components.SimInfo
import kotlinx.serialization.Serializable
import ir.dekot.kavosh.data.model.components.AppInfo // <-- ایمپورت جدید

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