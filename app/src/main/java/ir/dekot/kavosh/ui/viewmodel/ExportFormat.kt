package ir.dekot.kavosh.ui.viewmodel

enum class ExportFormat(val mimeType: String, val extension: String) {
    TXT("text/plain", "txt"),
    PDF("application/pdf", "pdf"),
    JSON("application/json", "json"),
    HTML("text/html", "html"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    QR_CODE("image/png", "png")
}