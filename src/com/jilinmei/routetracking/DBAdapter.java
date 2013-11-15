package com.jilinmei.routetracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
	
	static final String DATABASE_NAME = "MyDB";
	static final String DATABASE_TABLE = "location_data";
	static final int DATABASE_VERSION = 1;
	
	static final String COLUMN_ID = "_id";
	static final String COLUMN_LATITUDE = "latitude";
	static final String COLUMN_LONGITIDE = "longitude";
	static final String COLUMN_CREATED_AT = "created_at";
	
	static final String SQL_CREATE_TABLE =
			"create table location_data (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"latitude REAL, longitude REAL, created_at TEXT NOT NULL)";
	
	static final String SQL_DROP_TABLE =
			"";
	
	Context context;
	DatabaseHelper dbHelper;
	SQLiteDatabase db;
	
	public DBAdapter(Context context) {
		this.context = context;
		dbHelper = new DatabaseHelper(context);
	}
	
	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(SQL_CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public boolean open() {
		db = dbHelper.getWritableDatabase();
		return true;
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public boolean insertLocation(double lat, double lon) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_LATITUDE, lat);
		values.put(COLUMN_LONGITIDE, lon);
		values.put(COLUMN_CREATED_AT, "16:50");
		db.insert(DATABASE_TABLE, null, values);
		return true;
	}
	
	public Cursor getAllLocations()  {
		String[] columns = {COLUMN_LATITUDE, COLUMN_LONGITIDE};
		return db.query(DATABASE_TABLE, columns, null, null, null, null, null);
	}
	
	public boolean removeAllLocations() {
		db.delete(DATABASE_TABLE, null, null);
		return true;
	}
	
}
