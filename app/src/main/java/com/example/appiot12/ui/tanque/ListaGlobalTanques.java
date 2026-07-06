package com.example.appiot12.ui.tanque;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.example.appiot12.ui.BaseActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;

public class ListaGlobalTanques extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_lista_global_tanques);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        Toast.makeText(this, "Módulo en construcción. Próximamente: Panel Global de Tanques.", Toast.LENGTH_LONG).show();
    }
}
