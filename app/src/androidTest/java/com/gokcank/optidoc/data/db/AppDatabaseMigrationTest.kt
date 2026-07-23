package com.gokcank.optidoc.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gokcank.optidoc.data.local.AppDatabase
import com.gokcank.optidoc.di.DatabaseModule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO documents (id, title, createdAt, pageCount, output_type) VALUES (1, 'Test Doc', 1000, 1, 'NONE')")
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, DatabaseModule.MIGRATION_1_2)

        val cursor = db.query("SELECT folderId FROM documents WHERE id = 1")
        cursor.moveToFirst()
        cursor.close()
    }
}
