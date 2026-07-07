package com.gokcank.optidoc.domain.model

/**
 * Bir belgenin mevcut çıktı durumunu temsil eden sealed sınıf.
 *
 * - [None]       : Belge henüz işlenmemiş (kaydedilmemiş).
 * - [DirectPdf]  : OCR uygulanmadı; ML Kit'in ürettiği görsel-içeren PDF kaydedildi.
 * - [OcrExport]  : OCR uygulandı; metin seçilen formatta dışa aktarıldı.
 */
sealed class DocumentOutput {
    /** Henüz çıktı üretilmemiş. */
    data object None : DocumentOutput()

    /**
     * OCR uygulanmadan direkt kaydedilen belge.
     * @property uri ML Kit Document Scanner'ın ürettiği PDF'in Uri'si.
     */
    data class DirectPdf(val uri: String) : DocumentOutput()

    /**
     * OCR uygulanıp dışa aktarılan belge.
     * @property format Kullanıcının seçtiği dışa aktarma formatı.
     * @property uri    Üretilen .txt veya .pdf dosyasının Uri'si.
     */
    data class OcrExport(
        val format: ExportFormat,
        val uri: String
    ) : DocumentOutput()
}
