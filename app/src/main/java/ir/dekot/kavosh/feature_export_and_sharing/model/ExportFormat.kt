package ir.dekot.kavosh.feature_export_and_sharing.model

enum class ExportFormat(val mimeType: String, val extension: String) {
    TXT("text/plain", "txt"),
    PDF("application/pdf", "pdf"),
    JSON("application/json", "json"),
    HTML("text/html", "html"),
    QR_CODE("image/png", "png")
}