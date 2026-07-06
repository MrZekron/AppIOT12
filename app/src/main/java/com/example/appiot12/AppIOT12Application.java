package com.example.appiot12;

import android.app.Application;

import com.example.appiot12.util.ThemeManager;

public class AppIOT12Application extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
    }
}
