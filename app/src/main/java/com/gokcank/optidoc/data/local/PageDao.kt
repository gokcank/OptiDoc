package com.gokcank.optidoc.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * [PageEntity] için veri erişim nesnesi.
 *
 * Sayfaların silinmesi [DocumentEntity] CASCADE kuralıyla yönetilir;
 * [deletePagesByDocumentId] açık kullanım için de sağlanmıştır
 * (örn. tarama yeniden yapıldığında eski sayfaları temizleme).
 */
@Dao
interface PageDao {

    // ── Yazma ───────────────────────────────────────────────────────────────────────────

    /**
     * Birden fazla sayfayı tek seferde ekler (tarama sonucu için).
     * @return Her eklenen satırın id listesi.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPages(pages: List<PageEntity>): List<Long>

    /** Belirtilen sayfanın yalnızca OCR metnini günceller (kullanıcı düzenlemesi). */
    @Query("UPDATE pages SET ocrText = :text WHERE id = :pageId")
    suspend fun updateOcrText(pageId: Long, text: String)

    /** Sayfa numarasını (sırasını) günceller. */
    @Query("UPDATE pages SET pageNumber = :newPageNumber WHERE id = :pageId")
    suspend fun updatePageNumber(pageId: Long, newPageNumber: Int)
}
