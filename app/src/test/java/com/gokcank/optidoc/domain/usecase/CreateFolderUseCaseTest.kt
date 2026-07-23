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

class CreateFolderUseCaseTest {

    private lateinit var repository: FakeDocumentRepository
    private lateinit var useCase: CreateFolderUseCase

    @Before
    fun setUp() {
        repository = FakeDocumentRepository()
        useCase = CreateFolderUseCase(repository)
    }

    @Test
    fun `invoke with valid name creates folder and returns id`() = runTest {
        val folderId = useCase(" İş Documents ")
        assertEquals(1L, folderId)
        assertEquals("İş Documents", repository.createdFolderName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with blank name throws IllegalArgumentException`() = runTest {
        useCase("   ")
    }
}

private class FakeDocumentRepository : DocumentRepository {
    var createdFolderName: String? = null
    private var nextFolderId = 1L

    override suspend fun createFolder(name: String): Long {
        createdFolderName = name
        return nextFolderId++
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
    override suspend fun deleteFolder(id: Long) {}
    override suspend fun updatePageNumber(pageId: Long, newPageNumber: Int) {}
}
