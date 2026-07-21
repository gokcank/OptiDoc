package com.gokcank.optidoc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Klasörleri temsil eden Room entity'si.
 *
 * @property id        Otomatik artan birincil anahtar.
 * @property name      Klasör adı.
 * @property createdAt Oluşturulma zamanı (epoch millis, UTC).
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Long
)
