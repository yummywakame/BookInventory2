package com.yummywakame.bookinventory2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

/**
 * Allows user to view the details of a book but not edit it.
 */
public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {

    // Variables needed to appear to "move" the FAB to the bottom on scroll
    private View mFabTop;
    private View mFabBottom;

    public static void start(Context c) {
        c.startActivity(new Intent(c, DetailActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
//        getSupportActionBar().setTitle("Details");

        // Locate FAB buttons
        mFabTop = findViewById(R.id.fab_call_supplier_top);
        mFabBottom = findViewById(R.id.fab_call_supplier_bottom);
        // Hide bottom FAB button
        mFabBottom.setVisibility(View.INVISIBLE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        AppBarLayout appbar = findViewById(R.id.appbar);
        appbar.addOnOffsetChangedListener(this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0) {
            //  Collapsed
            ViewCompat.animate(mFabTop).scaleY(0).scaleX(0).start();
            mFabBottom.setVisibility(View.VISIBLE);
            ViewCompat.animate(mFabBottom).scaleY(1).scaleX(1).start();
        } else {
            //Expanded
            ViewCompat.animate(mFabTop).scaleY(1).scaleX(1).start();
            ViewCompat.animate(mFabBottom).scaleY(0).scaleX(0).start();
        }

    }
}