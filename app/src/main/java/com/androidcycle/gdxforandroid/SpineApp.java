package com.androidcycle.gdxforandroid;

import android.app.Application;

public class SpineApp extends Application {
    private static SpineApp spineApp;
    @Override
    public void onCreate() {
        super.onCreate();
        spineApp = this;
    }

    public static SpineApp getInstance() {
        return spineApp;
    }
}
