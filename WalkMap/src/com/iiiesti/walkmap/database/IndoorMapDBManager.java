package com.iiiesti.walkmap.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IndoorMapDBManager extends SQLiteOpenHelper{
	/* Constants */
//	private static final String TAG = "IdrMapDB";	// Tony COMMENTED @ 1st merge
	private static final String DB_NAME = "indoormap.db";
	private static final int DB_VERSION = 1;
	private static final String TB_III = "iii";
	private static final String TB_MRT_HSP = "mrt_hsp";
	private static final String TB_MRT_NG = "mrt_ng";
	private static final String TB_MRT_TCH = "mrt_tch";
	private static final String TB_MRT_YC = "mrt_yc";
	private static final String[] TB_LIST = {"iii", "mrt_tch", "mrt_yc", "mrt_hsp", "mrt_ng"};
	
	public static final String SPOINT_FIELD_ID = "_id";
	public static final String SPOINT_FIELD_PICNAME = "picname";
	public static final String SPOINT_FIELD_FLOOR = "floor";
	public static final String SPOINT_FIELD_LEFT_LON = "left_lon";
	public static final String SPOINT_FIELD_TOP_LAT = "top_lat";
	public static final String SPOINT_FIELD_RIGHT_LON = "right_lon";
	public static final String SPOINT_FIELD_BOTTOM_LAT = "bottom_lat";
	public static final String SPOINT_FIELD_CONNECT = "connect";
	public static final String SPOINT_FIELD_INFO = "info";
	public static final String SPOINT_FIELD_FLOOR_NAME = "floor_name";
	public static final String SPOINT_FIELD_BUILD_ADDRES = "build_address";
	
	public IndoorMapDBManager(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE " + TB_III + "(_id INTEGER PRIMARY KEY, " +
												"picname TEXT NOT NULL, " +
												"floor INTEGER NOT NULL, " +
												"left_lon INTEGER NOT NULL, " +
												"top_lat INTEGER NOT NULL, " +
												"right_lon INTEGER NOT NULL, " +
												"bottom_lat INTEGER NOT NULL, " +
												"connect TEXT, " +
												"info TEXT, " +
												"floor_name TEXT, " +
												"build_address TEXT);");
		
		db.execSQL("CREATE TABLE " + TB_MRT_HSP + "(_id INTEGER PRIMARY KEY, " +
				"picname TEXT NOT NULL, " +
				"floor INTEGER NOT NULL, " +
				"left_lon INTEGER NOT NULL, " +
				"top_lat INTEGER NOT NULL, " +
				"right_lon INTEGER NOT NULL, " +
				"bottom_lat INTEGER NOT NULL, " +
				"connect TEXT, " +
				"info TEXT, " +
				"floor_name TEXT, " +
				"build_address TEXT);");
		
		db.execSQL("CREATE TABLE " + TB_MRT_NG + "(_id INTEGER PRIMARY KEY, " +
				"picname TEXT NOT NULL, " +
				"floor INTEGER NOT NULL, " +
				"left_lon INTEGER NOT NULL, " +
				"top_lat INTEGER NOT NULL, " +
				"right_lon INTEGER NOT NULL, " +
				"bottom_lat INTEGER NOT NULL, " +
				"connect TEXT, " +
				"info TEXT, " +
				"floor_name TEXT, " +
				"build_address TEXT);");
		
		db.execSQL("CREATE TABLE " + TB_MRT_TCH + "(_id INTEGER PRIMARY KEY, " +
				"picname TEXT NOT NULL, " +
				"floor INTEGER NOT NULL, " +
				"left_lon INTEGER NOT NULL, " +
				"top_lat INTEGER NOT NULL, " +
				"right_lon INTEGER NOT NULL, " +
				"bottom_lat INTEGER NOT NULL, " +
				"connect TEXT, " +
				"info TEXT, " +
				"floor_name TEXT, " +
				"build_address TEXT);");
		
		db.execSQL("CREATE TABLE " + TB_MRT_YC + "(_id INTEGER PRIMARY KEY, " +
				"picname TEXT NOT NULL, " +
				"floor INTEGER NOT NULL, " +
				"left_lon INTEGER NOT NULL, " +
				"top_lat INTEGER NOT NULL, " +
				"right_lon INTEGER NOT NULL, " +
				"bottom_lat INTEGER NOT NULL, " +
				"connect TEXT, " +
				"info TEXT, " +
				"floor_name TEXT, " +
				"build_address TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TB_III);
		db.execSQL("DROP TABLE IF EXISTS " + TB_MRT_HSP);
		db.execSQL("DROP TABLE IF EXISTS " + TB_MRT_NG);
		db.execSQL("DROP TABLE IF EXISTS " + TB_MRT_TCH);
		db.execSQL("DROP TABLE IF EXISTS " + TB_MRT_YC);
		onCreate(db);
	}
	
	public Cursor getAll(String buildID){

		SQLiteDatabase db = this.getReadableDatabase();
		//Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		Cursor cursor = db.rawQuery("SELECT * FROM " + seekTable(buildID) + " ORDER BY floor DESC", null);
		return cursor;
	}
	
	public Cursor selectDownstairs(String buildID, int floor){

		SQLiteDatabase db = this.getReadableDatabase();
		//Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		Cursor cursor = db.query(seekTable(buildID), new String[]{"_id", "picname", "floor", "left_lon", "top_lat", "right_lon", "bottom_lat", "connect", "info", "floor_name", "build_address"}, 
													"floor < " + floor, null, null, null, "floor");
		return cursor;
	}
	
	public Cursor selectUpstairs(String buildID, int floor){

		SQLiteDatabase db = this.getReadableDatabase();
		//Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		Cursor cursor = db.query(seekTable(buildID), new String[]{"_id", "picname", "floor", "left_lon", "top_lat", "right_lon", "bottom_lat", "connect", "info", "floor_name", "build_address"}, 
													"floor > " + floor, null, null, null, "floor");
		return cursor;
	}
	
	public Cursor selectMapByFloor(String buildID, int floor){
		SQLiteDatabase db = this.getReadableDatabase();	
		
		//Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		Cursor cursor = db.query(seekTable(buildID), new String[]{"_id", "picname", "floor", "left_lon", "top_lat", "right_lon", "bottom_lat", "connect", "info", "floor_name", "build_address"}, 
													"floor = " + floor, null, null, null, null);
		return cursor;
	}
	
	public Cursor selectMapByConnect(String buildID, String connect){
		SQLiteDatabase db = this.getReadableDatabase();

		//Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		Cursor cursor = db.query(seekTable(buildID), new String[]{"_id", "picname", "floor", "left_lon", "top_lat", "right_lon", "bottom_lat", "connect", "info", "floor_name", "build_address"}, 
													"connect LIKE '" + connect + "'", null, null, null, null);
		return cursor;
	}
	
	private String seekTable(String buildID){
		for(int i=0; i<TB_LIST.length; i++){
			if(TB_LIST[i].equals(buildID)){
				return TB_LIST[i];
			}
		}
		return "";
	}
	
	public boolean insertData(){
		SQLiteDatabase db = this.getWritableDatabase();

		db.execSQL("INSERT INTO " + TB_III +" VALUES(1, 'iii_13f', 13, 121554277, 25058938, 121554969, 25058447, null, '資策會智慧所, 天空傳媒', '民生科技大樓13F', '105 台北市松山區民生東路四段133號13樓');");
		db.execSQL("INSERT INTO " + TB_III +" VALUES(2, 'iii_1f', 1, 121554277, 25058938, 121554969, 25058447, 'out', '大廳, 資策會網多所, 伯朗咖啡, 全聯, 精忠區民活動中心', '民生科技大樓1F', '105 台北市松山區民生東路四段133號1樓');");
		
		db.execSQL("INSERT INTO " + TB_MRT_HSP +" VALUES(1, 'mrt_hsp_b1', -1, 121580346, 25045745, 121584477, 25043675, 'out', '捷運大廳', '捷運後山埤站B1', '115 台北市南港區忠孝東路6段2號');");
		db.execSQL("INSERT INTO " + TB_MRT_HSP +" VALUES(2, 'mrt_hsp_b2', -2, 121580346, 25045745, 121584477, 25043675, 'mass', '捷運月台', '捷運後山埤站B2', '115 台北市南港區忠孝東路6段2號');");
		
		db.execSQL("INSERT INTO " + TB_MRT_NG +" VALUES(1, 'mrt_ng_b1', -1, 121607057, 25052604, 121608854, 25051704, 'out', '捷運大廳', '捷運南港站B1', '115 台北市南港區忠孝東路7段380號');");
		db.execSQL("INSERT INTO " + TB_MRT_NG +" VALUES(2, 'mrt_ng_b2', -2, 121607057, 25052604, 121608854, 25051704, 'mass', '捷運月台', '捷運南港站B2', '115 台北市南港區忠孝東路7段380號');");
		
		db.execSQL("INSERT INTO " + TB_MRT_TCH +" VALUES(1, 'mrt_tch_b1', -1, 121565002, 25041724, 121567266, 25040589, 'out', '捷運大廳', '捷運市政府站B1', '110 台北市信義區忠孝東路5段2號');");
		db.execSQL("INSERT INTO " + TB_MRT_TCH +" VALUES(2, 'mrt_tch_b2', -2, 121565002, 25041724, 121567266, 25040589, 'mass', '捷運月台', '捷運市政府站B2', '110 台北市信義區忠孝東路5段2號');");
		
		db.execSQL("INSERT INTO " + TB_MRT_YC +" VALUES(1, 'mrt_yc_b1', -1, 121575010, 25041310, 121576955, 25040337, 'out', '捷運大廳', '捷運永春站B1', '110 台北市信義區忠孝東路5段455號');");
		db.execSQL("INSERT INTO " + TB_MRT_YC +" VALUES(2, 'mrt_yc_b2', -2, 121575010, 25041310, 121576955, 25040337, 'mass', '捷運月台', '捷運永春站B2', '110 台北市信義區忠孝東路5段455號');");
		return true;
	}
}
