package com.gokcank.optidoc.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.gokcank.optidoc.core.dispatcher.DispatcherProvider
import com.gokcank.optidoc.domain.model.AppError
import com.gokcank.optidoc.domain.model.ExportFormat
import com.gokcank.optidoc.domain.repository.ExportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import javax.inject.Inject

/**
 * [ExportRepository] implementasyonu.
 *
 * ## Depolama konumu
 * Dosyalar `/storage/emulated/0/Taramalar/` (TR) veya `/storage/emulated/0/Scans/` (EN)
 * klasörüne yazılır — DCIM, Download, Pictures ile aynı seviyede.
 *
 * - API 29+ : MediaStore API ile izin gerektirmeden yazar.
 * - API ≤ 28: [Environment.getExternalStorageDirectory()] + WRITE_EXTERNAL_STORAGE izni.
 */
class ExportRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : ExportRepository {

    /** Cihaz diline göre "Taramalar" veya "Scans" */
    private fun folderName(): String = "OptiDoc"

    /** Başlıktaki dosya sistemi için geçersiz karakterleri temizler. */
    private fun safeTitle(title: String) =
        title.replace(Regex("[^a-zA-Z0-9ğüşıöçĞÜŞİÖÇ _\\-]"), "_").trim()

    // ── Export (TXT / PDF) ────────────────────────────────────────────────────

