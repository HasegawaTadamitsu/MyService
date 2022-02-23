package com.haselab.myservice

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*

private const val TAG = "DatabaseHelper"

class DatabaseHelper(context: Context) :

    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "MyServiceLocation.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.v(TAG, "onCreate")

        val sb = StringBuilder()
        sb.append("CREATE TABLE location (")
        sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,")
        sb.append("time INTEGER,")
        sb.append("lat REAL,")
        sb.append("lon REAL")
        sb.append(");")
        val sql = sb.toString()
        db.execSQL(sql)

        val sb2 = StringBuilder()
        sb2.append("CREATE TABLE UUID (")
        sb2.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,")
        sb2.append("time INTEGER,")
        sb2.append("db_ver INTEGER,")
        sb2.append("uuid TEXT")
        sb2.append(");")
        val sql2 = sb2.toString()
        db.execSQL(sql2)

        insertUUID(db, 1)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.v(TAG, "onUpgrade")
        insertUUID(db, newVersion)
    }

    private fun insertUUID(db: SQLiteDatabase, newVersion: Int) {
        Log.v(TAG, "start insertUUID")
        val sb = StringBuilder()
        val currentTimeMillis = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()
        Log.v(TAG, "insert $uuid")
        sb.append("INSERT INTO uuid ( time, db_ver, uuid) VALUES (")
        sb.append("$currentTimeMillis , 1, '")
        sb.append("${uuid}")
        sb.append("');")
        val sql = sb.toString()
        db.execSQL(sql)
    }

    fun getUUDI(): String {
        Log.v(TAG, "start getUUID")
        val db = this.readableDatabase
        val sql = "select uuid from uuid where db_ver = (select max(db_ver) from uuid) "
        val c = db.rawQuery(sql, null)
        c.moveToFirst()
        val uuid = c.getString(0)
        c.close()
        db.close()
        return uuid
    }
}
