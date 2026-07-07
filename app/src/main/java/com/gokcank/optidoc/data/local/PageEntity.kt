package com.gokcank.optidoc.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bir belgenin tek sayfasını temsil eden Room entity'si.
 *
 * [documentId] üzerindeki foreign key, üst belge silindiğinde tüm sayfaları
 * otomatik siler (CASCADE). [documentId] indekslendi — foreign key sorgularında
 * Room tarafından önerilir ve sorgu performansını artırır.
 *
 * @property id          Otomatik artan birincil anahtar.
 * @property documentId  Bağlı [DocumentEntity] id'si.
 * @property pageNumber  Belgede sayfanın sırası (0-tabanlı).
 * @property imageUri    ML Kit Document Scanner'ın ürettiği JPEG görselinin Uri'si (String).
 * @property ocrText     OCR ile çıkarılan metin; null = OCR henüz uygulanmadı.
 *                       Kullanıcı bu alanı sonradan düzenleyebilir.
 */
@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val documentId: Long,
    val pageNumber: Int,
    val imageUri: String,
    val ocrText: String? = null
)
