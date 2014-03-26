package com.remindme;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class that manages the SQLite database
 * @author Andy Valdez
 *
 */
public class RemindMeDatabase {

	private String DB_NAME = "memories.db";
	private int DB_VERSION = 1;
	private RemindMeOpenHelper openHelper;
	private SQLiteDatabase database;
	
	/*Table Constants*/
	private String THINGS = "things";
	
	/* Column constants */
	private String ITEM = "thing_to_remember";
	private String RAW_PHOTO = "raw_photo_path";
	private String RESIZED_PHOTO = "resized_photo_path";
	private String TIMELINE_CARD_ID = "timeline_card_id";
	
	Context context;
	
	public RemindMeDatabase(Context context) {
		this.context = context;
		openHelper = new RemindMeOpenHelper(context);
		database = openHelper.getWritableDatabase();
	}//end of RemindMeDatabase
	
	
	public void addReminder(String itemRemember, String rawPath, String resizedPath, long timelineId) {
		ContentValues c = new ContentValues();
		c.put(ITEM, itemRemember);
		c.put(RAW_PHOTO, rawPath);
		c.put(RESIZED_PHOTO, resizedPath);
		c.put(TIMELINE_CARD_ID, timelineId);
		database.insert(THINGS, null, c);
	}
	
	/*
	 * Using this method just to make sure database isn't getting filled while testing.
	 */
	public void deleteItems() {
		int rows = database.delete(THINGS, null, null);
		Log.d("TestService", "Rows deleted: " + rows);
	}
	
	
	public void closeDatabase() {
		if(database != null) {
			database.close();
		}
	}	
	
	
	public ArrayList<String> getItemImagePath(String itemRemember) {
		ArrayList<String> paths = new ArrayList<String>();
		String newString = "";
		String arr[] = itemRemember.split(" ");
		for (String s : arr) {
			newString += " *" + s + "* ";
		}
		
		Cursor resultQuery = database.query(THINGS, new String[] {"thing_to_remember", "photo_path"},
				"thing_to_remember MATCH ?", new String[] {"'" + newString + "'"}, null, null, null);
		
		//Should really either be 1 or 0 in size. Will worry about getting a larger set if other similar items exist.
		//Cursor resultQuery = database.rawQuery("SELECT * FROM things WHERE thing_to_remember MATCH ?", new String[]{"'"+itemRemember+"'"});
		
		Log.d("Test", resultQuery.getCount() + " " + resultQuery.toString());
		
		if(resultQuery.getCount() > 0) {
			resultQuery.moveToFirst();
			String path = resultQuery.getString(1);
			paths.add(path);
			
			if (resultQuery.getCount() > 1) {
				
				for (int i = 1; i < resultQuery.getCount(); i++) {
					resultQuery.moveToNext();
					path = resultQuery.getString(1);
					paths.add(path);
				}
				
			}
			
			return paths;
		}
		
		return null;
	}
	
	public String getSingleItemImagePath(String itemToRemember) {
		
		Cursor resultQuery = database.query(THINGS, new String[] {"thing_to_remember", "photo_path"},
				"thing_to_remember MATCH ?", new String[] {"'" + itemToRemember + "'"}, null, null, null);
		
		if(resultQuery.getCount() == 1) {
			resultQuery.moveToFirst();
			String imagePath = resultQuery.getString(1);
			return imagePath;
		}
		
		return null;
	}
	
	/**
	 * Method that takes a the String item to search for, and uses one of two
	 * empty objects also sent in for setting the return value. If both of those objects
	 * are still null afterwards, then the database returned nothing. Show appropriate method.
	 * 
	 * @param item String of the item to search for.
	 * @param mem null Memory object
	 * @param mCards null Card ArrayList
	 * @return Nothing. If both mem and mCards is null, then absolutely nothing was returned. Database is most likely empty.
	 */
	public Cursor searchMemory(String item) {
		
		Cursor resultQuery = database.query(THINGS, new String[] {"rowid",ITEM, RESIZED_PHOTO, RAW_PHOTO, TIMELINE_CARD_ID},
				"thing_to_remember MATCH ?", new String[] {"'" + item + "'"}, null, null, null);	
		
		return resultQuery;
		
	}//end of searchMemory()
	
	public boolean removeItem(Memory mem) {
		
		if(mem != null) {
			
			int result = database.delete(THINGS, "rowid = ?", new String[] {String.valueOf(mem.getId())});
			
			if (result == 1) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * Private inner class that creates the database instance.
	 * 
	 * @author Andy Valdez
	 *
	 */
	private class RemindMeOpenHelper extends SQLiteOpenHelper {
		
		public RemindMeOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("Test", "Creating table " + THINGS);
			String thingsToRemember = 
					"CREATE VIRTUAL TABLE " + THINGS + " USING fts4(thing_to_remember text,"
							+ " raw_photo_path text, resized_photo_path text, timeline_card_id text)";
			db.execSQL(thingsToRemember);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
		
	}//end of RemindMeOpenHelper
	
	
}//end of class
