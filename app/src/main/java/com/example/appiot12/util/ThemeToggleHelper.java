package com.example.appiot12.util;

import android.content.Context;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;

public final class ThemeToggleHelper {

    private ThemeToggleHelper() {}

    public static void setup(AppCompatActivity activity, ImageButton button) {
        updateIcon(activity, button);
        button.setOnClickListener(v -> {
            ThemeManager.toggleDarkMode(activity);
            activity.recreate();
        });
    }

    public static void updateIcon(Context context, ImageButton button) {
        boolean dark = ThemeManager.isDarkModeEnabled(context);
        button.setImageResource(dark ? R.drawable.ic_modo_claro : R.drawable.ic_modo_oscuro);
        button.setContentDescription(dark ? "Cambiar a modo claro" : "Cambiar a modo oscuro");
    }
}
