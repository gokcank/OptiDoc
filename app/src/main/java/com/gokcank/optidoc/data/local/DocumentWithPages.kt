package com.gokcank.optidoc.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Bir belgeyi sayfalarıyla birlikte döndüren Room projection nesnesi.
 *
 * @Relation, Room'un ayrı bir JOIN sorgusu çalıştırmasını sağlar;
 * bu nedenle bu nesneyi döndüren sorgular @Transaction ile işaretlenmelidir.
 */
data class DocumentWithPages(
    @Embedded
    val document: DocumentEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val pages: List<PageEntity>
)
