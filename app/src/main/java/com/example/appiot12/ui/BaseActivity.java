package com.example.appiot12.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.example.appiot12.util.ThemeToggleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        bindThemeToggle();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        bindThemeToggle();
    }

    private void bindThemeToggle() {
        ImageButton button = findViewById(R.id.btnToggleTema);
        if (button != null) {
            ThemeToggleHelper.setup(this, button);
        }
    }
}
