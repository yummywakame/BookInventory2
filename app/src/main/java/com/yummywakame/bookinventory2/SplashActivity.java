package com.yummywakame.bookinventory2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * BookInventory2
 * Created by Olivia Meiring on 2018/09/08.
 * Yummy Wakame
 * olivia@yummy-wakame.com
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, CatalogActivity.class);
        startActivity(intent);
        finish();
    }
}
