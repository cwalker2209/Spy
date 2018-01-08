package com.example.cwalker2209.spy;

import android.app.Application;

import java.util.ArrayList;

public class App extends Application {

    public static App INSTANCE;
    public float score;
    public boolean newColor;
    public String lastItem;

    public static App get() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        score = 0;
        newColor = false;
    }
}
