package com.gokcank.optidoc.data.repository

import com.gokcank.optidoc.data.local.DocumentDao
import com.gokcank.optidoc.data.local.OutputFormat
import com.gokcank.optidoc.data.local.PageDao
import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ExportFormat
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

import com.gokcank.optidoc.data.local.FolderDao
import com.gokcank.optidoc.data.local.FolderEntity
import com.gokcank.optidoc.domain.model.Folder

/**
 * [DocumentRepository] arayüzünün Room tabanlı implementasyonu.
 *
 * Entity ↔ domain model dönüşümleri [Mappers.kt] içinde tanımlıdır.
 * Bu sınıf yalnızca DAO çağrısı ve dönüşüm orkestrasyonu yapar.
 */
class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao,
    private val pageDao: PageDao,
    private val folderDao: FolderDao
) : DocumentRepository {

    override fun getDocuments(): Flow<List<ScannedDocument>> =
        documentDao.getAllDocuments().map { list -> list.map { it.toDomain() } }

    override fun getDocumentWithPages(
        id: Long
    ): Flow<Pair<ScannedDocument, List<DocumentPage>>?> =
        documentDao.getDocumentWithPages(id).map { withPages ->
            withPages?.let {
                it.document.toDomain() to it.pages.map { page -> page.toDomain() }
            }
        }

    override suspend fun saveDocument(document: ScannedDocument): Long =
        documentDao.insertDocument(document.toEntity())

    override suspend fun savePages(pages: List<DocumentPage>): List<Long> =
        pageDao.insertPages(pages.map { it.toEntity() })

    override suspend fun updateTitle(id: Long, title: String) =
        documentDao.updateTitle(id, title)

    override suspend fun updateOutput(
        id: Long,
        output: DocumentOutput,
        pageCount: Int
    ) {
        when (output) {
            is DocumentOutput.None -> Unit // henüz çıktı yok, güncelleme gerekmez
            is DocumentOutput.DirectPdf ->
                documentDao.updateOutput(id, OutputFormat.DIRECT_PDF, output.uri, pageCount)
            is DocumentOutput.OcrExport -> {
                val fmt = if (output.format == ExportFormat.TXT) OutputFormat.OCR_TXT
                          else OutputFormat.OCR_PDF
                documentDao.updateOutput(id, fmt, output.uri, pageCount)
            }
        }
    }

    override suspend fun updatePageText(pageId: Long, text: String) =
        pageDao.updateOcrText(pageId, text)

    override suspend fun deleteDocument(id: Long) =
        documentDao.deleteDocumentById(id)

    override fun getDocumentsByFolder(folderId: Long): Flow<List<ScannedDocument>> =
        documentDao.getDocumentsByFolder(folderId).map { list -> list.map { it.toDomain() } }

    override fun getDocumentsWithoutFolder(): Flow<List<ScannedDocument>> =
        documentDao.getDocumentsWithoutFolder().map { list -> list.map { it.toDomain() } }

    override suspend fun moveToFolder(documentId: Long, folderId: Long?) =
        documentDao.moveToFolder(documentId, folderId)

    override fun getAllFolders(): Flow<List<Folder>> =
        folderDao.getAllFolders().map { list -> list.map { it.toDomain() } }

    override suspend fun createFolder(name: String): Long =
        folderDao.insertFolder(FolderEntity(name = name, createdAt = System.currentTimeMillis()))

    override suspend fun deleteFolder(id: Long) =
        folderDao.deleteFolderById(id)

    override suspend fun updatePageNumber(pageId: Long, newPageNumber: Int) =
        pageDao.updatePageNumber(pageId, newPageNumber)
}
