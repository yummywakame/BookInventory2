package com.yummywakame.bookinventory2.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.yummywakame.bookinventory2.R;
import com.yummywakame.bookinventory2.data.BookContract.BookEntry;

/**
 * {@link android.content.ContentProvider} for Books app.
 */
public class ContentProvider extends android.content.ContentProvider {
    /**
     * Variable for getting the context
     */
    private Context mContext;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = "My Log - " + ContentProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the books table
     */
    private static final int BOOKS = 100;
    /**
     * URI matcher code for the content URI for a single book in the books table
     */
    private static final int BOOK_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The content URI of the form "content://com.yummywakame.bookinventory2/books" will map to the
        // integer code {@link #BOOKS}. This URI is used to provide access to MULTIPLE rows
        // of the books table.
        // No wildcard is used in the path.
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        // The content URI of the form "content://com.yummywakame.bookinventory2/books/#" will map to the
        // integer code {@link #BOOK_ID}. This URI is used to provide access to ONE single row.
        // In this case the "#" wild card is used where "#" can be substituted for an integer.
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    /**
     * Database helper object
     */
    private BookDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
        mContext = getContext();
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // For the BOOKS code, query the books table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the books table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.yummywakame.bookinventory2/books/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the books table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException npe) {
            Log.e(LOG_TAG, "Failed to setNotificationUri on Cursor. NullPointerException: " + npe);
        }
        return cursor;
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Helper Method:
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(@NonNull Uri uri, @Nullable ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(BookEntry.COLUMN_BOOK_TITLE);
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_title));
        }
        // Check that the author is not null
        String author = values.getAsString(BookEntry.COLUMN_BOOK_AUTHOR);
        if (TextUtils.isEmpty(author)) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_author));
        }
        // Check that the supplier is valid
        Integer supplier = values.getAsInteger(BookEntry.COLUMN_SUPPLIER_ID);
        if (supplier == null || !BookEntry.isValidSupplier(supplier)) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_supplier));
        }
        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_quantity));
        }
        // If the price is provided, check that it's greater than or equal to 0
        Double price = values.getAsDouble(BookEntry.COLUMN_BOOK_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException(mContext.getString(R.string.toast_required_price));
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new book with the given values
        long id = database.insert(BookEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for uri: " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the book content URI
        try {
            // When passing in null as a parameter for ContentObserver, by default the CursorAdapter
            // object will get notified, and the LoaderCallBack methods will still get triggered.
            // uri: content://com.yummywakame.bookinventory2/books
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException npe) {
            Log.e(LOG_TAG, "notifyChange() Failed! " + npe);
        }

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                // For the BOOK_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update books in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more books).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link BookEntry#COLUMN_BOOK_TITLE} key is present,
        // check that the name value is not null.
        if (values.containsKey(BookEntry.COLUMN_BOOK_TITLE)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_TITLE);
            if (name == null) {
                throw new IllegalArgumentException(mContext.getString(R.string.toast_required_title));
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_AUTHOR} key is present,
        // check that the name value is not null.
        if (values.containsKey(BookEntry.COLUMN_BOOK_AUTHOR)) {
            String author = values.getAsString(BookEntry.COLUMN_BOOK_AUTHOR);
            if (author == null) {
                throw new IllegalArgumentException(mContext.getString(R.string.toast_required_author));
            }
        }

        // If the {@link BookEntry#COLUMN_SUPPLIER_ID} key is present,
        // check that the supplier value is valid.
        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_ID)) {
            Integer supplier = values.getAsInteger(BookEntry.COLUMN_SUPPLIER_ID);
            if (supplier == null || !BookEntry.isValidSupplier(supplier)) {
                throw new IllegalArgumentException(mContext.getString(R.string.toast_required_supplier));
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_BOOK_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException(mContext.getString(R.string.toast_required_quantity));
            }
        }

        // If the {@link BookEntry#COLUMN_BOOK_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            // Check that the price is greater than or equal to 0
            Integer price = values.getAsInteger(BookEntry.COLUMN_BOOK_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException(mContext.getString(R.string.toast_required_price));
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (NullPointerException npe) {
                Log.e(LOG_TAG, "notifyChange() Failed! " + npe);
            }
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                /// Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            try {
                getContext().getContentResolver().notifyChange(uri, null);
            } catch (NullPointerException npe) {
                Log.e(LOG_TAG, "notifyChange() Failed! " + npe);
            }
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}