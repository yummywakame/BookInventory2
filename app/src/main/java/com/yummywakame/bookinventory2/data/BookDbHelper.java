package com.yummywakame.bookinventory2.data;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yummywakame.bookinventory2.data.BookContract.BookEntry;

/**
 * ud845-Books
 * Created by Olivia Meiring on 2018/08/16.
 * Yummy Wakame
 * olivia@yummy-wakame.com
 */
public class BookDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "My Log - " + BookDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "books.db";

    /**
     * If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link BookDbHelper}.
     *
     * @param context of the app
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the books table
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL, "
                + BookEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL, "
                + BookEntry.COLUMN_SUPPLIER_ID + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_BOOK_PRICE + " REAL NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
        Log.i(LOG_TAG, "db.execSQL(SQL_CREATE_BOOKS_TABLE): " + SQL_CREATE_BOOKS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Get the total number of books in the database to display the count at the top of the list
     */
    public static long getBookCount(Context context) {
        SQLiteOpenHelper database = new BookDbHelper(context);
        SQLiteDatabase db = database.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, BookEntry.TABLE_NAME);
        db.close();
        return count;
    }
}