package com.gokcank.optidoc.domain.usecase

import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.Folder
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReorderPagesUseCaseTest {

    private lateinit var repository: FakeReorderDocumentRepository
    private lateinit var useCase: ReorderPagesUseCase

    @Before
    fun setUp() {
        repository = FakeReorderDocumentRepository()
        useCase = ReorderPagesUseCase(repository)
    }

    @Test
    fun `invoke updates page numbers in order`() = runTest {
        val pageIds = listOf(105L, 102L, 108L)
        useCase(pageIds)

        assertEquals(0, repository.updatedPageNumbers[105L])
        assertEquals(1, repository.updatedPageNumbers[102L])
        assertEquals(2, repository.updatedPageNumbers[108L])
    }
}

private class FakeReorderDocumentRepository : DocumentRepository {
    val updatedPageNumbers = mutableMapOf<Long, Int>()

    override suspend fun updatePageNumber(pageId: Long, newPageNumber: Int) {
        updatedPageNumbers[pageId] = newPageNumber
    }

    override fun getDocuments(): Flow<List<ScannedDocument>> = flowOf(emptyList())
    override fun getDocumentWithPages(id: Long): Flow<Pair<ScannedDocument, List<DocumentPage>>?> = flowOf(null)
    override suspend fun saveDocument(document: ScannedDocument): Long = 1L
    override suspend fun savePages(pages: List<DocumentPage>): List<Long> = emptyList()
    override suspend fun updateTitle(id: Long, title: String) {}
    override suspend fun updateOutput(id: Long, output: DocumentOutput, pageCount: Int) {}
    override suspend fun updatePageText(pageId: Long, text: String) {}
    override suspend fun deleteDocument(id: Long) {}
    override fun getDocumentsByFolder(folderId: Long): Flow<List<ScannedDocument>> = flowOf(emptyList())
    override fun getDocumentsWithoutFolder(): Flow<List<ScannedDocument>> = flowOf(emptyList())
    override suspend fun moveToFolder(documentId: Long, folderId: Long?) {}
    override fun getAllFolders(): Flow<List<Folder>> = flowOf(emptyList())
    override suspend fun createFolder(name: String): Long = 1L
    override suspend fun deleteFolder(id: Long) {}
}
