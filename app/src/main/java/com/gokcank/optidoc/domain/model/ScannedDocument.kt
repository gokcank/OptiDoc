package com.gokcank.optidoc.domain.model

/**
 * Taranan bir belgenin domain modeli. Room entity'lerinden veya
 * Android framework'ünden bağımsızdır.
 *
 * @property id        Veritabanı birincil anahtarı (0 = henüz kaydedilmemiş).
 * @property title     Kullanıcı tarafından düzenlenebilir başlık.
 * @property createdAt Oluşturulma zamanı (epoch millis, UTC).
 * @property pageCount Belgedeki sayfa sayısı.
 * @property output    Belgenin mevcut çıktı durumu.
 */
data class ScannedDocument(
    val id: Long = 0L,
    val title: String,
    val createdAt: Long,
    val pageCount: Int,
    val output: DocumentOutput = DocumentOutput.None,
    val folderId: Long? = null
)
