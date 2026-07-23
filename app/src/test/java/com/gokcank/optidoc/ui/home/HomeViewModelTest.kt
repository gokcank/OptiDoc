package com.gokcank.optidoc.ui.home

import com.gokcank.optidoc.core.dispatcher.DispatcherProvider
import com.gokcank.optidoc.data.storage.FileStorageManager
import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.Folder
import com.gokcank.optidoc.domain.model.ScannedDocument
import com.gokcank.optidoc.domain.repository.DocumentRepository
import com.gokcank.optidoc.domain.repository.FileStorageRepository
import com.gokcank.optidoc.domain.usecase.CreateFolderUseCase
import com.gokcank.optidoc.domain.usecase.DeleteDocumentUseCase
import com.gokcank.optidoc.domain.usecase.DeleteFolderUseCase
import com.gokcank.optidoc.domain.usecase.GetDocumentsUseCase
import com.gokcank.optidoc.domain.usecase.GetFoldersUseCase
import com.gokcank.optidoc.domain.usecase.MoveDocumentToFolderUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val doc1 = ScannedDocument(id = 1L, title = "Fatura 2026", createdAt = 1000L, pageCount = 2, output = DocumentOutput.None, folderId = null)
    private val doc2 = ScannedDocument(id = 2L, title = "Sözleşme PDF", createdAt = 2000L, pageCount = 5, output = DocumentOutput.None, folderId = 10L)

    private val folder1 = Folder(id = 10L, name = "Resmi İşler", createdAt = 500L)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `searchQuery filters document titles correctly`() = runTest {
        val repo = FakeHomeRepository(listOf(doc1, doc2), listOf(folder1))
        val fakeFileStorageRepo = FakeFileStorageRepository()
        val deleteUseCase = DeleteDocumentUseCase(repo, fakeFileStorageRepo, DummyFileStorageManager())

        val viewModel = HomeViewModel(
            getDocumentsUseCase = GetDocumentsUseCase(repo),
            getFoldersUseCase = GetFoldersUseCase(repo),
            deleteDocumentUseCase = deleteUseCase,
            createFolderUseCase = CreateFolderUseCase(repo),
            deleteFolderUseCase = DeleteFolderUseCase(repo),
            moveDocumentToFolderUseCase = MoveDocumentToFolderUseCase(repo)
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.updateSearchQuery("sözleşme")

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        val docs = (state as HomeUiState.Success).documents
        assertEquals(1, docs.size)
        assertEquals("Sözleşme PDF", docs.first().title)

        collectJob.cancel()
    }

    @Test
    fun `selecting folder filters documents by folderId`() = runTest {
        val repo = FakeHomeRepository(listOf(doc1, doc2), listOf(folder1))
        val fakeFileStorageRepo = FakeFileStorageRepository()
        val deleteUseCase = DeleteDocumentUseCase(repo, fakeFileStorageRepo, DummyFileStorageManager())

        val viewModel = HomeViewModel(
            getDocumentsUseCase = GetDocumentsUseCase(repo),
            getFoldersUseCase = GetFoldersUseCase(repo),
            deleteDocumentUseCase = deleteUseCase,
            createFolderUseCase = CreateFolderUseCase(repo),
            deleteFolderUseCase = DeleteFolderUseCase(repo),
            moveDocumentToFolderUseCase = MoveDocumentToFolderUseCase(repo)
        )

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.selectFolder(10L)

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        val docs = (state as HomeUiState.Success).documents
        assertEquals(1, docs.size)
        assertEquals(2L, docs.first().id)

        collectJob.cancel()
    }
}

private class DummyFileStorageManager : FileStorageManager(
    android.content.ContextWrapper(null),
    object : DispatcherProvider {
        override val io: CoroutineDispatcher get() = Dispatchers.Unconfined
        override val default: CoroutineDispatcher get() = Dispatchers.Unconfined
        override val main: CoroutineDispatcher get() = Dispatchers.Unconfined
    }
) {
    override suspend fun deleteDocumentFiles(documentId: Long) {}
}

private class FakeHomeRepository(
    private val docs: List<ScannedDocument>,
    private val foldersList: List<Folder>
) : DocumentRepository {
    override fun getDocuments(): Flow<List<ScannedDocument>> = flowOf(docs)
    override fun getAllFolders(): Flow<List<Folder>> = flowOf(foldersList)
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
    override suspend fun createFolder(name: String): Long = 1L
    override suspend fun deleteFolder(id: Long) {}
    override suspend fun updatePageNumber(pageId: Long, newPageNumber: Int) {}
}

private class FakeFileStorageRepository : FileStorageRepository {
    override suspend fun deleteFiles(uris: List<String>) {}
}
