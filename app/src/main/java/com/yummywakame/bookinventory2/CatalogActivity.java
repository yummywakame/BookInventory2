/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yummywakame.bookinventory2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yummywakame.bookinventory2.adapters.BookCursorAdapter;
import com.yummywakame.bookinventory2.adapters.SwipeDismissListViewTouchListener;
import com.yummywakame.bookinventory2.data.BookContract;
import com.yummywakame.bookinventory2.data.BookContract.BookEntry;
import com.yummywakame.bookinventory2.data.BookDbHelper;
import com.yummywakame.bookinventory2.data.ContentProvider;

/**
 * Displays list of books that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = "My Log - " + ContentProvider.class.getSimpleName();
    /**
     * Initialize value can be set as any unique integer.
     */
    private static final int BOOK_LOADER = 0;
    /**
     * The adapter for the list view
     */
    BookCursorAdapter mCursorAdapter;

    /**
     * The Sort By preference array
     */
    String[] sortBy;


    /**
     * Number for results message that displays above the ListView
     */
    private String resultsCounter;
    private TextView numberOfResults;
    private TextView orderOfResults;
    private LinearLayout orderByToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Get the App Title Bar and menu items to display
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the book data
        ListView bookListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);

        // Setup an adapter to create a list item for each row of book data in the Cursor.
        // There is no book data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);

        // Set up item click listener
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific book that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link BookEntry#Content_URI}.
                // For example, the URI would be "content://com.yummywakame.bookinventory2/2"
                // If the book with ID 2 was clicked on.
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);
                startActivity(intent);
            }
        });

        // Kick off the loader that loads the list items
        getLoaderManager().initLoader(BOOK_LOADER, null, this);

        // Find and set the results counter in the toolbar area
        numberOfResults = findViewById(R.id.orderby_toolbar_results);
        orderOfResults = findViewById(R.id.orderby_toolbar_ordering);
        orderByToolbar = findViewById(R.id.orderby_toolbar);
        setResultsCounter(this, numberOfResults, orderOfResults);


        // Swipe to delete
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        bookListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int id : reverseSortedPositions) {
                                    long currentID = listView.getAdapter().getItemId(id);
                                    Uri currentBookUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, currentID);
                                    HelperClass.deleteBook(CatalogActivity.this, currentBookUri, false);
                                    setResultsCounter(CatalogActivity.this, numberOfResults, orderOfResults);
                                }
                            }
                        });
        bookListView.setOnTouchListener(touchListener);
    }

    /**
     * Helper that checks and sets the results counter above the ListView
     * Also shows or hides the counter and the App Title depending on whether the row count
     * is greater than zero.
     */
    public void setResultsCounter(Context context, TextView numberOfResults, TextView orderOfResults) {
        // Show how many results found
        long count = BookDbHelper.getBookCount(this);
        resultsCounter = String.valueOf(count) + " " + getString(R.string.order_by_toolbar_results_found);
        numberOfResults.setText(resultsCounter);

        // Shows what it's sorted by and changes the arrow graphic accordingly
        sortBy = HelperClass.getSortByPreference(context);
        String sortColumn;
        //sortBy[0] to retrieve the column and sortBy[2] to retrieve the sort direction

        switch (sortBy[0]) {
            case "book_author":
                sortColumn = "Author";
                break;
            case "book_title":
                sortColumn = "Title";
                break;
            case "quantity":
                sortColumn = "Stock Count";
                break;
            case "price":
                sortColumn = "Price";
                break;
            default:
                sortColumn = "Default";
                break;
        }

        switch (sortBy[1]) {
            case "ASC":
                orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ascending, 0);
                break;
            case "DESC":
                orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_descending, 0);
                break;
            default:
                orderOfResults.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ascending, 0);
                break;
        }
        orderOfResults.setText(sortColumn);

        // Hides App Title if there are no books to show
        if (count == 0) {
            orderByToolbar.setVisibility(View.INVISIBLE);
            // Hide the app title
            getSupportActionBar().setTitle("");
        } else {
            orderByToolbar.setVisibility(View.VISIBLE);
            // Show the app title
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    /**
     * Helper method to insert hardcoded book data into the database. For debugging purposes only.
     */
    private void insertBook(String Title, String Author, Integer Supplier, Integer Stock, Double Price) {
        // Create a ContentValues object where column names are the keys,
        // and the book attributes are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_TITLE, Title);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, Author);
        values.put(BookEntry.COLUMN_SUPPLIER_ID, Supplier);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, Stock);
        values.put(BookEntry.COLUMN_BOOK_PRICE, Price);

        // Insert a new row for the book into the provider using the ContentResolver.
        // Use the {@link BookEntry#CONTENT_URI} to indicate that we want to insert
        // into the books database table.
        // Receive the new content URI that will allow us to access the book's data in the future.
        Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

        Log.v(LOG_TAG, "Inserted new row: " + newUri);
    }

    /**
     * Helper method to delete all books in the database.
     */
    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            // If all rows were deleted, then show the success message as a toast
            Toast.makeText(CatalogActivity.this, getString(R.string.toast_delete_all_books_successful, rowsDeleted),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Nothing was deleted
            Toast.makeText(CatalogActivity.this, getString(R.string.toast_delete_all_books_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prompt the user to confirm that they want to delete ALL the books.
     */
    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all the books.
                deleteAllBooks();
                setResultsCounter(CatalogActivity.this, numberOfResults, orderOfResults);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Add a Book" menu option
            case R.id.action_add:
                // Open EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;

            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Insert 10 books from array_dummy_data
                for (int i = 0; i <= 9; i++) {
                    insertBook(
                            getResources().getStringArray(R.array.array_book_title)[i],
                            getResources().getStringArray(R.array.array_book_author)[i],
                            getResources().getIntArray(R.array.array_book_supplier)[i],
                            getResources().getIntArray(R.array.array_book_stock)[i],
                            Double.parseDouble(getResources().getStringArray(R.array.array_book_price)[i]));
                }
                setResultsCounter(this, numberOfResults, orderOfResults);
                return true;

            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Pop up confirmation dialog for deletion
                showDeleteAllConfirmationDialog();
                return true;

            // Respond to a click on the "Settings" menu option
            case R.id.action_preferences:
                Intent settingsIntent = new Intent(this, PreferencesActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table.
        // Use only the columns needed to display the list view for better performance.
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_TITLE,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_PRICE};

        Log.i(LOG_TAG, "Loader<Cursor> onCreateLoader()");

        // Get the Sort By preference array from Preferences using the helper method
        sortBy = HelperClass.getSortByPreference(this);

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,               // Parent activity context
                BookEntry.CONTENT_URI,                      //Provider content URI to query
                projection,                                 // Columns to include in the resulting Cursor
                null,                              // No selection clause
                null,                           // No selection arguments
                sortBy[0] + " " + sortBy[1]);      // Sort order from Preferences
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursorData) {
        // Update {@link BookCursorAdapter} with this new cursor containing updated book data
        mCursorAdapter.swapCursor(cursorData);
        Log.i(LOG_TAG, "onLoadFinished() updates BookCursorAdapter with new cursor containing updated book data. Cursor: " + cursorData);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
        Log.i(LOG_TAG, "onLoaderReset() on data that needs to be deleted.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        setResultsCounter(this, numberOfResults, orderOfResults);
    }
}