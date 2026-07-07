package com.gokcank.optidoc.domain.repository

import com.gokcank.optidoc.domain.model.ExportFormat

/**
 * OCR metnini veya sayfa görsellerini seçilen formatta ortak depolamaya yazan sözleşme.
 *
 * Dosyalar `/storage/emulated/0/Taramalar/` (TR) veya `/storage/emulated/0/Scans/` (EN)
 * klasörüne kaydedilir; MediaStore (API 29+) veya doğrudan dosya yazma (API ≤ 28) kullanılır.
 */
interface ExportRepository {

    /**
     * OCR metnini TXT veya PDF olarak kaydeder.
     *
     * @param documentId  İlişkili belgenin id'si.
     * @param text        Dışa aktarılacak metin. Çok sayfalıysa [PAGE_SEPARATOR] ile birleştirilmiş.
     * @param format      [ExportFormat.TXT] veya [ExportFormat.PDF].
     * @param title       Dosya adı tabanı (belge başlığından türetilir).
     * @return Başarıda üretilen dosyanın content:// veya file:// Uri'si.
     */
    suspend fun export(
        documentId: Long,
        text: String,
        format: ExportFormat,
        title: String = "document_$documentId"
    ): Result<String>

    /**
     * Sayfa görsellerini ayrı JPEG dosyaları olarak kaydeder.
     *
     * @param documentId    İlişkili belgenin id'si.
     * @param pageImageUris Kalıcı depolamadaki sayfa görseli URI'leri (file://).
     * @param title         Dosya adı tabanı.
     * @return Başarıda oluşturulan JPEG URI'lerinin listesi.
     */
    suspend fun exportImages(
        documentId: Long,
        pageImageUris: List<String>,
        title: String = "document_$documentId"
    ): Result<List<String>>

    /**
     * Zaten var olan bir dosyayı ortak depolamaya kopyalar.
     *
     * @param documentId İlişkili belgenin id'si.
     * @param sourceUri  Kopyalanacak dosyanın URI'si (file:// vb.)
     * @param mimeType   Dosyanın MIME türü (örn. "application/pdf")
     * @param title      Dosya adı tabanı.
     * @return Başarıda oluşturulan dosyanın URI'si.
     */
    suspend fun exportExistingFile(
        documentId: Long,
        sourceUri: String,
        mimeType: String,
        title: String = "document_$documentId"
    ): Result<String>

    companion object {
        /**
         * Çok sayfalı metni tek String'de birleştirmek için kullanılan sayfa ayracı.
         * PDF üretiminde sert sayfa sonu olarak yorumlanır.
         */
        const val PAGE_SEPARATOR = "\n\n----------\n\n"
    }
}
