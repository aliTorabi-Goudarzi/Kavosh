package ir.dekot.kavosh.feature_deviceInfo.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.dekot.kavosh.R
import ir.dekot.kavosh.core.ui.shared_components.KavoshTopAppBar
import ir.dekot.kavosh.core.navigation.NavigationViewModel
import ir.dekot.kavosh.feature_export_and_sharing.model.ReportFormatter
import ir.dekot.kavosh.core.util.shareText
import ir.dekot.kavosh.feature_deviceInfo.model.InfoCategory
import ir.dekot.kavosh.feature_deviceInfo.model.localizedTitle
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceCacheViewModel
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceInfoViewModel
import ir.dekot.kavosh.feature_deviceInfo.viewModel.DeviceScanViewModel
import ir.dekot.kavosh.feature_testing.viewModel.StorageViewModel
import kotlinx.coroutines.launch

private fun buildSelectedItemsString(
    allItems: List<Pair<String, String>>,
    selections: Map<Pair<String, String>, Boolean>
): String {
    return buildString {
        allItems.forEachIndexed { index, item ->
            val isHeader = item.second.isEmpty()
            if (isHeader) {
                val hasSelectedChildren = allItems
                    .subList(index + 1, allItems.size)
                    .takeWhile { !it.second.isEmpty() }
                    .any { selections[it] == true }

                if (hasSelectedChildren) {
                    if (this.isNotEmpty()) appendLine()
                    appendLine(item.first)
                }
            } else {
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
    deviceInfoViewModel: DeviceInfoViewModel,
    deviceCacheViewModel: DeviceCacheViewModel,
    navigationViewModel: NavigationViewModel, // <-- پارامتر جدید
    onBackClick: () -> Unit,
    // **اصلاح کلیدی: دریافت مستقیم StorageViewModel با Hilt**
    storageViewModel: StorageViewModel = hiltViewModel()
) {
    val appsLoadingState by deviceCacheViewModel.appsLoadingState.collectAsState()
    val deviceInfo by deviceCacheViewModel.deviceInfo.collectAsState()
    val batteryInfo by deviceInfoViewModel.batteryInfo.collectAsState()
    val thermalDetails by deviceInfoViewModel.thermalDetails.collectAsState()
    val liveCpuFrequencies by deviceInfoViewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by deviceInfoViewModel.liveGpuLoad.collectAsState()
    val downloadSpeed by deviceInfoViewModel.downloadSpeed.collectAsState()
    val uploadSpeed by deviceInfoViewModel.uploadSpeed.collectAsState()
    // تمام وضعیت‌های مورد نیاز در اینجا collect می‌شوند
    val userApps by deviceCacheViewModel.userApps.collectAsState()
    val systemApps by deviceCacheViewModel.systemApps.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCopyDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // وقتی کاربر وارد صفحه می‌شود، درخواست بارگذاری را ارسال کن
    LaunchedEffect(category) {
        if (category == InfoCategory.APPS) {
            deviceCacheViewModel.loadAppsListIfNeeded()
        }
    }

    if (showCopyDialog || showShareDialog) {
        // *** تغییر کلیدی: پاس دادن context ***
        val itemsToSelect =
            ReportFormatter.getCategoryData(context, category, deviceInfo, batteryInfo)

        InfoSelectionDialog(
            onDismissRequest = {
                showCopyDialog = false
                showShareDialog = false
            },
            itemsToSelect = itemsToSelect,
            title = if (showCopyDialog) stringResource(R.string.copy_selection_title) else stringResource(
                R.string.share_selection_title
            ),
            confirmButtonText = if (showCopyDialog) stringResource(R.string.copy_selection_button) else stringResource(
                R.string.share_selection_button
            ),
            onConfirm = { selections ->
                val text = buildSelectedItemsString(itemsToSelect, selections)
                if (text.isNotBlank()) {
                    if (showCopyDialog) {
                        clipboardManager.setText(AnnotatedString(text))
                        scope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.copied_to_clipboard))
                        }
                    } else {
                        shareText(context, text)
                    }
                }
            }
        )
    }

    // *** شروع اصلاحات کلیدی ***

    // **اصلاح کلیدی: همگام‌سازی داده‌ها بین دو ViewModel**
    // این افکت داده‌های دستگاه را از DeviceCacheViewModel به DeviceInfoViewModel منتقل می‌کند
    LaunchedEffect(deviceInfo) {
        deviceInfoViewModel.updateDeviceInfo(deviceInfo)
    }

    // این افکت با ورود به صفحه اجرا شده و polling را آغاز می‌کند
    LaunchedEffect(key1 = category) {
        deviceInfoViewModel.startPollingForCategory(category)
    }

    // این افکت با خروج از صفحه اجرا شده و تمام polling ها را متوقف می‌کند
    DisposableEffect(key1 = Unit) {
        onDispose {
            deviceInfoViewModel.stopAllPolling()
        }
    }

    // *** پایان اصلاحات کلیدی ***


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // استفاده از نوار بالایی سفارشی برای یکپارچگی رنگی
            KavoshTopAppBar(
                title = { Text(category.localizedTitle()) },
                onBackClick = onBackClick,
                /**
                 * کامنت: یک شرط ساده اضافه شد تا آیکون‌های کپی و اشتراک‌گذاری
                 * فقط برای صفحاتی غیر از "برنامه‌ها" نمایش داده شوند.
                 */
                actions = {
                    if (category != InfoCategory.APPS) {
                        IconButton(onClick = { showCopyDialog = true }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.copy))
                        }
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // **کامنت: برای دسته APPS، دیگر از LazyColumn استفاده نمی‌کنیم چون صفحه خودش اسکرول دارد.**
        if (category == InfoCategory.APPS) {
            Box(modifier = Modifier.padding(paddingValues)) {
                AppsPage(deviceCacheViewModel = deviceCacheViewModel)
            }
        } else {
            // برای سایر صفحات، از LazyColumn استفاده می‌کنیم تا اسکرول داشته باشند
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    when (category) {
                        InfoCategory.SOC -> SocPage(
                            deviceInfoViewModel,
                            onNavigateToStressTest = { navigationViewModel.navigateToCpuStressTest() })

                        InfoCategory.DEVICE -> DevicePage(
                            deviceInfoViewModel,
                            storageViewModel,
                            onNavigateToDisplayTest = { navigationViewModel.navigateToDisplayTest() })

                        InfoCategory.SYSTEM -> SystemPage(deviceInfoViewModel)
                        InfoCategory.BATTERY -> BatteryPage(deviceInfoViewModel)
                        InfoCategory.SENSORS -> SensorsPage(deviceInfoViewModel, navigationViewModel)
                        InfoCategory.THERMAL -> ThermalPage(deviceInfoViewModel)
                        InfoCategory.CAMERA -> CameraPage(deviceInfoViewModel)
                        InfoCategory.NETWORK -> NetworkPage(
                            deviceInfoViewModel,
                            onNavigateToTools = { navigationViewModel.navigateToNetworkTools() })

                        InfoCategory.SIM -> SimPage(
                            deviceInfoViewModel,
                            deviceCacheViewModel
                        )
                        InfoCategory.APPS -> { /* Handled above */
                        }
                    }
                }
            }
        }
    }
}