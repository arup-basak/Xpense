package com.arup.xpense

import models.TransactionModel
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHandler(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val query = """
            CREATE TABLE $TABLE_NAME (
                $ID_COL  INTEGER PRIMARY KEY AUTOINCREMENT, 
                $TITLE_COL TEXT, 
                $AMOUNT_COL INTEGER,
                $TIME_COL BIGINT,
                $NOTE_COL TEXT                
            )
        """.trimIndent()
        db.execSQL(query)
    }

    fun addNewTransaction(
        transactionName: String?,
        transactionAmount: Int?,
        transactionNote: String?
    ) {
        val db = this.writableDatabase
        val values = ContentValues()

        val currentTime = getCurrentDateTime()

        values.put(TITLE_COL, transactionName)
        values.put(AMOUNT_COL, transactionAmount)
        values.put(TIME_COL, currentTime)
        values.put(NOTE_COL, transactionNote)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private const val DB_NAME = "APP_DATA"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "TRANSACTION_DATA"
        private const val ID_COL = "id"
        private const val TITLE_COL = "name"
        private const val AMOUNT_COL = "amount"
        private const val TIME_COL = "time"
        private const val NOTE_COL = "note"
    }


    fun readTransactions(
        fromTime: Long? = null,
        toTime: Long? = null,
        minimumAmount: Int? = 0,
        maximumAmount: Int? = Int.MAX_VALUE
    ): List<TransactionModel> {
        val db = this.readableDatabase

        val query = buildString {
            append("SELECT * FROM $TABLE_NAME")
            append(" WHERE $AMOUNT_COL >= $minimumAmount AND $AMOUNT_COL <= $maximumAmount")
            if (fromTime != null) {
                append(" AND $TIME_COL >= $fromTime")
            }
            if (toTime != null) {
                append(" AND $TIME_COL <= $toTime")
            }
        }


        val cursorTransactions = db.rawQuery(query, null)

        return cursorTransactions.use {
            generateSequence {
                if (it.moveToNext()) {
                    TransactionModel(
                        it.getInt(0),
                        it.getString(1),
                        it.getInt(2),
                        it.getLong(3),
                        it.getString(4)
                    )
                } else {
                    null
                }
            }.toList()
        }
    }

    private fun getCurrentDateTime(): Long {
        return System.currentTimeMillis()
    }
}
