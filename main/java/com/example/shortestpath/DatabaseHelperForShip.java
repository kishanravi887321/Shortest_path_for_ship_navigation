package com.example.shortestpath;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperForShip extends SQLiteOpenHelper {

    // Database and table information
    private static final String DATABASE_NAME = "ShipRoutes.db";
    private static final String TABLE_NAME = "routes";
    private static final String COL_1 = "ID"; // Primary key
    private static final String COL_2 = "ORIGIN";
    private static final String COL_3 = "DESTINATION";
    private static final String COL_4 = "WAYPOINTS";
    private static final String COL_5 = "FUEL";
    private static final String COL_6 = "TRAVEL_TIME";
    private static final String COL_7 = "COMFORT";

    public DatabaseHelperForShip(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the routes table
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ORIGIN TEXT, DESTINATION TEXT, WAYPOINTS TEXT, FUEL REAL, TRAVEL_TIME REAL, COMFORT TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Method to insert a route into the database
    public boolean insertRoute(String origin, String destination, String waypoints, double fuel, double travelTime, String comfort) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, origin);
        contentValues.put(COL_3, destination);
        contentValues.put(COL_4, waypoints);
        contentValues.put(COL_5, fuel);
        contentValues.put(COL_6, travelTime);
        contentValues.put(COL_7, comfort);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // If result is -1, insertion failed
    }

    // Optional: Method to retrieve all routes from the database
    public Cursor getAllRoutes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
