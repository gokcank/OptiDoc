package com.gokcank.optidoc.domain.model

/**
 * Taranan bir belgenin tek sayfasının domain modeli.
 *
 * @property id         Veritabanı birincil anahtarı (0 = henüz kaydedilmemiş).
 * @property documentId Bağlı [ScannedDocument] id'si.
 * @property pageNumber Belgede sayfanın sırası (0-tabanlı).
 * @property imageUri   ML Kit Document Scanner'ın ürettiği JPEG görselinin Uri'si.
 * @property ocrText    OCR ile çıkarılan veya kullanıcının düzenlediği metin;
 *                      null = OCR henüz uygulanmadı.
 */
data class DocumentPage(
    val id: Long = 0L,
    val documentId: Long,
    val pageNumber: Int,
    val imageUri: String,
    val ocrText: String? = null
)
