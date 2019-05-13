package com.gaius.gaiusapp.utils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExternalDbOpenHelper extends SQLiteOpenHelper {

	
	public static String DB_PATH;
	
	public static String DB_NAME;
	public SQLiteDatabase database;
	public final Context context;
	
	public static final String KEY_BoxId="bxid";
	public static final String KEY_DataTime="orderid";
	public static final String KEY_PName="pname";
	public static final String KEY_Address = "addresskyc";
	public static final String KEY_Status = "status";
	//private static final String KEY_QTY = "qty";
	
	public SQLiteDatabase getDb() {
		return database;
	}

	public ExternalDbOpenHelper(Context context, String databaseName) {
		super(context, databaseName, null, 1);
		this.context = context;
		
		String packageName = context.getPackageName();
//		DB_PATH = String.format("//data//data//%s//databases//", packageName);
		DB_PATH = context.getDatabasePath(databaseName).getPath();
		DB_NAME = databaseName;
		openDataBase();
	}

	
	

	public void createDataBase() {
		boolean dbExist = checkDataBase();
		if (!dbExist) {
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException e) {
				Log.e(this.getClass().toString(), "Copying error");
				throw new Error("Error copying database!");
			}
		} else {
			Log.i(this.getClass().toString(), "Database already exists");
		}
	}
	
	private boolean checkDataBase() {
		SQLiteDatabase checkDb = null;
		try {
//			String path = DB_PATH + DB_NAME;
			String path = DB_PATH;
			checkDb = SQLiteDatabase.openDatabase(path, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLException e) {
			Log.e(this.getClass().toString(), "Error while checking db");
		}
		
		if (checkDb != null) {
			checkDb.close();
		}
		return checkDb != null;
	}
	
	private void copyDataBase() throws IOException {
		
		InputStream externalDbStream = context.getAssets().open(DB_NAME);

		
//		String outFileName = DB_PATH + DB_NAME;
		String outFileName = DB_PATH;

		
		OutputStream localDbStream = new FileOutputStream(outFileName);

		
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = externalDbStream.read(buffer)) > 0) {
			localDbStream.write(buffer, 0, bytesRead);
		}
		
		localDbStream.close();
		externalDbStream.close();

	}

	public SQLiteDatabase openDataBase() throws SQLException {
//		String path = DB_PATH + DB_NAME;
		String path = DB_PATH;
		if (database == null) {
			createDataBase();
			database = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READWRITE);
		}
		return this.getWritableDatabase();
	}
	@Override
	public synchronized void close() {
		if (database != null) {
			database.close();
		}
		super.close();
	}
	@Override
	public void onCreate(SQLiteDatabase db) {}
	/*@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	
	/* Method to create a Employee  
	 public long createProduct(AtomPayment pro) {
	     long c =0;

	     //SQLiteDatabase database = getWritableDatabase();
	     ContentValues values = new ContentValues();
	     values.put(KEY_BoxId, pro.getOrderID());
	     values.put(KEY_DataTime, pro.getDod());
	     values.put(KEY_PName, pro.getProductName());
	     values.put(KEY_Address, pro.getAddress());
	     values.put(KEY_Status, pro.getStatus());
	     //values.put(KEY_ADDRESS, emp.getAddress());
	     // c = database.insert(TABLE_EMP, null, values);
	     // database.close();
	     return c;

	 }*/

	 /* Method for fetching record from Database */ /* This method is used to get a single record from Database. 
	    I have given an example, you have to do something like this. */

	 private Context getBaseContext() {
		// TODO Auto-generated method stub
		return null;
	}

		
	 @Override
	 public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	  db.execSQL("DROP TABLE IF EXISTS RunningStatus");
	  
	  onCreate(db);

	 }

	public SQLiteDatabase deleteDatabase() throws SQLException {
//		String path = DB_PATH + DB_NAME;
		String path = DB_PATH;
		if (database == null) {
			createDataBase();
			database = SQLiteDatabase.openDatabase(path, null,
					SQLiteDatabase.OPEN_READWRITE);
			SQLiteDatabase.deleteDatabase(new File(path));
		}
		return database;
	}

    @Override
    public void onConfigure(SQLiteDatabase db) {
//        super.onConfigure(db);
        super.onOpen(db);
        db.rawQuery("PRAGMA journal_mode = OFF",null);
        db.disableWriteAheadLogging();
    }
}
