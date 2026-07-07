package com.gokcank.optidoc.data.local

/**
 * Belge çıktısının türünü belirtir.
 *
 * - [DIRECT_PDF]  : OCR uygulanmadı; ML Kit Document Scanner'ın ürettiği,
 *                   görsel içeren PDF kaydedildi.
 * - [OCR_TXT]     : OCR uygulandı; metin düz .txt dosyası olarak dışa aktarıldı.
 * - [OCR_PDF]     : OCR uygulandı; metin yalnızca metin içeren .pdf olarak dışa aktarıldı.
 *
 * [DocumentEntity.outputFormat] null ise belge henüz işlenmemiş demektir.
 */
enum class OutputFormat {
    DIRECT_PDF,
    OCR_TXT,
    OCR_PDF
}
