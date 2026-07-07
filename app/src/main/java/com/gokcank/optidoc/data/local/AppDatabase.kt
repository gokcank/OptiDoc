package com.gokcank.optidoc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Uygulamanın tek Room veritabanı.
 *
 * ## Migrasyon politikası
 * [fallbackToDestructiveMigration] **kullanılmıyor**. Her şema değişikliğinde
 * `di/DatabaseModule.kt` içindeki builder'a bir Migration nesnesi eklenmelidir.
 *
 * ## Şema dışa aktarımı
 * `exportSchema = true` ile şema JSON dosyaları `app/schemas/` dizinine
 * yazılır; migration testlerinde kullanılabilir.
 */
@Database(
    entities = [
        DocumentEntity::class,
        PageEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun pageDao(): PageDao
}
