package com.haselab.myservice


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private const val TAG = "DatabaseHelper"


class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	// クラス内のprivate定数を宣言するためにcompanion objectブロックとする。
	companion object {
		/**
		 * データベースファイル名の定数フィールド。
		 */
		private const val DATABASE_NAME = "MyServiceLocation.db"
		/**
		 * バージョン情報の定数フィールド。
		 */
		private const val DATABASE_VERSION = 3
	}

	override fun onCreate(db: SQLiteDatabase) {
		Log.v(TAG,"onCreate")
		// テーブル作成用SQL文字列の作成。
		val sb = StringBuilder()
		sb.append("CREATE TABLE location (")
		sb.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,")
		sb.append("time INTEGER,")
		sb.append("lat REAL,")
		sb.append("lon REAL")
		sb.append(");")
		val sql = sb.toString()

		// SQLの実行。
		db.execSQL(sql)
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		Log.v(TAG,"onUpgrade")
	}
}
