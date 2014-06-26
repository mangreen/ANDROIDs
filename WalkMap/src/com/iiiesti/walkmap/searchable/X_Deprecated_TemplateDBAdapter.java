package com.iiiesti.walkmap.searchable;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class X_Deprecated_TemplateDBAdapter {
	/* Constants */
	private static final String DB_NAME = "template.db";
	private static final int DB_VERSION = 1;
	private static final String DB_TABLE = "test";
	private static final String DB_CREATE = 
		"CREATE TABLE " + DB_TABLE + " (" +
		"_id INTEGER PRIMARY KEY," +
		"name TEXT NOT NULL," +
		"lat6e INTEGER NOT NULL," +
		"lon6e INTEGER NOT NULL" +
		");";
		
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXIST " + DB_TABLE);
			onCreate(db);
		}
		
	}
	
	private Context mContext = null;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	
	/** Constructor */
	public X_Deprecated_TemplateDBAdapter(Context context) {
		mContext = context;
	}
	
	public X_Deprecated_TemplateDBAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(mContext);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public Cursor selectAll() {
		return db.rawQuery("SELECT * FROM " + DB_TABLE, null);
	}
}
