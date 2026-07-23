package com.gokcank.optidoc.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gokcank.optidoc.data.local.AppDatabase
import com.gokcank.optidoc.data.local.DocumentDao
import com.gokcank.optidoc.data.local.FolderDao
import com.gokcank.optidoc.data.local.PageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room veritabanını ve DAO'larını Hilt grafiğine sağlayan modül.
 *
 * ## Scope kararları
 * - [AppDatabase] → [@Singleton]: Veritabanı bağlantısı pahalıdır;
 *   uygulama boyunca tek bir örnek yeterlidir.
 * - [DocumentDao] / [PageDao] / [FolderDao] → unscoped: DAO'lar yalnızca veritabanı
 *   referansını taşır ve durumsuzdur; her injection noktasında
 *   yeniden oluşturulmaları ücretsizdir.
 *
 * ## Migration
 * Şema değişikliklerinde .addMigrations(Migration(from, to) { ... })
 * zinciriyle migration ekleyin. fallbackToDestructiveMigration eklenmeyecek.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
            )
            database.execSQL(
                "ALTER TABLE `documents` ADD COLUMN `folderId` INTEGER"
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "belge_tarayici.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideDocumentDao(db: AppDatabase): DocumentDao = db.documentDao()

    @Provides
    fun providePageDao(db: AppDatabase): PageDao = db.pageDao()

    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()
}