    override suspend fun export(
        documentId: Long,
        text: String,
        format: ExportFormat,
        title: String
    ): Result<String> = withContext(dispatcherProvider.io) {
        try {
            val fileName = safeTitle(title)
            val uriString = when (format) {
                ExportFormat.TXT -> writePublic(
                    fileName = "$fileName.txt",
                    mimeType = "text/plain"
                ) { out -> out.write(text.toByteArray(Charsets.UTF_8)) }

                ExportFormat.PDF -> writePublic(
                    fileName = "$fileName.pdf",
                    mimeType = "application/pdf"
                ) { out -> writePdfToStream(text, out) }

                ExportFormat.JPEG -> throw IllegalArgumentException(
                    "JPEG export için exportImages() kullanın."
                )
            }
            Result.success(uriString)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AppError.ExportFailed(e))
        }
    }

    // ── Export (JPEG) ─────────────────────────────────────────────────────────

    override suspend fun exportImages(
        documentId: Long,
        pageImageUris: List<String>,
        title: String
    ): Result<List<String>> = withContext(dispatcherProvider.io) {
        try {
            val safeBase = safeTitle(title)
            val uris = pageImageUris.mapIndexed { index, uriStr ->
                val fileName = if (pageImageUris.size > 1) "${safeBase}_s${index + 1}.jpg" else "$safeBase.jpg"
                writePublic(fileName = fileName, mimeType = "image/jpeg") { out ->
                    val uri = Uri.parse(uriStr)
                    val input = if (uri.scheme == "file") {
                        File(uri.path!!).inputStream()
                    } else {
                        context.contentResolver.openInputStream(uri)
                    }
                    input?.use { it.copyTo(out) } ?: throw IOException("Görsel açılamadı: $uriStr")
                }
            }
            Result.success(uris)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AppError.ExportFailed(e))
        }
    }

    override suspend fun exportExistingFile(
        documentId: Long,
        sourceUri: String,
        mimeType: String,
        title: String
    ): Result<String> = withContext(dispatcherProvider.io) {
        try {
            val extension = when (mimeType) {
                "application/pdf" -> ".pdf"
                "text/plain" -> ".txt"
                "image/jpeg" -> ".jpg"
                else -> ""
            }
            val safeBase = safeTitle(title)
            val fileName = "${safeBase}$extension"
            
            var generatedUri = ""
            writePublic(fileName = fileName, mimeType = mimeType) { out ->
                val uri = Uri.parse(sourceUri)
                val input = if (uri.scheme == "file") {
                    File(uri.path!!).inputStream()
                } else {
                    context.contentResolver.openInputStream(uri)
                }
                input?.use { it.copyTo(out) } ?: throw IOException("Kaynak dosya açılamadı: $sourceUri")
            }.also { generatedUri = it }
            
            Result.success(generatedUri)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AppError.ExportFailed(e))
        }
    }

    // ── Ortak yazma yardımcısı ────────────────────────────────────────────────

    /**
     * API 29+ → MediaStore ile ana dizine yazar (izin gerektirmez).
     * API ≤ 28 → WRITE_EXTERNAL_STORAGE ile doğrudan yazar.
     *
     * @return Üretilen dosyanın URI string'i.
     */
    private fun writePublic(
        fileName: String,
        mimeType: String,
        writer: (OutputStream) -> Unit
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(fileName, mimeType, writer)
        } else {
            writeLegacy(fileName, writer)
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun writeViaMediaStore(
        fileName: String,
        mimeType: String,
        writer: (OutputStream) -> Unit
    ): String {
        val folder = folderName()
        val isImage = mimeType.startsWith("image/")
        val relativePath = if (isImage) {
            Environment.DIRECTORY_PICTURES + "/$folder/"
        } else {
            Environment.DIRECTORY_DOWNLOADS + "/$folder/"
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        // Dosya türüne göre uygun MediaStore koleksiyonunu seç
        val collection = if (isImage) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }

        val uri = context.contentResolver.insert(collection, values)
            ?: throw IOException("MediaStore insert başarısız: $fileName")

        try {
            context.contentResolver.openOutputStream(uri)?.use(writer)
                ?: throw IOException("OutputStream açılamadı")
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
        } catch (e: Exception) {
            context.contentResolver.delete(uri, null, null)
            throw e
        }

        return uri.toString()
    }

    private fun writeLegacy(fileName: String, writer: (OutputStream) -> Unit): String {
        @Suppress("DEPRECATION")
        val dir = File(Environment.getExternalStorageDirectory(), folderName()).apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use(writer)
        return Uri.fromFile(file).toString()
    }

    // ── PDF üretimi ───────────────────────────────────────────────────────────

    private fun writePdfToStream(text: String, outputStream: OutputStream) {
        val document = PdfDocument()
        try {
            val paint = TextPaint().apply {
                isAntiAlias = true
                typeface = Typeface.DEFAULT
                textSize = PDF_TEXT_SIZE
                color = Color.BLACK
            }
            val textWidth = (PDF_PAGE_WIDTH - 2 * PDF_MARGIN).toInt()
            val usableHeight = PDF_PAGE_HEIGHT - 2 * PDF_MARGIN

            var pageNumber = 1
            var page = document.startPage(newPageInfo(pageNumber))

            fun startNewPage() {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(newPageInfo(pageNumber))
            }

            val segments = text.split(ExportRepository.PAGE_SEPARATOR)
            segments.forEachIndexed { segmentIndex, segment ->
                if (segmentIndex > 0) startNewPage()

                val layout = StaticLayout.Builder
                    .obtain(segment, 0, segment.length, paint, textWidth)
                    .setLineSpacing(PDF_LINE_SPACING_EXTRA, PDF_LINE_SPACING_MULT)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setIncludePad(false)
                    .build()

                var lineIndex = 0
                while (lineIndex < layout.lineCount) {
                    val rangeTop = layout.getLineTop(lineIndex)
                    var endLine = lineIndex
                    while (endLine < layout.lineCount &&
                        layout.getLineBottom(endLine) - rangeTop <= usableHeight
                    ) { endLine++ }
                    if (endLine == lineIndex) endLine = lineIndex + 1

                    val canvas = page.canvas
                    canvas.save()
                    canvas.clipRect(PDF_MARGIN, PDF_MARGIN, PDF_MARGIN + textWidth, PDF_MARGIN + usableHeight)
                    canvas.translate(PDF_MARGIN, PDF_MARGIN - rangeTop)
                    layout.draw(canvas)
                    canvas.restore()

                    lineIndex = endLine
                    if (lineIndex < layout.lineCount) startNewPage()
                }
            }

            document.finishPage(page)
            document.writeTo(outputStream)
        } finally {
            document.close()
        }
    }

    private fun newPageInfo(pageNumber: Int): PdfDocument.PageInfo =
        PdfDocument.PageInfo.Builder(PDF_PAGE_WIDTH.toInt(), PDF_PAGE_HEIGHT.toInt(), pageNumber).create()

    companion object {
        private const val PDF_PAGE_WIDTH = 595f
        private const val PDF_PAGE_HEIGHT = 842f
        private const val PDF_MARGIN = 40f
        private const val PDF_TEXT_SIZE = 12f
        private const val PDF_LINE_SPACING_EXTRA = 4f
        private const val PDF_LINE_SPACING_MULT = 1f
    }
}
