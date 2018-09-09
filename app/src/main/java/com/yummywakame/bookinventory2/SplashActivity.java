package com.yummywakame.bookinventory2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * BookInventory2
 * Created by Olivia Meiring on 2018/09/08.
 * Yummy Wakame
 * olivia@yummy-wakame.com
 */
public class SplashActivity extends AppCompatActivity {
    private static final int SECONDS_DELAY = 5000;
    private ImageView gifImaveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Show animated gif logo using Glide
        gifImaveView = findViewById(R.id.logo);
        Glide.with(this).asGif()
                .load(R.drawable.animated_logo)
                .into(gifImaveView);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // After 5 seconds go to next activity
                launchNextActivity();
            }
        }, SECONDS_DELAY);   // 5 seconds

    }

    // Launches Catalog Activity
    private void launchNextActivity() {
        Intent intent = new Intent(this, CatalogActivity.class);
        startActivity(intent);
        finish();
    }
}