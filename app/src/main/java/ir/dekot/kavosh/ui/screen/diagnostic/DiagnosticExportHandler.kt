package ir.dekot.kavosh.ui.screen.diagnostic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import ir.dekot.kavosh.ui.viewmodel.DiagnosticExportViewModel
import ir.dekot.kavosh.ui.viewmodel.DiagnosticViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * کامپوننت کمکی برای مدیریت export events در صفحات تشخیصی
 */
@Composable
fun DiagnosticExportHandler(
    diagnosticViewModel: DiagnosticViewModel = hiltViewModel(),
    diagnosticExportViewModel: DiagnosticExportViewModel = hiltViewModel()
) {
    // گوش دادن به درخواست‌های export از DiagnosticViewModel
    LaunchedEffect(Unit) {
        diagnosticViewModel.exportRequest.collectLatest { request ->
            diagnosticExportViewModel.startExport(request)
        }
    }
}
