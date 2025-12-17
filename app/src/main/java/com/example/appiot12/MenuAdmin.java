package com.example.appiot12;
// 📦 Menú principal del ADMIN en AguaSegura.
// Este es el “centro de mando” donde el admin controla el sistema 💼🚀

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 🌟 MENU ADMINISTRADOR 🌟
 *
 * Explicado para un niño 👶:
 * 👉 Esta pantalla es como la sala de control 🎮
 * 👉 Desde aquí el admin puede mirar y ordenar todo
 * 👉 No guarda datos, no calcula nada, solo abre otras pantallas 🚪
 *
 * REGLA DE ORO:
 * 👉 Si empieza a tener lógica → se mueve a un Controller ❌
 */
public class MenuAdmin extends AppCompatActivity {

    // 🔘 Botones del panel admin
    private Button btnGestionUsuarios;
    private Button btnHistorialGlobal;
    private Button btnConfigAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 📱 Activamos UI moderna a pantalla completa
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_admin);

        // 📐 Ajustar márgenes para no chocar con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                    return insets;
                }
        );

        // 🔗 Vincular botones del XML
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnHistorialGlobal = findViewById(R.id.btnHistorialGlobal);
        btnConfigAdmin = findViewById(R.id.btnConfigAdmin);

        configurarAcciones(); // 🎛️ Activar navegación
    }

    // =========================================================
    // 🚀 MÉTODO GENÉRICO PARA NAVEGAR ENTRE PANTALLAS
    // =========================================================
    private void irA(Class<?> destino, String mensaje) {

        // 🧠 Mensaje UX opcional (feedback inmediato)
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();

        // 🚪 Navegación limpia
        startActivity(new Intent(this, destino));
    }

    // =========================================================
    // 🎛️ CONFIGURAR ACCIONES DE LOS BOTONES
    // =========================================================
    private void configurarAcciones() {

        // 👥 Gestión de usuarios
        btnGestionUsuarios.setOnClickListener(v ->
                irA(GestionUsuarios.class, "Abriendo gestión de usuarios 👥")
        );

        // 📊 Historial / métricas globales
        btnHistorialGlobal.setOnClickListener(v ->
                irA(HistorialGlobal.class, "Abriendo estadísticas globales 📊")
        );

        // ⚙️ Configuración general
        btnConfigAdmin.setOnClickListener(v ->
                irA(Configuracion.class, "Abriendo configuración ⚙️")
        );
    }
}
