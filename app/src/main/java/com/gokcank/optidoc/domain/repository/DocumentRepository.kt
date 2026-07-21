package com.gokcank.optidoc.domain.repository

import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ScannedDocument
import kotlinx.coroutines.flow.Flow

/**
 * Belge ve sayfa CRUD işlemlerinin sözleşmesi.
 *
 * Implementasyon [data.repository.DocumentRepositoryImpl] içindedir.
 * Test ortamında kolayca taklit edilebilir (fake/mock).
 */
interface DocumentRepository {

    /** Tüm belgeleri oluşturma tarihine göre azalan sırada yayar. */
    fun getDocuments(): Flow<List<ScannedDocument>>

    /**
     * Verilen [id]'ye sahip belgeyi sayfalarıyla birlikte yayar.
     * Belge bulunamazsa null yayar.
     */
    fun getDocumentWithPages(id: Long): Flow<Pair<ScannedDocument, List<DocumentPage>>?>

    /**
     * Yeni bir belge kaydeder.
     * @return Eklenen belgenin veritabanı id'si.
     */
    suspend fun saveDocument(document: ScannedDocument): Long

    /** Sayfaları toplu olarak kaydeder ve id listesini döndürür. */
    suspend fun savePages(pages: List<DocumentPage>): List<Long>

    /** Belge başlığını günceller. */
    suspend fun updateTitle(id: Long, title: String)

    /**
     * Belgenin çıktı durumunu ve sayfa sayısını günceller.
     * OCR veya direkt kaydetme tamamlandığında çağrılır.
     */
    suspend fun updateOutput(id: Long, output: DocumentOutput, pageCount: Int)

    /** Tek bir sayfanın OCR metnini günceller (kullanıcı düzenlemesi). */
    suspend fun updatePageText(pageId: Long, text: String)

    /** Belgeyi ve tüm sayfalarını siler. */
    suspend fun deleteDocument(id: Long)

    // ── Klasör İşlemleri ─────────────────────────────────────────────────────

    /** Belirli bir klasördeki belgeleri yayar. */
    fun getDocumentsByFolder(folderId: Long): Flow<List<ScannedDocument>>

    /** Klasörsüz (ana dizindeki) belgeleri yayar. */
    fun getDocumentsWithoutFolder(): Flow<List<ScannedDocument>>

    /** Belgeyi bir klasöre taşır veya klasörden çıkarır (folderId = null). */
    suspend fun moveToFolder(documentId: Long, folderId: Long?)

    /** Tüm klasörleri yayar. */
    fun getAllFolders(): Flow<List<com.gokcank.optidoc.domain.model.Folder>>

    /** Yeni klasör oluşturur ve id'sini döner. */
    suspend fun createFolder(name: String): Long

    /** Klasörü siler. */
    suspend fun deleteFolder(id: Long)

    // ── Sayfa Sıralama ───────────────────────────────────────────────────────

    /** Sayfa sırasını günceller. */
    suspend fun updatePageNumber(pageId: Long, newPageNumber: Int)
}
