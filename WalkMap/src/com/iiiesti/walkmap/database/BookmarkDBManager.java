package com.iiiesti.walkmap.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.iiiesti.walkmap.R;

public class BookmarkDBManager {
	/* Constants */
	private static final String D_TAG = "BOOKMARKDB";
	private static final String DB_NAME = "bookmark.db";
	private static final int DB_VERSION = 1;
	private static final String TB_SIMPLE_POINTS = "simple_points";

	public static final int CCOLUMN_SPOINT_INT_ID = 0;
	public static final int CCOLUMN_SPOINT_STR_NAME = 1;
	public static final int CCOLUMN_SPOINT_STR_INFO = 2;
	public static final int CCOLUMN_SPOINT_INT_LAT = 3;
	public static final int CCOLUMN_SPOINT_INT_LON = 4;
	public static final int CCOLUMN_SPOINT_INT_ADDR_LAT = 5;
	public static final int CCOLUMN_SPOINT_INT_ADDR_LON = 6;
	/*mangreen MODIFY*/
	public static final int CCOLUMN_SPOINT_INT_FLOOR = 7;
	public static final int CCOLUMN_SPOINT_STR_BUILD_ID = 8;
	
	public static final String SPOINT_FIELD_ID = "_id";
	public static final String SPOINT_FIELD_NAME = "name";
	public static final String SPOINT_FIELD_INFO = "info";
	public static final String SPOINT_FIELD_LAT = "latitude";
	public static final String SPOINT_FIELD_LON = "longitude";
	public static final String SPOINT_FIELD_ADDR_LAT = "addr_latitude";
	public static final String SPOINT_FIELD_ADDR_LON = "addr_longitude";
	/*mangreen MODIFY*/
	public static final String SPOINT_FIELD_FLOOR = "floor";
	public static final String SPOINT_FIELD_BUILD_ID = "build_id";
	
	/* Resources */
	private Context mContext;
	private DBOpenHelper mDBOpenHelper;
	private SQLiteDatabase mDB;
	private boolean dbAvailable;
	
	/** Local DB OpenHelper */
	private static class DBOpenHelper extends SQLiteOpenHelper {
		private Context mContext;
		
		public DBOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(D_TAG, "DBOpenHelper - onCreate");
			db.execSQL("CREATE TABLE simple_points(_id INTEGER PRIMARY KEY,name TEXT,info TEXT,longitude INTEGER NOT NULL,latitude INTEGER NOT NULL,addr_longitude INTEGER NOT NULL,addr_latitude INTEGER NOT NULL, floor INTEGER NOT NULL, build_id TEXT)");
			
			// Insert the default data
			ContentValues cv = new ContentValues();
			cv.put(SPOINT_FIELD_NAME, mContext.getString(R.string.hint_on_direction_cur_location));
			cv.put(SPOINT_FIELD_INFO, mContext.getString(R.string.hint_on_direction_cur_location_info));
			cv.put(SPOINT_FIELD_LAT, 0);
			cv.put(SPOINT_FIELD_LON, 0);
			cv.put(SPOINT_FIELD_ADDR_LAT, 0);
			cv.put(SPOINT_FIELD_ADDR_LON, 0);
			/*mangreen MODIFY*/
			cv.put(SPOINT_FIELD_FLOOR, 0);

			db.insert(TB_SIMPLE_POINTS, SPOINT_FIELD_ID, cv);
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			Log.v(D_TAG, "DBOpenHelper - onOpen");
			super.onOpen(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion2) {
			Log.v(D_TAG, "DBOpenHelper - onUpgrade");
			db.execSQL("DROP TABLE IF EXISTS " + TB_SIMPLE_POINTS);
			onCreate(db);
		}
	}
	
	/** Constructor */
	public BookmarkDBManager(Context context) 
	{
		dbAvailable = false;
		mContext = context;
	}
	
