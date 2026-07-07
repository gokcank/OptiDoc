package com.gokcank.optidoc.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * [DocumentEntity] için veri erişim nesnesi.
 *
 * Tüm yazma işlemleri `suspend` fonksiyondur (IO thread'inde çalışır).
 * Okuma işlemleri [Flow] döner; Room, veritabanı değişikliklerinde
 * akışı otomatik günceller.
 */
@Dao
interface DocumentDao {

    // ── Okuma ────────────────────────────────────────────────────────────────

    /** Tüm belgeleri oluşturma tarihine göre azalan sırada döndürür. */
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    /**
     * Verilen [id]'ye sahip belgeyi sayfalarıyla birlikte döndürür.
     * @Transaction, Room'un tutarlı bir snapshot almasını garanti eder.
     */
    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    fun getDocumentWithPages(id: Long): Flow<DocumentWithPages?>

    // ── Yazma ────────────────────────────────────────────────────────────────

    /**
     * Yeni bir belge ekler.
     * @return Eklenen satırın otomatik artan id değeri.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDocument(document: DocumentEntity): Long

    /** Belgenin tüm alanlarını günceller (id değişmez). */
    @Update
    suspend fun updateDocument(document: DocumentEntity)

    /** Yalnızca başlığı günceller — tüm nesneyi yüklemekten kaçınır. */
    @Query("UPDATE documents SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: Long, title: String)

    /**
     * Çıktı bilgisini ve sayfa sayısını günceller
     * (OCR veya direkt PDF tamamlandığında çağrılır).
     */
    @Query(
        """UPDATE documents
           SET outputFormat = :format,
               outputUri    = :uri,
               pageCount    = :pageCount
           WHERE id = :id"""
    )
    suspend fun updateOutput(
        id: Long,
        format: OutputFormat,
        uri: String,
        pageCount: Int
    )

    /** Belgeyi ve foreign key CASCADE sayesinde tüm sayfalarını siler. */
    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    /** id üzerinden silme — nesneyi önceden yüklemeye gerek yoktur. */
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
}
