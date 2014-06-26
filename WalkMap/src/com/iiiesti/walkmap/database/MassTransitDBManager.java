package com.iiiesti.walkmap.database;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Point;
import android.util.Log;

public class MassTransitDBManager {
	/* Constants */
	private static final String D_TAG = "MTDB";
	private static final String DB_NAME = "masstransit.db";
	private static final int DB_VERSION = 1;
	private static final String TB_PROVIDER = "provider";
	private static final String TB_LINEP = "line_p";
	private static final String TB_LINE = "line";
	private static final String TB_TICKET = "ticket";
	private static final String TB_STOPL = "stop_l";
	private static final String TB_STOP = "stop";
	private static final String TB_SCHEDULE = "schedule";

	public static final int CCOLUMN_STOP_INT_ID = 0;
	public static final int CCOLUMN_STOP_STR_NAME = 1;
	public static final int CCOLUMN_STOP_INT_LAT = 2;
	public static final int CCOLUMN_STOP_INT_LON = 3;
	public static final int CCOLUMN_STOP_INT_TYPE = 4;
	public static final int CCOLUMN_STOP_INT_COUNTY = 5;
	public static final int CCOLUMN_STOP_INT_TOWN = 6;
	
	/* Resources */
	private Context mContext;
	private DBOpenHelper mDBOpenHelper;
	private SQLiteDatabase mDB;
	private boolean dbAvailable;
	
	/** Local DB OpenHelper */
	private static class DBOpenHelper extends SQLiteOpenHelper {

		public DBOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(D_TAG, "DBOpenHelper - onCreate");
			db.execSQL("CREATE TABLE provider(_id INTEGER PRIMARY KEY,name TEXT,type INTEGER NOT NULL,phone TEXT,url TEXT);");
			db.execSQL("CREATE TABLE line_p(_id INTEGER PRIMARY KEY,line_id INTEGER NOT NULL,stop_seq_num INTEGER NOT NULL,nation_id INTEGER NOT NULL,stop_id INTEGER NOT NULL)");
			db.execSQL("CREATE TABLE line(_id INTEGER PRIMARY KEY,name TEXT,type INTEGER NOT NULL,provider_id INTEGER NOT NULL,s_time TEXT,e_time TEXT,line_info TEXT)");
			db.execSQL("CREATE TABLE ticket(_id INTEGER PRIMARY KEY,line_id INTEGER NOT NULL,from_stop_seq_num INTEGER NOT NULL,to_stop_seq_num INTEGER NOT NULL,price INTEGER NOT NULL)");
			db.execSQL("CREATE TABLE stop_l(_id INTEGER PRIMARY KEY,stop_id INTEGER NOT NULL,line_seq_num INTEGER NOT NULL,nation_id INTEGER NOT NULL,line_id INTEGER NOT NULL)");
			db.execSQL("CREATE TABLE stop(_id INTEGER PRIMARY KEY,name TEXT,type INTEGER NOT NULL,county_id INTEGER NOT NULL,town_id INTEGER NOT NULL,longitude INTEGER NOT NULL,latitude INTEGER NOT NULL)");
			db.execSQL("CREATE TABLE schedule(_id INTEGER PRIMARY KEY,line_id INTEGER NOT NULL,stop_seq_num INTEGER NOT NULL,nation_id INTEGER NOT NULL,stop_id INTEGER NOT NULL,sch_num TEXT,arrival_time INTEGER NOT NULL,depart_time INTEGER NOT NULL)");
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			Log.v(D_TAG, "DBOpenHelper - onOpen");
			super.onOpen(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion2) {
			Log.v(D_TAG, "DBOpenHelper - onUpgrade");
			db.execSQL("DROP TABLE IF EXISTS " + TB_LINE);
			db.execSQL("DROP TABLE IF EXISTS " + TB_LINEP);
			db.execSQL("DROP TABLE IF EXISTS " + TB_PROVIDER);
			db.execSQL("DROP TABLE IF EXISTS " + TB_SCHEDULE);
			db.execSQL("DROP TABLE IF EXISTS " + TB_STOP);
			db.execSQL("DROP TABLE IF EXISTS " + TB_STOPL);
			db.execSQL("DROP TABLE IF EXISTS " + TB_TICKET);
			onCreate(db);
		}
		
