package me.algorhythmics.diracui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val NAME = "data.db"
private const val VERSION = 2

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
			device INTEGER NOT NULL DEFAULT -1,
			gamemodeEnabled INTEGER NOT NULL DEFAULT 0
		)""")
		db.execSQL("INSERT INTO presets(name, bands) VALUES('internal', '0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0')") // 10 bands
		db.execSQL("INSERT INTO presets(name, device) VALUES('headphones', 1)")
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
		for (i in (old + 1)..new) {
			when (i) {
				2 -> {
					db.execSQL("UPDATE presets SET bands = '0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0' WHERE name = 'internal'")
					db.execSQL("ALTER TABLE presets ADD COLUMN gamemodeEnabled INTEGER NOT NULL DEFAULT 0")
				}
			}
		}
    }
}
