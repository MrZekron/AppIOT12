package com.example.appiot12.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import com.example.appiot12.ui.BaseActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.ui.admin.ConfirmarPagosAdmin;
import com.example.appiot12.ui.admin.GestionUsuarios;
import com.example.appiot12.ui.admin.SeedTestData;
import com.example.appiot12.ui.admin.GraficosAdmin;
import com.example.appiot12.ui.perfil.Configuracion;

public class MenuAdmin extends BaseActivity {

    private View cardGestionUsuarios;
    private View cardConfigAdmin;
    private View cardGraficos;
    private View cardConfirmarPagos;
    private Button btnSeedData;

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

        cardGestionUsuarios = findViewById(R.id.cardGestionUsuarios);
        cardConfigAdmin     = findViewById(R.id.cardConfigAdmin);
        cardGraficos        = findViewById(R.id.cardGraficos);
        cardConfirmarPagos  = findViewById(R.id.cardConfirmarPagos);
        btnSeedData         = findViewById(R.id.btnSeedData);

        configurarAcciones();
    }

    private void irA(Class<?> destino, String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, destino));
    }

    private void configurarAcciones() {
        cardGestionUsuarios.setOnClickListener(v -> irA(GestionUsuarios.class, "Abriendo gestión de usuarios"));
        cardConfigAdmin.setOnClickListener(v     -> irA(Configuracion.class, "Abriendo configuración"));
        cardGraficos.setOnClickListener(v        -> irA(GraficosAdmin.class, "Abriendo gráficos"));
        cardConfirmarPagos.setOnClickListener(v  -> irA(ConfirmarPagosAdmin.class, "Abriendo comprobantes"));
        btnSeedData.setOnClickListener(v         -> SeedTestData.sembrar(this));
    }
}
