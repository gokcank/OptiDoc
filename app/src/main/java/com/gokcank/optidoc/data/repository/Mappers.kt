package com.gokcank.optidoc.data.repository

import com.gokcank.optidoc.data.local.DocumentDao
import com.gokcank.optidoc.data.local.DocumentEntity
import com.gokcank.optidoc.data.local.OutputFormat
import com.gokcank.optidoc.data.local.PageDao
import com.gokcank.optidoc.data.local.PageEntity
import com.gokcank.optidoc.domain.model.DocumentOutput
import com.gokcank.optidoc.domain.model.DocumentPage
import com.gokcank.optidoc.domain.model.ExportFormat
import com.gokcank.optidoc.domain.model.ScannedDocument

import com.gokcank.optidoc.data.local.FolderEntity
import com.gokcank.optidoc.domain.model.Folder

// ── Entity → Domain ──────────────────────────────────────────────────────────

internal fun DocumentEntity.toDomain(): ScannedDocument = ScannedDocument(
    id        = id,
    title     = title,
    createdAt = createdAt,
    pageCount = pageCount,
    output    = when (outputFormat) {
        OutputFormat.DIRECT_PDF -> DocumentOutput.DirectPdf(outputUri!!)
        OutputFormat.OCR_TXT    -> DocumentOutput.OcrExport(ExportFormat.TXT, outputUri!!)
        OutputFormat.OCR_PDF    -> DocumentOutput.OcrExport(ExportFormat.PDF, outputUri!!)
        null                    -> DocumentOutput.None
    },
    folderId  = folderId
)

internal fun PageEntity.toDomain(): DocumentPage = DocumentPage(
    id          = id,
    documentId  = documentId,
    pageNumber  = pageNumber,
    imageUri    = imageUri,
    ocrText     = ocrText
)

internal fun FolderEntity.toDomain(): Folder = Folder(
    id        = id,
    name      = name,
    createdAt = createdAt
)

// ── Domain → Entity ──────────────────────────────────────────────────────────

internal fun ScannedDocument.toEntity(): DocumentEntity = DocumentEntity(
    id           = id,
    title        = title,
    createdAt    = createdAt,
    pageCount    = pageCount,
    outputFormat = when (val o = output) {
        is DocumentOutput.None      -> null
        is DocumentOutput.DirectPdf -> OutputFormat.DIRECT_PDF
        is DocumentOutput.OcrExport -> if (o.format == ExportFormat.TXT) OutputFormat.OCR_TXT
                                       else OutputFormat.OCR_PDF
    },
    outputUri = when (val o = output) {
        is DocumentOutput.None      -> null
        is DocumentOutput.DirectPdf -> o.uri
        is DocumentOutput.OcrExport -> o.uri
    },
    folderId = folderId
)

internal fun DocumentPage.toEntity(): PageEntity = PageEntity(
    id         = id,
    documentId = documentId,
    pageNumber = pageNumber,
    imageUri   = imageUri,
    ocrText    = ocrText
)

internal fun Folder.toEntity(): FolderEntity = FolderEntity(
    id        = id,
    name      = name,
    createdAt = createdAt
)
