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

    // ── Okuma ────────────────────────────────────────────────────────────────

    /** Belgeye ait sayfaları sayfa numarasına göre artan sırada döndürür. */
    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageNumber ASC")
    fun getPagesByDocument(documentId: Long): Flow<List<PageEntity>>

    // ── Yazma ────────────────────────────────────────────────────────────────

    /**
     * Tek bir sayfa ekler.
     * @return Eklenen satırın id'si.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPage(page: PageEntity): Long

    /**
     * Birden fazla sayfayı tek seferde ekler (tarama sonucu için).
     * @return Her eklenen satırın id listesi.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPages(pages: List<PageEntity>): List<Long>

    /** Sayfanın tüm alanlarını günceller. */
    @Update
    suspend fun updatePage(page: PageEntity)

    /** Belirtilen sayfanın yalnızca OCR metnini günceller (kullanıcı düzenlemesi). */
    @Query("UPDATE pages SET ocrText = :text WHERE id = :pageId")
    suspend fun updateOcrText(pageId: Long, text: String)

    /** Bir belgeye ait tüm sayfaları açıkça siler. */
    @Query("DELETE FROM pages WHERE documentId = :documentId")
    suspend fun deletePagesByDocumentId(documentId: Long)
}
