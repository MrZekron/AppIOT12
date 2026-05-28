package com.example.appiot12.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.ui.admin.GestionUsuarios;
import com.example.appiot12.ui.admin.GraficosAdmin;
import com.example.appiot12.ui.admin.HistorialGlobal;
import com.example.appiot12.ui.perfil.Configuracion;

public class MenuAdmin extends AppCompatActivity {

    private Button btnGestionUsuarios;
    private Button btnHistorialGlobal;
    private Button btnConfigAdmin;
    private Button btnGraficos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnHistorialGlobal = findViewById(R.id.btnHistorialGlobal);
        btnConfigAdmin     = findViewById(R.id.btnConfigAdmin);
        btnGraficos        = findViewById(R.id.btnGraficos);

        configurarAcciones();
    }

    private void irA(Class<?> destino, String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, destino));
    }

    private void configurarAcciones() {
        btnGestionUsuarios.setOnClickListener(v -> irA(GestionUsuarios.class, "Abriendo gestión de usuarios"));
        btnHistorialGlobal.setOnClickListener(v -> irA(HistorialGlobal.class, "Abriendo estadísticas globales"));
        btnConfigAdmin.setOnClickListener(v    -> irA(Configuracion.class, "Abriendo configuración"));
        btnGraficos.setOnClickListener(v       -> irA(GraficosAdmin.class, "Abriendo gráficos"));
    }
}
