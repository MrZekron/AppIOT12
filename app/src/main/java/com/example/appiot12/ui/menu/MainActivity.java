package com.example.appiot12.ui.menu;
// 📦 Activity inicial del sistema AguaSegura.
// Pantalla de bienvenida + tareas de mantenimiento ligero 👋💧

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.service.HistorialService;
import com.example.appiot12.ui.auth.IniciarSesion;

/**
 * 🏠 MAIN ACTIVITY
 *
 * Rol en la arquitectura:
 * 👉 Pantalla de entrada
 * 👉 No maneja lógica de negocio
 * 👉 Ejecuta limpieza automática del historial (30 días)
 *
 * Principios:
 * ✔ Simple
 * ✔ Clara
 * ✔ Sin sobrecargar responsabilidades
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 📱 Modo pantalla completa moderno
        EdgeToEdge.enable(this);

        // 🎨 Layout principal
        setContentView(R.layout.activity_main);

        // 📐 Ajuste automático para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        // =========================
        // 🧹 MANTENIMIENTO AUTOMÁTICO
        // =========================
        // Elimina eventos del historial con más de 30 días
        // (sensores, pagos, compras, tanques antiguos)
        HistorialService.limpiarHistorialAntiguo();
    }

    /**
     * ▶️ iniciar()
     *
     * Conectado al botón del XML:
     * android:onClick="iniciar"
     *
     * Redirige al login 🔐
     */
    public void iniciar(View view) {
        startActivity(new Intent(this, IniciarSesion.class));
    }
}
