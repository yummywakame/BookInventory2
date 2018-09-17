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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yummywakame.bookinventory2.data.BookContract.BookEntry;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;

    /**
     * EditText fields to enter the book's information
     */
    private EditText mNameEditText, mAuthorEditText, mQuantityEditText, mPriceEditText;

    /**
     * TextView field to display the currency symbol based on chosen locale
     */
    private TextView mCurrencyTextView;

    /**
     * EditText field to enter the book's supplier
     */
    private Spinner mSupplierSpinner;

    /**
     * Supplier of the book. The possible valid values are in the BookContract.java file:
     * {@link BookEntry#SUPPLIER_SELECT}, {@link BookEntry#SUPPLIER_1}, {@link BookEntry#SUPPLIER_2}
     * {@link BookEntry#SUPPLIER_3}, or {@link BookEntry#SUPPLIER_4}
     */
    private int mSupplierId = BookEntry.SUPPLIER_SELECT;

    /**
     * Values for validation
     */
    private String titleString;
    private String authorString;
    private String quantityString;
    private String priceString;

    /**
     * Boolean flag that keeps track of whether the book has been edited (true) or not (false)
     */
    private boolean mBookHasChanged = false;

    /**
     * OnTouchListener that listens touches on EditText views, implying that they are modifying
     * the view, so we change the mBookHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            view.performClick();
            return false;
        }
    };

    /**
     * OnTouchListener that listens for when a user touches the spinner
     * to close the soft keyboard if it is open.
     */
    private View.OnTouchListener mSpinnerTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            hideSoftKeyboard(view);
            view.performClick();
            return false;
        }
    };

    /**
     * OnFocusChangeListener that listens for any click outside the EditText field
     * so we can hide the keyboard.
     */
    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                hideSoftKeyboard(view);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // If the intent DOES NOT contain a book content URI, then we know that we are
        // creating a new book.
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.editor_activity_title_add_book));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a book that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Initialize a loader to read the book data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.editTextTitle);
        mAuthorEditText = findViewById(R.id.editTextAuthor);
        mQuantityEditText = findViewById(R.id.editTextQuantity);
        mSupplierSpinner = findViewById(R.id.spinnerSupplier);
        mPriceEditText = findViewById(R.id.editTextPrice);
        mCurrencyTextView = findViewById(R.id.currencySymbol);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mAuthorEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        // Setup OnFocusChangeListeners on all the input fields, so we can hide the
        // soft keyboard and get it out of the way
        mNameEditText.setOnFocusChangeListener(mFocusChangeListener);
        mAuthorEditText.setOnFocusChangeListener(mFocusChangeListener);
        mQuantityEditText.setOnFocusChangeListener(mFocusChangeListener);
        mPriceEditText.setOnFocusChangeListener(mFocusChangeListener);

        // Setup OnTouchListener on the spinner so we can hide the soft keyboard
        mSupplierSpinner.setOnTouchListener(mSpinnerTouchListener);

        // Show user's locale currency
        mCurrencyTextView.setText(String.valueOf(HelperClass.formatPrice(this, 0, true, false)));

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier of the book.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, R.layout.spinner_title);

        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown);

        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);

                // Set the text color of the Spinner's selected view (not a drop down list view)
                TextView selectedSpinnerView = (TextView) mSupplierSpinner.getSelectedView();
                selectedSpinnerView.setTextColor(getResources().getColor(R.color.darkTextColor));

                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_1))) {
                        mSupplierId = BookEntry.SUPPLIER_1;
                    } else if (selection.equals(getString(R.string.supplier_2))) {
                        mSupplierId = BookEntry.SUPPLIER_2;
                    } else if (selection.equals(getString(R.string.supplier_3))) {
                        mSupplierId = BookEntry.SUPPLIER_3;
                    } else if (selection.equals(getString(R.string.supplier_4))) {
                        mSupplierId = BookEntry.SUPPLIER_4;
                    } else {
                        mSupplierId = BookEntry.SUPPLIER_SELECT;
                        selectedSpinnerView.setTextColor(getResources().getColor(R.color.mediumTextColor));
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplierId = BookEntry.SUPPLIER_SELECT;
            }
        });
    }

    public boolean isValidBook() {
        // Read from input fields. Use trim to eliminate leading or trailing white space.
        titleString = mNameEditText.getText().toString().trim();
        authorString = mAuthorEditText.getText().toString().trim();
        quantityString = mQuantityEditText.getText().toString().trim();
        priceString = mPriceEditText.getText().toString().trim();

        // If quantity is left empty, set to zero
        if (TextUtils.isEmpty(quantityString)) {
            // Show the error in a toast message.
            mQuantityEditText.setText(String.valueOf(0));
        }

        // Quick validation
        if (TextUtils.isEmpty(titleString)) {
            // Show the error in a toast message.
            Toast.makeText(this, getString(R.string.toast_required_title),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(authorString)) {
            // Show the error in a toast message.
            Toast.makeText(this, getString(R.string.toast_required_author),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (mSupplierId == 0) {
            // Show the error in a toast message.
            Toast.makeText(this, getString(R.string.toast_required_supplier),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(priceString)) {
            // Show the error in a toast message.
            Toast.makeText(this, getString(R.string.toast_required_price),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get user input from editor and save book into database.
     */
    private void saveBook() {
        // Check if this is supposed to be a new book
        // and check if all the fields in the editor are blank
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(titleString) && TextUtils.isEmpty(authorString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString) &&
                mSupplierId == BookEntry.SUPPLIER_SELECT) {
            // Since no fields were modified, we can return early without creating a new book.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and book attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_TITLE, titleString);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, authorString);
        values.put(BookEntry.COLUMN_SUPPLIER_ID, mSupplierId);

        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);

        // If the price is not provided by the user, don't try to parse the string into an
        // double value. Use 0 by default.
        double price = 0.00;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);

        // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
        if (mCurrentBookUri == null) {
            // This is a NEW book, so insert a new book into the provider,
            // returning the content URI for the new book.
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.toast_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.toast_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentBookUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.toast_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.toast_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save book to database
                if (isValidBook()) {
                    saveBook();
                    // Exit activity
                    finish();
                    return true;
                } else {
                    return false;
                }
                // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                HelperClass.showDeleteConfirmationDialog(this, mCurrentBookUri);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_TITLE,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_SUPPLIER_ID,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_PRICE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_TITLE);
            int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_ID);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mAuthorEditText.setText(author);
            mQuantityEditText.setText(String.valueOf(quantity));
            mPriceEditText.setText(String.valueOf(HelperClass.formatPrice(this, price, false, true)));
            // Show user's locale currency
            mCurrencyTextView.setText(String.valueOf(HelperClass.formatPrice(this, price, true, false)));

            // Supplier is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is "Please select...",
            // 1 is "Ingram Content Group", 2 is "Baker & Taylor", 3 is "Publishers Group West",
            // 4 is "Independent Publishers Group").
            // Then call setSelection() so that option is displayed on screen as the current
            // selection.
            switch (supplier) {
                case BookEntry.SUPPLIER_1:
                    mSupplierSpinner.setSelection(1);
                    break;
                case BookEntry.SUPPLIER_2:
                    mSupplierSpinner.setSelection(2);
                    break;
                case BookEntry.SUPPLIER_3:
                    mSupplierSpinner.setSelection(3);
                    break;
                case BookEntry.SUPPLIER_4:
                    mSupplierSpinner.setSelection(4);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mAuthorEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierSpinner.setSelection(0); // Select "Unknown" supplier
        mPriceEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    // Hide the software keyboard when necessary
    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}