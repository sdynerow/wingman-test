package me.dynerowicz.wtest.database

import android.database.sqlite.SQLiteDatabase
import me.dynerowicz.wtest.presenter.PostalCodeRow
import me.dynerowicz.wtest.tasks.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLog
import java.io.File

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class ScrollingCursorTest : CsvImportListener, DatabaseQueryListener, ScrollingCursorObserver {
    companion object {
        const val ENTRY_COUNT = 314772L
    }

    private lateinit var databaseHelper: DatabaseHelper
    lateinit var database: SQLiteDatabase

    lateinit var scroller: ScrollingCursor
    var currentRow = 0
    var desiredRow = PostalCodeRow(2695, 650, "São João da Talha")
    var desiredRowFound = false

    @Before
    fun setUp() {
        ShadowLog.stream = System.out

        databaseHelper = DatabaseHelper(RuntimeEnvironment.application)
        database = databaseHelper.writableDatabase

        val csvFile = File("app/src/test/resources/codigos_postais-$ENTRY_COUNT.csv")
        val asyncTask = CsvImporterTask(database, csvFile, this)
        asyncTask.execute()
        ShadowApplication.runBackgroundTasks()

        val (importedEntries, invalidEntries) = asyncTask.get()

        Assert.assertTrue(importedEntries == ENTRY_COUNT)
        Assert.assertTrue(invalidEntries == 0L)
    }

    @After
    fun tearDown() {
        database.close()
        databaseHelper.close()
    }

    override fun onMoreResultsAvailable(count: Int) {
        while (currentRow < scroller.count()) {
            if (desiredRow == scroller.getPostalCodeRow(currentRow))
                desiredRowFound = true
            currentRow += 1
        }
    }

    override fun onEndOfResults() {
        Assert.assertTrue(desiredRowFound)
    }
    
    private fun databaseContainsDesiredRowFor(vararg inputs: String) {
        scroller = ScrollingCursor(database, this, inputs = *inputs)
        scroller.initialize()
    }

    @Test
    fun databaseContainsDesiredRowForPostalCode2695() =
        databaseContainsDesiredRowFor("2695")

    @Test
    fun databaseContainsDesiredRowForPostalCode2695Extension650() =
        databaseContainsDesiredRowFor("2695", "650")

    @Test
    fun databaseContainsDesiredRowForKeywordsSaoAndJoao() =
        databaseContainsDesiredRowFor("São", "João")

    @Test
    fun databaseContainsDesiredRowForKeywords_sAo_joA_da_Talha() =
        databaseContainsDesiredRowFor("sAo", "joA", "da", "TaLH")

    @Test
    fun databaseContainsDesiredRowForKeywords_sao_talha() =
        databaseContainsDesiredRowFor("sao", "talha")

    @Test
    fun databaseContainsDesiredRowForKeywords_talh_joa() =
        databaseContainsDesiredRowFor("talh", "joa")

    @Test
    fun databaseContainsDesiredRowForKeywords_joao_talha() =
        databaseContainsDesiredRowFor("joao", "talha")
}