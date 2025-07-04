package ir.dekot.kavosh.ui.viewmodel

enum class ExportFormat(val mimeType: String, val extension: String) {
    TXT("text/plain", "txt"),
    PDF("application/pdf", "pdf"),
    JSON("application/json", "json")
}