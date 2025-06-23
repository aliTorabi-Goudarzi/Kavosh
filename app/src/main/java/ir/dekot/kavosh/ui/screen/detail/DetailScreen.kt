package ir.dekot.kavosh.ui.screen.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ir.dekot.kavosh.data.model.DeviceInfo
import ir.dekot.kavosh.data.model.components.BatteryInfo
import ir.dekot.kavosh.data.model.components.ThermalInfo
import ir.dekot.kavosh.ui.screen.detail.infoCards.*
import ir.dekot.kavosh.ui.screen.shared.EmptyStateMessage
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.util.report.ReportFormatter
import ir.dekot.kavosh.util.shareText
import kotlinx.coroutines.launch

/**
 * یک تابع کمکی برای ساختن رشته خروجی بر اساس موارد انتخاب شده.
 * این تابع از تکرار کد در بخش‌های کپی و اشتراک‌گذاری جلوگیری می‌کند.
 */
private fun buildSelectedItemsString(
    allItems: List<Pair<String, String>>,
    selections: Map<Pair<String, String>, Boolean>
): String {
    return buildString {
        allItems.forEachIndexed { index, item ->
            val isHeader = item.second.isEmpty()
            if (isHeader) {
                // اگر آیتم یک هدر است، چک کن که آیا فرزندی از آن انتخاب شده است یا خیر
                val hasSelectedChildren = allItems
                    .subList(index + 1, allItems.size)
                    .takeWhile { !it.second.isEmpty() }
                    .any { selections[it] == true }

                if (hasSelectedChildren) {
                    if (this.isNotEmpty()) appendLine()
                    appendLine(item.first)
                }
            } else {
                // اگر آیتم عادی است، فقط در صورت انتخاب شدن آن را اضافه کن
                if (selections[item] == true) {
                    appendLine("${item.first}: ${item.second}")
                }
            }
        }
    }.trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun DetailScreen(
    category: InfoCategory,
    viewModel: DeviceInfoViewModel,
    onBackClick: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val thermalDetails by viewModel.thermalDetails.collectAsState()
    val liveCpuFrequencies by viewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by viewModel.liveGpuLoad.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCopyDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    if (showCopyDialog) {
        val itemsToSelect = ReportFormatter.getCategoryData(category, deviceInfo, batteryInfo)
        InfoSelectionDialog(
            onDismissRequest = { showCopyDialog = false },
            itemsToSelect = itemsToSelect,
            title = "انتخاب موارد برای کپی",
            confirmButtonText = "کپی موارد انتخاب شده",
            onConfirm = { selections ->
                val textToCopy = buildSelectedItemsString(itemsToSelect, selections)
                if (textToCopy.isNotBlank()) {
                    clipboardManager.setText(AnnotatedString(textToCopy))
                    scope.launch {
                        snackbarHostState.showSnackbar("اطلاعات ${category.title} کپی شد")
                    }
                }
            }
        )
    }

    if (showShareDialog) {
        val itemsToSelect = ReportFormatter.getCategoryData(category, deviceInfo, batteryInfo)
        InfoSelectionDialog(
            onDismissRequest = { showShareDialog = false },
            itemsToSelect = itemsToSelect,
            title = "انتخاب موارد برای اشتراک‌گذاری",
            confirmButtonText = "اشتراک‌گذاری",
            onConfirm = { selections ->
                // --- اصلاحیه در این بخش ---
                // تابع کمکی را فراخوانی کرده و نتیجه را مستقیماً برای اشتراک‌گذاری ارسال می‌کنیم
                val textToShare = buildSelectedItemsString(itemsToSelect, selections)
                if (textToShare.isNotBlank()) {
                    shareText(context, textToShare)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(category.title) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = { showCopyDialog = true }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "کپی")
                    }
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            CategoryDetailContent(
                category = category,
                deviceInfo = deviceInfo,
                batteryInfo = batteryInfo,
                thermalDetails = thermalDetails,
                liveCpuFrequencies = liveCpuFrequencies,
                liveGpuLoad = liveGpuLoad
            )
        }
    }
}

private fun LazyListScope.CategoryDetailContent(
    category: InfoCategory,
    deviceInfo: DeviceInfo,
    batteryInfo: BatteryInfo,
    thermalDetails: List<ThermalInfo>,
    liveCpuFrequencies: List<String>,
    liveGpuLoad: Int?
) {
    when (category) {
        InfoCategory.SOC -> {
            item { CpuInfoCard(deviceInfo.cpu, liveCpuFrequencies) }
            item { GpuInfoCard(deviceInfo.gpu, liveGpuLoad) }
            item { RamInfoCard(deviceInfo.ram) }
        }
        InfoCategory.DEVICE -> {
            item { DisplayInfoCard(deviceInfo.display) }
            item { StorageInfoCard(deviceInfo.storage) }
        }
        InfoCategory.SYSTEM -> {
            item { SystemInfoCard(deviceInfo.system) }
        }
        InfoCategory.BATTERY -> {
            item { BatteryInfoCard(batteryInfo) }
        }
        InfoCategory.SENSORS -> {
            // --- تغییر کلیدی در این بخش ---
            if (deviceInfo.sensors.isEmpty()) {
                item { EmptyStateMessage("هیچ سنسوری در این دستگاه یافت نشد.") }
            } else {
                items(deviceInfo.sensors, key = { it.name }) { sensor ->
                    SensorInfoCard(info = sensor)
                }
            }
        }
        InfoCategory.THERMAL -> {
            // --- تغییر کلیدی در این بخش ---
            if (thermalDetails.isEmpty()) {
                item { EmptyStateMessage("اطلاعات دمای حرارتی برای این دستگاه در دسترس نیست.") }
            } else {
                items(thermalDetails, key = { it.type }) { thermalInfo ->
                    ThermalInfoCard(info = thermalInfo)
                }
            }
        }
        InfoCategory.CAMERA -> {
            // --- تغییر کلیدی در این بخش ---
            if (deviceInfo.cameras.isEmpty()) {
                item { EmptyStateMessage("دوربینی یافت نشد یا دسترسی به آن ممکن نیست.") }
            } else {
                items(deviceInfo.cameras, key = { it.id }) { camera ->
                    CameraInfoCard(info = camera)
                }
            }
        }
        InfoCategory.NETWORK -> {
            item { NetworkInfoCard(deviceInfo.network) }
        }
    }
}