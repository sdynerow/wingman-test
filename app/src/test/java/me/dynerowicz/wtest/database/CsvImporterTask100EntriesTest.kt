package me.dynerowicz.wtest.database

import android.database.sqlite.SQLiteDatabase
import me.dynerowicz.wtest.tasks.CsvImporterTask
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowApplication
import java.io.File

@RunWith(RobolectricTestRunner::class)
class CsvImporterTask100EntriesTest {

    private lateinit var databaseHelper: DatabaseHelper
    lateinit var database: SQLiteDatabase

    @Before
    fun setUp() {
        databaseHelper = DatabaseHelper(RuntimeEnvironment.application)
        database = databaseHelper.writableDatabase

        val csvFile = File("app/src/test/resources/codigos_postais-100.csv")
        val asyncTask = CsvImporterTask(database, csvFile, null)
        asyncTask.execute()
        ShadowApplication.runBackgroundTasks()

        val (importedEntries, invalidEntries) = asyncTask.get()

        Assert.assertTrue(importedEntries == 100L)
        Assert.assertTrue(invalidEntries == 0L)
    }

    @After
    fun tearDown() {
        database.close()
        databaseHelper.close()
    }

    @Test
    fun testHas100EntriesInDatabase() {
        val query = "SELECT * FROM ${DatabaseContract.TABLE_NAME}"
        val cursor = database.rawQuery(query, null)
        Assert.assertTrue(cursor.count == 100)
        cursor.close()
    }
}