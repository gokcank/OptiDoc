package com.gokcank.optidoc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Taranan bir belgeyi temsil eden Room entity'si.
 *
 * ## İki çıktı yolu
 * - **OCR'sız (DIRECT_PDF):** [outputFormat] = [OutputFormat.DIRECT_PDF],
 *   [outputUri] = ML Kit'in ürettiği PDF'in Uri'si.
 * - **OCR'lı (OCR_TXT / OCR_PDF):** [outputFormat] = ilgili değer,
 *   [outputUri] = dışa aktarılan dosyanın Uri'si.
 * - **Henüz işlenmemiş:** [outputFormat] = null, [outputUri] = null.
 *
 * @property id           Otomatik artan birincil anahtar.
 * @property title        Kullanıcı tarafından güncellenebilir başlık.
 * @property createdAt    Oluşturulma zamanı (epoch millis, UTC).
 * @property pageCount    Belgedeki sayfa sayısı; sayfa eklenince güncellenir.
 * @property outputFormat Çıktı türü; null = henüz kaydedilmedi.
 * @property outputUri    Üretilen dosyanın Uri'si (String); null = henüz üretilmedi.
 */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val createdAt: Long,
    val pageCount: Int,
    val outputFormat: OutputFormat? = null,
    val outputUri: String? = null,
    val folderId: Long? = null
)
