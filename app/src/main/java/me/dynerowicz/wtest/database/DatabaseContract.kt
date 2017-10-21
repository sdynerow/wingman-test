package me.dynerowicz.wtest.database

import android.provider.BaseColumns

/** Contract describing the table used to encode the postal codes. */
object DatabaseContract {
    const val TABLE_NAME = "postalCodes"
    const val COLUMN_POSTAL_CODE = "postalCode"
    const val COLUMN_EXTENSION = "extension"
    const val COLUMN_LOCALITY = "locality"

    const val INDEX_POSTAL_CODE = "IDX_POSTAL_CODE"

    // Fields as they appear in the CSV to be imported
    const val CSV_NUMBER_OF_FIELDS = 14
    const val CSV_POSTAL_CODE = "cod_postal"
    const val CSV_EXTENSION = "extensao_cod_postal"
    const val CSV_LOCALITY = "localidade"

    const val CREATE_TABLE_STATEMENT = "CREATE TABLE ${DatabaseContract.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "${DatabaseContract.COLUMN_POSTAL_CODE} INTEGER, " +
            "${DatabaseContract.COLUMN_EXTENSION} INTEGER, " +
            "${DatabaseContract.COLUMN_LOCALITY} TEXT " +
            ")"

    const val CREATE_INDEX_STATEMENT = "CREATE INDEX IF NOT EXISTS ${DatabaseContract.INDEX_POSTAL_CODE} " +
            "ON ${DatabaseContract.TABLE_NAME}(${DatabaseContract.COLUMN_POSTAL_CODE})"

    const val DROP_TABLE_STATEMENT = "DROP TABLE IF EXISTS ${DatabaseContract.TABLE_NAME}"

    const val INSERT_QUERY = "INSERT into ${DatabaseContract.TABLE_NAME} (" +
            "${DatabaseContract.COLUMN_POSTAL_CODE}, " +
            "${DatabaseContract.COLUMN_EXTENSION}, " +
            DatabaseContract.COLUMN_LOCALITY +
        ") VALUES (?,?,?)"
}
