package ir.dekot.kavosh.ui.screen.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import ir.dekot.kavosh.ui.screen.detail.pages.AppsPage
import ir.dekot.kavosh.ui.screen.detail.pages.BatteryPage
import ir.dekot.kavosh.ui.screen.detail.pages.CameraPage
import ir.dekot.kavosh.ui.screen.detail.pages.DevicePage
import ir.dekot.kavosh.ui.screen.detail.pages.NetworkPage
import ir.dekot.kavosh.ui.screen.detail.pages.SensorsPage
import ir.dekot.kavosh.ui.screen.detail.pages.SimPage
import ir.dekot.kavosh.ui.screen.detail.pages.SocPage
import ir.dekot.kavosh.ui.screen.detail.pages.SystemPage
import ir.dekot.kavosh.ui.screen.detail.pages.ThermalPage
import ir.dekot.kavosh.ui.viewmodel.DeviceInfoViewModel
import ir.dekot.kavosh.ui.viewmodel.InfoCategory
import ir.dekot.kavosh.ui.viewmodel.NavigationViewModel
import ir.dekot.kavosh.ui.viewmodel.StorageViewModel
import ir.dekot.kavosh.ui.viewmodel.localizedTitle
import ir.dekot.kavosh.util.report.ReportFormatter
import ir.dekot.kavosh.util.shareText
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
    viewModel: DeviceInfoViewModel,
    navigationViewModel: NavigationViewModel, // <-- پارامتر جدید
    onBackClick: () -> Unit,
    // **اصلاح کلیدی: دریافت مستقیم StorageViewModel با Hilt**
    storageViewModel: StorageViewModel = hiltViewModel()
) {
    val appsLoadingState by viewModel.appsLoadingState.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val thermalDetails by viewModel.thermalDetails.collectAsState()
    val liveCpuFrequencies by viewModel.liveCpuFrequencies.collectAsState()
    val liveGpuLoad by viewModel.liveGpuLoad.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    // تمام وضعیت‌های مورد نیاز در اینجا collect می‌شوند
    val userApps by viewModel.userApps.collectAsState()
    val systemApps by viewModel.systemApps.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCopyDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    // وقتی کاربر وارد صفحه می‌شود، درخواست بارگذاری را ارسال کن
    LaunchedEffect(category) {
        if (category == InfoCategory.APPS) {
            viewModel.loadAppsListIfNeeded()
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

    // این افکت با ورود به صفحه اجرا شده و polling را آغاز می‌کند
    LaunchedEffect(key1 = category) {
        viewModel.startPollingForCategory(category)
    }

    // این افکت با خروج از صفحه اجرا شده و تمام polling ها را متوقف می‌کند
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.stopAllPolling()
        }
    }

    // *** پایان اصلاحات کلیدی ***


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                // *** تغییر کلیدی: استفاده از تابع الحاقی برای عنوان ***
                title = { Text(category.localizedTitle()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
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
                AppsPage(viewModel = viewModel)
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
                            viewModel,
                            onNavigateToStressTest = { navigationViewModel.navigateToCpuStressTest() })

                        InfoCategory.DEVICE -> DevicePage(
                            viewModel,
                            storageViewModel,
                            onNavigateToDisplayTest = { navigationViewModel.navigateToDisplayTest() })

                        InfoCategory.SYSTEM -> SystemPage(viewModel)
                        InfoCategory.BATTERY -> BatteryPage(viewModel)
                        InfoCategory.SENSORS -> SensorsPage(viewModel, navigationViewModel)
                        InfoCategory.THERMAL -> ThermalPage(viewModel)
                        InfoCategory.CAMERA -> CameraPage(viewModel)
                        InfoCategory.NETWORK -> NetworkPage(
                            viewModel,
                            onNavigateToTools = { navigationViewModel.navigateToNetworkTools() })

                        InfoCategory.SIM -> SimPage(viewModel)
                        InfoCategory.APPS -> { /* Handled above */
                        }
                    }
                }
            }
        }
    }
}