package com.yummywakame.bookinventory2;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yummywakame.bookinventory2.data.BookContract;
import com.yummywakame.bookinventory2.data.BookContract.BookEntry;

/**
 * Allows user to view the details of a book but not edit it.
 */
public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {

    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;
    /**
     * String that holds the book title for the AppBar
     */
    String title;
    /**
     * Custom Expandable AppBar
     */
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    AppBarLayout mAppBarLayout;
    private View mFabBottom;
    /**
     * Variables needed to appear to "move" the FAB to the bottom on scroll
     */
    private View mFabTop;
    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri mCurrentBookUri;
    /**
     * EditText fields to enter the book's information
     */
    private TextView mTitle, mAuthor, mSupplier, mQuantity, mPrice;
    /**
     * String that holds currency from preferences
     */
    private String mCurrency;
    /**
     * String that holds the suppliers phone number
     */
    private String mSupplierPhone;

    /**
     * Int that holds the quantity for the + and - buttons
     */
    private int quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Toolbar back button click
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Style the CollapsingToolbarLayout Title
        mCollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

//        getSupportActionBar().setTitle(R.string.editor_activity_title_view_book);
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        // This is the most important when you are putting custom textview in CollapsingToolbar
        mCollapsingToolbarLayout.setTitle(" ");

        mAppBarLayout = findViewById(R.id.appbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                // Test if AppBar is expanded or collapsed
                if (scrollRange + verticalOffset == 0) {
                    // When AppBar is Collapsed
                    //when collapsingToolbar at that time display actionbar title
                    mCollapsingToolbarLayout.setTitle(title);
                    isShow = true;

                    //  Hide top FAB, show bottom FAB
                    ViewCompat.animate(mFabTop).scaleY(0).scaleX(0).start();
                    mFabBottom.setVisibility(View.VISIBLE);
                    ViewCompat.animate(mFabBottom).scaleY(1).scaleX(1).start();
                } else if (isShow) {
                    // When AppBar is expanded
                    //careful there must a space between double quote otherwise it doesn't work
                    mCollapsingToolbarLayout.setTitle(" ");
                    isShow = false;

                    // Hide bottom FAB, show top FAB
                    ViewCompat.animate(mFabTop).scaleY(1).scaleX(1).start();
                    ViewCompat.animate(mFabBottom).scaleY(0).scaleX(0).start();
                }
            }
        });

        Typeface font = Typeface.createFromAsset(MyApplication.getAppContext().getAssets(), "fonts/quicksand_medium.ttf");
        mCollapsingToolbarLayout.setCollapsedTitleTypeface(font);
        mCollapsingToolbarLayout.setExpandedTitleTypeface(font);

//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); //bellow setSupportActionBar(toolbar);
//        getSupportActionBar().setCustomView(R.layout.titlebar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Locate both FAB buttons
        mFabTop = findViewById(R.id.fab_call_supplier_top);
        mFabBottom = findViewById(R.id.fab_call_supplier_bottom);
        // Hide bottom FAB button to start
        mFabBottom.setVisibility(View.INVISIBLE);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // Initialize a loader to read the book data from the database
        // and display the current values on the screen
        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        // Find all relevant views that we will need to read user input from
        mTitle = findViewById(R.id.textViewTitle);
        mAuthor = findViewById(R.id.textViewAuthor);
        mQuantity = findViewById(R.id.textViewQuantity);
        mSupplier = findViewById(R.id.textViewSupplier);
        mPrice = findViewById(R.id.textViewPrice);
//        mCurrency = findViewById(R.id.currencySymbol);

        // Find user's locale currency from Preferences
        mCurrency = String.valueOf(HelperClass.formatPrice(this, 0, true, false));

        // Click listener for "+" button
        Button increaseBtn = findViewById(R.id.button_add);
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(BookEntry.COLUMN_BOOK_QUANTITY, Integer.valueOf(mQuantity.getText().toString()) + 1);
                getContentResolver().update(mCurrentBookUri, contentValues, null, null);
            }
        });

        // Click listener for "-" button
        Button decreaseBtn = findViewById(R.id.button_subtract);
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity = Integer.parseInt(mQuantity.getText().toString().trim());
                if (quantity > 0) {
                    quantity--;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                    getContentResolver().update(mCurrentBookUri, contentValues, null, null);
                }
            }
        });

        // On touch listener for top FAB to call Supplier
        mFabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelperClass.callSupplier(DetailActivity.this, mSupplierPhone);
            }
        });

        // On touch listener for bottom FAB to call Supplier
        mFabBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelperClass.callSupplier(DetailActivity.this, mSupplierPhone);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the activity shows all book attributes, define a projection that contains
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
                mCurrentBookUri,                // Query the content URI for the current book
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null);                 // Default sort order
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
            title = cursor.getString(nameColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);

            // Update the views on the screen with the values from the database
//            getSupportActionBar().setTitle(name);
//            getSupportActionBar().setSubtitle(author);
            mTitle.setText(title);
            mAuthor.setText(author);
            mQuantity.setText(String.valueOf(quantity));
            // Show price with user's locale currency
            mPrice.setText(String.valueOf(HelperClass.formatPrice(this, price, true, true)));

            // Supplier options available: (0 is "Please select...",
            // 1 is "Ingram Content Group", 2 is "Baker & Taylor", 3 is "Publishers Group West",
            // 4 is "Independent Publishers Group").

            switch (supplier) {
                case BookEntry.SUPPLIER_1:
                    mSupplier.setText(getResources().getStringArray(R.array.array_supplier_options)[1]);
                    mSupplierPhone = getResources().getStringArray(R.array.array_supplier_phone)[1];
                    break;
                case BookEntry.SUPPLIER_2:
                    mSupplier.setText(getResources().getStringArray(R.array.array_supplier_options)[2]);
                    mSupplierPhone = getResources().getStringArray(R.array.array_supplier_phone)[2];
                    break;
                case BookEntry.SUPPLIER_3:
                    mSupplier.setText(getResources().getStringArray(R.array.array_supplier_options)[3]);
                    mSupplierPhone = getResources().getStringArray(R.array.array_supplier_phone)[3];
                    break;
                case BookEntry.SUPPLIER_4:
                    mSupplier.setText(getResources().getStringArray(R.array.array_supplier_options)[4]);
                    mSupplierPhone = getResources().getStringArray(R.array.array_supplier_phone)[4];
                    break;
                default:
                    mSupplier.setText(getResources().getString(R.string.supplier_unknown));
                    mSupplierPhone = "0";
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Edit" menu option
            case R.id.action_edit:
                // Go to {@link EditorActivity}
                Intent editIntent = new Intent(this, EditorActivity.class);
                editIntent.setData(mCurrentBookUri);
                startActivity(editIntent);
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                HelperClass.showDeleteConfirmationDialog(this, mCurrentBookUri);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Go back to {@link CatalogActivity}.

                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mTitle.setText(getResources().getString(R.string.currently_unavailable));
        mAuthor.setText(getResources().getString(R.string.currently_unavailable));
        mQuantity.setText(getResources().getString(R.string.unknown_quantity));
        mSupplier.setText(getResources().getString(R.string.currently_unavailable));
        mPrice.setText(getResources().getString(R.string.unknown_price));
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
    }
}