		public boolean dataImport(SQLiteDatabase db, Context context)
		{
			/** File (data) prepare */
			final String FN_DIRECTORY = "/sdcard/develop/pmap/mass_transit/";
			final String FN_PROVIDER = "tw-val-provider.txt";
			final String FN_LINEP = "tw-val-linep.txt";
			final String FN_LINE = "tw-val-line.txt";
			final String FN_TICKET = "tw-val-ticket.txt";
			final String FN_STOPL = "tw-val-stopl.txt";
			final String FN_STOP = "tw-val-stop.txt";
			final String FN_SCHEDULE = "tw-val-schedule.txt";
			
			File fProvider = new File(FN_DIRECTORY + FN_PROVIDER);
			File fLineP = new File(FN_DIRECTORY + FN_LINEP);
			File fLine = new File(FN_DIRECTORY + FN_LINE);
			File fTicket = new File(FN_DIRECTORY + FN_TICKET);
			File fStopL = new File(FN_DIRECTORY + FN_STOPL);
			File fStop = new File(FN_DIRECTORY + FN_STOP);
			File fSchedule = new File(FN_DIRECTORY + FN_SCHEDULE);
			
			Log.v(D_TAG, "dataImport: start to import the data from files locate on the " + FN_DIRECTORY);
			
			if(fProvider.exists() && fProvider.isFile() && fProvider.canRead() &&
			   fLineP.exists() && fLineP.isFile() && fLineP.canRead() &&
			   fLine.exists() && fLine.isFile() && fLine.canRead() &&
			   fTicket.exists() && fTicket.isFile() && fTicket.canRead() &&
			   fStopL.exists() && fStopL.isFile() && fStopL.canRead() &&
			   fStop.exists() && fStop.isFile() && fStop.canRead() &&
			   fSchedule.exists() && fSchedule.isFile() && fSchedule.canRead()
			   )
			{
				Log.d(D_TAG, "File all ready");
				
				try 
				{	
					DataInputStream inStream;
					BufferedReader bufReader;
					String strLine;
					
					inStream = new DataInputStream(new FileInputStream(fProvider));
					bufReader = new BufferedReader(new InputStreamReader(inStream));
				    while ((strLine = bufReader.readLine()) != null)
				    	db.execSQL("INSERT INTO " + TB_PROVIDER + strLine);
			    	Log.d(D_TAG, "Provider imported complete");
			    	inStream.close();
					
			    	inStream = new DataInputStream(new FileInputStream(fLineP));
					bufReader = new BufferedReader(new InputStreamReader(inStream));
				    while ((strLine = bufReader.readLine()) != null)
				    	db.execSQL("INSERT INTO " + TB_LINEP + strLine);
			    	Log.d(D_TAG, "LineP imported complete");
			    	inStream.close();
			    	
			    	inStream = new DataInputStream(new FileInputStream(fLine));
					bufReader = new BufferedReader(new InputStreamReader(inStream));
				    while ((strLine = bufReader.readLine()) != null)
				    	db.execSQL("INSERT INTO " + TB_LINE + strLine);
			    	Log.d(D_TAG, "Line imported complete");
			    	inStream.close();

			    	inStream = new DataInputStream(new FileInputStream(fTicket));
					bufReader = new BufferedReader(new InputStreamReader(inStream));
				    while ((strLine = bufReader.readLine()) != null)
				    	db.execSQL("INSERT INTO " + TB_TICKET + strLine);
			    	Log.d(D_TAG, "Ticket imported complete");
			    	inStream.close();
					
			    	inStream = new DataInputStream(new FileInputStream(fStopL));
					bufReader = new BufferedReader(new InputStreamReader(inStream));
				    while ((strLine = bufReader.readLine()) != null)
				    	db.execSQL("INSERT INTO " + TB_STOPL + strLine);
			    	Log.d(D_TAG, "StopL imported complete");
			    	inStream.close();
					
			    	inStream = new DataInputStream(new FileInputStream(fStop));
					bufReader = new BufferedReader(new InputStreamReader(inStream));
				    while ((strLine = bufReader.readLine()) != null)
				    	db.execSQL("INSERT INTO " + TB_STOP + strLine);
			    	Log.d(D_TAG, "Stop imported complete");
			    	inStream.close();

			    	inStream = new DataInputStream(new FileInputStream(fSchedule));
					bufReader = new BufferedReader(new InputStreamReader(inStream));
				    while ((strLine = bufReader.readLine()) != null)
				    	db.execSQL("INSERT INTO " + TB_SCHEDULE + strLine);
			    	Log.d(D_TAG, "Schedule imported complete");
			    	inStream.close();
			    	
			    	Log.d(D_TAG, "All data imported complete");
				} 
				catch (FileNotFoundException e) 
				{
					Log.e(D_TAG, "File not found at Scanner initializing: " + e.getMessage());
					return false;
				}
				catch (IllegalStateException e)
				{
					Log.e(D_TAG, e.getMessage());
				}
				catch (SQLException e)
				{
					Log.e(D_TAG, e.getMessage());
				}
				catch (Exception e)
				{
					Log.e(D_TAG, e.getMessage());
					return false;
				}
				
				return true;
			}
			else
			{
				Log.d(D_TAG, "File not ready, check the file existence on the " + FN_DIRECTORY);
				return false;
			}
		}
	}
	
