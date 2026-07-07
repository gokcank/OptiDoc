package com.gokcank.optidoc.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.gokcank.optidoc.core.dispatcher.DispatcherProvider
import com.gokcank.optidoc.domain.repository.FileStorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * [FileStorageRepository] implementasyonu.
 *
 * Hem content:// URI'lerini (FileProvider ile paylaşılan export dosyaları)
 * hem de file:// URI'lerini (kalıcı depolamadaki görsel dosyaları) siler.
 */
class FileStorageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : FileStorageRepository {

    override suspend fun deleteFiles(uris: List<String>) = withContext(dispatcherProvider.io) {
        for (uriString in uris) {
            try {
                val uri = Uri.parse(uriString)
                when (uri.scheme) {
                    "file" -> {
                        // Doğrudan dosya yolu — File.delete() kullan
                        val file = File(uri.path ?: continue)
                        if (file.exists()) file.delete()
                    }
                    "content" -> {
                        // FileProvider veya MediaStore URI — ContentResolver kullan
                        context.contentResolver.delete(uri, null, null)
                    }
                    else -> {
                        // Bilinmeyen şema — her ikisini de dene
                        try { context.contentResolver.delete(uri, null, null) } catch (_: Exception) {}
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Dosya silinemedi, atlanıyor: $uriString", e)
            }
        }
    }

    companion object {
        private const val TAG = "FileStorageRepository"
    }
}
