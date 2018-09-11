package com.yummywakame.bookinventory2;

import android.app.Application;
import android.content.Context;

/**
 * BookInventory2
 * Created by Olivia Meiring on 2018/09/10.
 * Yummy Wakame
 * olivia@yummy-wakame.com
 */
public class MyApplication extends Application {

    private static Context context;

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }
}
