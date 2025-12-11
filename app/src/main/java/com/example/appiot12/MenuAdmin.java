package com.example.appiot12;
// 📦 Pantalla oficial del ADMINISTRADOR del sistema AguaSegura
// El “Command Center” donde el admin domina absolutamente todo 😎🚀

import android.content.Intent; // 🚪 Para navegar entre módulos
import android.os.Bundle; // 🎒 Estado persistente de la Activity
import android.view.View; // 👆 Manejar clics en botones
import android.widget.Button; // 🔘 Botones del menú
import android.widget.Toast; // 🍞 Mensajes sutiles informativos

import androidx.activity.EdgeToEdge; // 📱 UI moderna edge-to-edge
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity base
import androidx.core.graphics.Insets; // 📐 Márgenes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 🌟 MENU ADMINISTRADOR 🌟
 *
 * Este panel permite al administrador gestionar:
 *   ✔ Usuarios del sistema
 *   ✔ Estadísticas globales (tanques, dispositivos, usuarios)
 *   ✔ Configuración general
 *
 * Es la interfaz desde donde se gobierna todo el ecosistema AguaSegura 💧🔧💼
 */
public class MenuAdmin extends AppCompatActivity {

    // Botones del panel principal
    private Button btnGestionUsuarios;
    private Button btnHistorialGlobal;
    private Button btnConfigAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this); // 📱 Activar diseño de pantalla completa
        setContentView(R.layout.activity_menu_admin); // 🎨 Dibujar layout

        // Ajuste automático para evitar choque con barras superior/inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // 🔗 Vincular elementos UI del XML
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnHistorialGlobal = findViewById(R.id.btnHistorialGlobal); // ✔ Botón añadido
        btnConfigAdmin = findViewById(R.id.btnConfigAdmin);

        // Configurar acciones de los botones
        configurarListeners();
    }

    // =========================================================
    // 🎛️ CONFIGURACIÓN DE EVENTOS DE BOTONES
    // =========================================================
    private void configurarListeners() {

        // ⭐ GESTIÓN DE USUARIOS
        btnGestionUsuarios.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo gestión de usuarios...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, GestionUsuarios.class));
            // 🎯 Módulo donde se listan y gestionan usuarios
        });

        // ⭐ HISTORIAL GLOBAL
        btnHistorialGlobal.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo historial global...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HistorialGlobal.class));
            // 🚀 Dashboard con gráficas globales (usuarios/tanques/dispositivos)
        });

        // ⭐ CONFIGURACIÓN DEL SISTEMA
        btnConfigAdmin.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo configuración...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Configuracion.class));
            // ⚙️ Configuración general del admin
        });
    }
}
