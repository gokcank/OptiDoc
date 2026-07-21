package com.gokcank.optidoc.domain.model

/**
 * Klasör domain modeli.
 *
 * @property id        Veritabanı birincil anahtarı (0 = henüz kaydedilmemiş).
 * @property name      Klasör adı.
 * @property createdAt Oluşturulma zamanı (epoch millis, UTC).
 */
data class Folder(
    val id: Long = 0L,
    val name: String,
    val createdAt: Long
)
