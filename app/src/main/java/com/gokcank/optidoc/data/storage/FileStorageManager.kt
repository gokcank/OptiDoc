package com.gokcank.optidoc.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import com.gokcank.optidoc.core.dispatcher.DispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit'in geçici (cache) klasörüne yazdığı görselleri ve PDF'i uygulamanın
 * kalıcı iç depolama klasörüne kopyalar.
 *
 * ML Kit, tarama sonrası ürettiği JPEG ve PDF dosyalarını uygulama cache'ine
 * yazar. Android sistemi veya ML Kit bu cache'i herhangi bir zamanda
 * temizleyebilir; bu da önizleme ekranlarının boş görünmesine yol açar.
 * Bu sınıf, kayıt öncesinde dosyaları `filesDir/documents/<documentId>/` altına
 * kopyalayarak görsellerin kalıcı olmasını sağlar.
 */
@Singleton
class FileStorageManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) {
    private fun documentDir(documentId: Long): File =
        File(context.filesDir, "documents/$documentId").apply { mkdirs() }

    /**
     * Verilen URI listesindeki dosyaları [documentId] klasörüne kopyalar.
     * @return Kopyalanan dosyaların `file://` URI'lerinden oluşan liste.
     */
    suspend fun copyPageImages(documentId: Long, uris: List<String>): List<String> =
        withContext(dispatcherProvider.io) {
            val dir = documentDir(documentId)
            uris.mapIndexed { index, uriStr ->
                try {
                    val dest = File(dir, "page_${index + 1}.jpg")
                    copyUri(Uri.parse(uriStr), dest)
                    dest.toURI().toString()
                } catch (e: Exception) {
                    Log.w(TAG, "Sayfa kopyalanamadı (index=$index): $uriStr", e)
                    uriStr // Orijinal URI'yi yedek olarak döndür
                }
            }
        }

    suspend fun copyNativePdf(documentId: Long, uri: String): String =
        withContext(dispatcherProvider.io) {
            try {
                val dest = File(documentDir(documentId), "native.pdf")
                copyUri(Uri.parse(uri), dest)
                dest.toURI().toString()
            } catch (e: Exception) {
                Log.w(TAG, "Native PDF kopyalanamadı: $uri", e)
                uri
            }
        }

    /**
     * Taramadan hemen sonra ML Kit önbelleğinden silinmesini önlemek için
     * geçici (temp) dizine kopyalar.
     */
    suspend fun copyToTemp(uris: List<String>, pdfUri: String?): Pair<List<String>, String?> =
        withContext(dispatcherProvider.io) {
            val tempDir = File(context.filesDir, "temp_scan").apply {
                deleteRecursively() // Eski temp dosyalarını temizle
                mkdirs()
            }
            
            val newUris = uris.mapIndexed { index, uriStr ->
                try {
                    val dest = File(tempDir, "temp_page_${index + 1}.jpg")
                    copyUri(Uri.parse(uriStr), dest)
                    dest.toURI().toString()
                } catch (e: Exception) {
                    Log.w(TAG, "Geçici sayfa kopyalanamadı: $uriStr", e)
                    uriStr
                }
            }
            
            val newPdfUri = pdfUri?.let { uriStr ->
                try {
                    val dest = File(tempDir, "temp_native.pdf")
                    copyUri(Uri.parse(uriStr), dest)
                    dest.toURI().toString()
                } catch (e: Exception) {
                    Log.w(TAG, "Geçici PDF kopyalanamadı: $uriStr", e)
                    uriStr
                }
            }
            
            Pair(newUris, newPdfUri)
        }

    /** Dokümanın tüm kalıcı dosyalarını siler. */
    suspend fun deleteDocumentFiles(documentId: Long) = withContext(dispatcherProvider.io) {
        try {
            documentDir(documentId).deleteRecursively()
        } catch (e: Exception) {
            Log.w(TAG, "Dosyalar silinemedi: documentId=$documentId", e)
        }
    }

    private fun copyUri(source: Uri, dest: File) {
        context.contentResolver.openInputStream(source)?.use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("InputStream açılamadı: $source")
    }

    companion object {
        private const val TAG = "FileStorageManager"
    }
}
