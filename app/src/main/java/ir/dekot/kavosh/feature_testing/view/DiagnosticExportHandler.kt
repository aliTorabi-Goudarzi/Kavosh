package ir.dekot.kavosh.feature_testing.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import ir.dekot.kavosh.feature_export_and_sharing.viewModel.DiagnosticExportViewModel
import ir.dekot.kavosh.feature_testing.viewModel.DiagnosticViewModel
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
