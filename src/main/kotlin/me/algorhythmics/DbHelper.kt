package me.algorhythmics

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val NAME = "data.db"
private const val VERSION = 1

class DbHelper(ctx: Context) : SQLiteOpenHelper(ctx, NAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
		db.execSQL("""CREATE TABLE presets(
			name TEXT PRIMARY KEY,
			enabled INTEGER NOT NULL DEFAULT 0,
			filterEnabled INTEGER NOT NULL DEFAULT 0,
			sfxEnabled INTEGER NOT NULL DEFAULT 0,
			eqEnabled INTEGER NOT NULL DEFAULT 0,
			bands TEXT NOT NULL DEFAULT '',
			stereoWidth REAL NOT NULL DEFAULT 0.0,
			tonalBalance REAL NOT NULL DEFAULT 0.0,
			loudness REAL NOT NULL DEFAULT 0.0,
			usecase INTEGER NOT NULL DEFAULT 1,
			device INTEGER NOT NULL DEFAULT -1
		)""")
		db.execSQL("INSERT INTO presets(name) VALUES('internal')")
		db.execSQL("INSERT INTO presets(name) VALUES('headphones')")
    }

    override fun onUpgrade(p0: SQLiteDatabase, p1: Int, p2: Int) {
    }
}