	/** Interfaces */
	public BookmarkDBManager open() throws SQLiteException {
		mDBOpenHelper = new DBOpenHelper(mContext);
		mDB = mDBOpenHelper.getReadableDatabase();
		return this;
	}
	
	public void setAvailable(boolean status)
	{
		dbAvailable = status;
	}
	
	public boolean availability()
	{
		return dbAvailable;
	}
	
	public void dataClear()
	{
		mDBOpenHelper.onUpgrade(mDB, DB_VERSION, DB_VERSION);
	}
	
	public void close() {
		Log.d(D_TAG, "BookmarkDBManager: DB closed");
		mDBOpenHelper.close();
	}
	
	/** Query interface */
	
	public String SPOINT_countInfo() {
		SQLiteStatement simplePoint = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_SIMPLE_POINTS);
		return "Available Information:\n\n[ " + TB_SIMPLE_POINTS + " ]  -  " + simplePoint.simpleQueryForString() + "\n";
	}
	
	public long SPOINT_count() {
		SQLiteStatement simplePoint = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_SIMPLE_POINTS);
		return simplePoint.simpleQueryForLong();
	}
	
	public long SPOINT_insert(String name, String info, int latE6, int lonE6, int addrLatE6, int addrLonE6, int floor, String buildId)
	{
		ContentValues cv = new ContentValues();
		cv.put(SPOINT_FIELD_NAME, name);
		cv.put(SPOINT_FIELD_INFO, info);
		cv.put(SPOINT_FIELD_LAT, latE6);
		cv.put(SPOINT_FIELD_LON, lonE6);
		cv.put(SPOINT_FIELD_ADDR_LAT, addrLatE6);
		cv.put(SPOINT_FIELD_ADDR_LON, addrLonE6);
		cv.put(SPOINT_FIELD_FLOOR, floor);
		cv.put(SPOINT_FIELD_BUILD_ID, buildId);
		return mDB.insert(TB_SIMPLE_POINTS, SPOINT_FIELD_ID, cv);
	}
	
	public void SPOINT_update(long _id, String name, String info, int latE6, int lonE6, int addrLatE6, int addrLonE6, int floor, String buildId)
	{
		ContentValues cv = new ContentValues();
		cv.put(SPOINT_FIELD_NAME, name);
		cv.put(SPOINT_FIELD_INFO, info);
		cv.put(SPOINT_FIELD_LAT, latE6);
		cv.put(SPOINT_FIELD_LON, lonE6);
		cv.put(SPOINT_FIELD_ADDR_LAT, addrLatE6);
		cv.put(SPOINT_FIELD_ADDR_LON, addrLonE6);
		cv.put(SPOINT_FIELD_FLOOR, floor);
		cv.put(SPOINT_FIELD_BUILD_ID, buildId);
		mDB.update(TB_SIMPLE_POINTS, cv, "_id = ?", new String[] {Long.toString(_id)});
	}
	
	public void SPOINT_delete(long _id)
	{
		mDB.delete(TB_SIMPLE_POINTS, "_id = ?", new String[] {Long.toString(_id)});
	}
	
	public Cursor SPOINT_select()
	{
		Log.v(D_TAG, "SPOINT_getAll");
		
		Cursor cursor;
		
		try {
			cursor =  mDB.query(TB_SIMPLE_POINTS, new String[]{
					SPOINT_FIELD_ID, 
					SPOINT_FIELD_NAME, 
					SPOINT_FIELD_INFO, 
					SPOINT_FIELD_LAT, 
					SPOINT_FIELD_LON,
					SPOINT_FIELD_ADDR_LAT,
					SPOINT_FIELD_ADDR_LON,
					SPOINT_FIELD_FLOOR,
					SPOINT_FIELD_BUILD_ID}, 
				null, null, null, null, null);
			return cursor;
		} catch (SQLiteException e) {
			Log.e(D_TAG, e.getMessage());
			return null;
		}
	}
}
