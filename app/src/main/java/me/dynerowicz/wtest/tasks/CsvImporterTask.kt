package me.dynerowicz.wtest.tasks

import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.util.Log
import com.opencsv.CSVReader
import me.dynerowicz.wtest.database.DatabaseContract
import java.io.File
import java.io.IOException

class CsvImporterTask(
        private val database: SQLiteDatabase,
        private val csvFile: File,
        private val importListener: CsvImportListener?
) : AsyncTask<Unit, Int, Pair<Long, Long>>() {

    private fun determineEntryCount(csvFile: File): Long {
        val reader = csvFile.bufferedReader()

        var numberOfLines = 0L
        try {
            while (reader.readLine() != null) {
                numberOfLines += 1L
            }
        } catch (ioe: IOException) {
            Log.e(TAG, "Error while pre-processing CSV file: $ioe")
            numberOfLines = -1L
        } finally {
            reader.close()
        }

        return numberOfLines
    }

    override fun onPostExecute(result: Pair<Long, Long>) {
        super.onPostExecute(result)
        importListener?.onImportComplete(result)
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        if(values.isNotEmpty()) {
            val progressUpdate = values.first()
            if(progressUpdate != null)
                importListener?.onImportProgressUpdate(progressUpdate)
        }
    }

    override fun onCancelled() {
        importListener?.onImportCancelled()
    }

    override fun doInBackground(vararg p0: Unit?): Pair<Long, Long> {
        val csvFileReader = CSVReader(csvFile.bufferedReader())
        val parserIterator = csvFileReader.iterator()

        val totalNumberOfEntries = determineEntryCount(csvFile)

        var currentProgress = 0
        var importedEntriesCount = 0L
        var invalidEntriesCount = 0L

        var lineNumber = 0
        val fieldNames: Array<String>

        if (parserIterator.hasNext()) {
            fieldNames = parserIterator.next()
            lineNumber += 1

            var validCsvHeader = fieldNames.size == DatabaseContract.CSV_NUMBER_OF_FIELDS

            if(validCsvHeader) {
                val localityIndex = fieldNames.indexOf(DatabaseContract.CSV_LOCALITY)
                val postalCodeIndex = fieldNames.indexOf(DatabaseContract.CSV_POSTAL_CODE)
                val extensionIndex = fieldNames.indexOf(DatabaseContract.CSV_EXTENSION)

                validCsvHeader = localityIndex != -1 && postalCodeIndex != -1 && extensionIndex != -1

                if (validCsvHeader) {
                    importListener?.onImportProgressUpdate(0)

                    var fields: Array<String>

                    database.beginTransaction()

                    while (parserIterator.hasNext() && !isCancelled) {
                        fields = parserIterator.next()
                        lineNumber += 1

                        if (fields.size == DatabaseContract.CSV_NUMBER_OF_FIELDS) {
                            try {
                                val locality = fields[localityIndex]
                                val postalCode = fields[postalCodeIndex].toLong()
                                val extension = fields[extensionIndex].toLong()
                                insertPostalCode(postalCode, extension, locality)
                            } catch (nfe: NumberFormatException) {
                                Log.v(TAG, "Error at line $lineNumber: $nfe")
                            }
                            importedEntriesCount += 1

                            if (importedEntriesCount.rem(10000) == 0L) {
                                database.setTransactionSuccessful()
                                database.endTransaction()
                                database.beginTransaction()
                            }

                            val progressUpdate = (importedEntriesCount * 100L / totalNumberOfEntries).toInt()

                            importListener?.let {
                                if (currentProgress != progressUpdate) {
                                    Log.v(TAG, "ImportProgressUpdate: $importedEntriesCount / $totalNumberOfEntries entries [$progressUpdate%] (invalid=$invalidEntriesCount)")
                                    currentProgress = progressUpdate
                                    publishProgress(currentProgress)
                                }
                            }

                        } else {
                            Log.e(TAG, "Ill-formed CSV file@line $lineNumber]: $fieldNames")
                            invalidEntriesCount += 1
                        }
                    }

                    database.setTransactionSuccessful()
                    database.endTransaction()
                }
            } else
                Log.e(TAG, "Invalid CSV Header: $fieldNames")

            if (invalidEntriesCount > 0)
                Log.e(TAG, "Invalid entries found in CSV files: $invalidEntriesCount")
        }

        csvFileReader.close()

        return Pair(importedEntriesCount, invalidEntriesCount)
    }

    // Database insertion operation
    private val insertStatement = database.compileStatement(DatabaseContract.INSERT_QUERY)
    private fun insertPostalCode(postalCode: Long, extension: Long, locality: String) =
        insertStatement.run {
            clearBindings()
            bindLong(1, postalCode)
            bindLong(2, extension)
            bindString(3, locality)
            execute()
            clearBindings()
        }

    companion object {
        const val TAG = "CsvImporterTask"
    }
}