package com.example.appiot12.ui.tanque;
// 📦 Activity destinada a mostrar TODOS los tanques registrados en el sistema.

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;

/**
 * 🚀 LISTA GLOBAL DE TANQUES
 */
public class ListaGlobalTanques extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_global_tanques);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        mostrarMensajeEnConstruccion();
    }

    private void mostrarMensajeEnConstruccion() {
        Toast.makeText(
                this,
                "Módulo en construcción 🚧\nPróximamente: Panel Global de Tanques 💧📊",
                Toast.LENGTH_LONG
        ).show();
    }
}
