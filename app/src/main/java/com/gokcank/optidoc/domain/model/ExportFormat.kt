package com.gokcank.optidoc.domain.model

/**
 * Kullanıcının dışa aktarma işleminde belirleyebileceği format.
 *
 * - [TXT]  : Düz metin, UTF-8, .txt uzantısı.
 * - [PDF]  : Yalnızca metin içeren .pdf (PdfDocument ile üretilir).
 * - [JPEG] : Sayfa görsellerini ayrı .jpg dosyaları olarak kaydeder (OCR gerekmez).
 */
enum class ExportFormat {
    TXT,
    PDF,
    JPEG
}
