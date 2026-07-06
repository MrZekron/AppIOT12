package com.example.appiot12.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private static final String PREFS_NAME = "app_theme_prefs";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private ThemeManager() {}

    public static void applySavedTheme(Context context) {
        AppCompatDelegate.setDefaultNightMode(resolveNightMode(context));
    }

    public static boolean isDarkModeEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkModeEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        AppCompatDelegate.setDefaultNightMode(
                enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static boolean toggleDarkMode(Context context) {
        boolean enabled = !isDarkModeEnabled(context);
        setDarkModeEnabled(context, enabled);
        return enabled;
    }

    private static int resolveNightMode(Context context) {
        return isDarkModeEnabled(context)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