	/** Constructor */
	public MassTransitDBManager(Context context) 
	{
		dbAvailable = false;
		mContext = context;
	}
	
	/** Interfaces */
	public MassTransitDBManager open() throws SQLiteException {
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
	
	public boolean dataImport()
	{
		mDBOpenHelper.onUpgrade(mDB, DB_VERSION, DB_VERSION);
		return mDBOpenHelper.dataImport(mDB, mContext);
	}
	
	public void dataClear()
	{
		mDBOpenHelper.onUpgrade(mDB, DB_VERSION, DB_VERSION);
	}
	
	public void close() {
		Log.d(D_TAG, "MassTransitDBManager: DB closed");
		mDBOpenHelper.close();
	}
	
	/** Query interface */
	
	public String TC_countInfo() {
		SQLiteStatement simpleProvider = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_PROVIDER);
		SQLiteStatement simpleLine = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_LINE);
		SQLiteStatement simpleLineP = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_LINEP);
		SQLiteStatement simpleStop = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_STOP);
		SQLiteStatement simpleStopL = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_STOPL);
		SQLiteStatement simpleSchedule = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_SCHEDULE);
		SQLiteStatement simpleTicket = mDB.compileStatement("SELECT COUNT(*) FROM " + TB_TICKET);

		return "Available Information:\n\n[ " + TB_PROVIDER + " ]  -  " + simpleProvider.simpleQueryForString() + "\n[ " +
			TB_LINE + " ]  -  " + simpleLine.simpleQueryForString() + "\n[ " +
			TB_LINEP + " ]  -  " + simpleLineP.simpleQueryForString() + "\n[ " +
			TB_STOP + " ]  -  " + simpleStop.simpleQueryForString() + "\n[ " +
			TB_STOPL + " ]  -  " + simpleStopL.simpleQueryForString() + "\n[ " +
			TB_SCHEDULE + " ]  -  " + simpleSchedule.simpleQueryForString() + "\n[ " +
			TB_TICKET + " ]  -  " + simpleTicket.simpleQueryForString() + "\n";
	}
	
	public Cursor MT_getStopsByRect(Point leftDown, Point rightTop)
	{
		Log.v(D_TAG, "MT_getStopsByRect");
		
		// Query the stops within the target rectangle-area
		Cursor stopsInRect = mDB.query(TB_STOP, new String[]{"_id", "name", "latitude", "longitude", "type", "county_id", "town_id"}, 
			" latitude<" + rightTop.x + 
			" and latitude>" + leftDown.x + 
			" and longitude<" + rightTop.y + 
			" and longitude>" + leftDown.y
			, null, null, null, null);
		
		Log.d(D_TAG, "Found " + Integer.toString(stopsInRect.getCount()) + " stops within LD(" + 
				Integer.toString(leftDown.x) + "," + Integer.toString(leftDown.y) + ") - RT(" +
				Integer.toString(rightTop.x) + "," + Integer.toString(rightTop.y) + ")");
		
		if(stopsInRect.getCount() > 0)
		{
			Log.d(D_TAG, Integer.toString(stopsInRect.getCount()) + "items Found, return Cursor now");
			return stopsInRect;
		}
		else
		{
			Log.d(D_TAG, "Nothing to return, return null now");
			stopsInRect.close();
			return null;	
		}
	}

	/*** Backup section */
	
	/** String parser *//*
	private static class StrInt
	{
		public String string;
		public int index;
		
		public StrInt()
		{
			string = "";
			index = 0;
		}
		
		public StrInt(String mString, int mIndex)
		{
			string = mString;
			index = mIndex;
		}
	}
	
	private static StrInt getNextToken(StrInt inStrInt)
	{
		StrInt token = new StrInt();
		
		token.string = "";
		
		for(int i = inStrInt.index; i < inStrInt.string.length(); i++)
		{
			if(',' == inStrInt.string.charAt(i) || '\n' == inStrInt.string.charAt(i))
			{
				token.index = i + 1;
				return token;
			}
			else
			{
				token.string += inStrInt.string.charAt(i);
			}
		}
		
		return token;
	}*/
	
	/** Simple query */
	/*public void testToast() {
		Cursor cursor = mDB.query(TB_PROVIDER, new String[]{"name"}, "_id=1", null, null, null, null);
		cursor.moveToFirst();
		Toast.makeText(mContext, cursor.getString(0), Toast.LENGTH_LONG).show();
	}*/
}
