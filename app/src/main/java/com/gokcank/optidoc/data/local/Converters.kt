package com.gokcank.optidoc.data.local

import androidx.room.TypeConverter

/**
 * Room için TypeConverter'lar.
 *
 * - [OutputFormat] ↔ [String]: enum adı String olarak saklanır.
 *   Bilinmeyen değerler için null döner (ileriki sürümlerle uyumluluk).
 *
 * Uri alanları entity'lerde doğrudan [String] olarak tutulduğundan
 * ayrıca bir Uri TypeConverter'ına gerek yoktur.
 */
class Converters {

    @TypeConverter
    fun fromOutputFormat(format: OutputFormat?): String? = format?.name

    @TypeConverter
    fun toOutputFormat(value: String?): OutputFormat? =
        value?.let {
            runCatching { OutputFormat.valueOf(it) }.getOrNull()
        }
}
