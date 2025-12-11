package com.example.appiot12; // 📦 Aquí vive nuestro super panel de admin

import android.content.Intent; // 🚪 Navegación entre Activities
import android.os.Bundle; // 🎒 Estado de la Activity
import android.view.View; // 👆 Eventos de clic
import android.widget.Button; // 🔘 Botones del panel
import android.widget.Toast; // 🍞 Mensajes rápidos

import androidx.activity.EdgeToEdge; // 📱 UI moderna de extremo a extremo
import androidx.appcompat.app.AppCompatActivity; // 🏛 Base de actividades
import androidx.core.graphics.Insets; // 📐 Márgenes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 🌟 MENU ADMINISTRADOR 🌟
 *
 * Panel maestro que permite gestionar:
 * - Usuarios
 * - Historial Global
 * - Configuración general
 */

public class MenuAdmin extends AppCompatActivity {

    private Button btnGestionUsuarios, btnHistorialGlobal, btnConfigAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_admin);

        // 🧱 Ajuste de márgenes para evitar choques con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // 🔗 Conectar botones con el XML
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnHistorialGlobal = findViewById(R.id.btnHistorialGlobal); // ← NUEVO BOTÓN
        btnConfigAdmin = findViewById(R.id.btnConfigAdmin);

        configurarListeners();
    }

    // 🎛 Configurar comportamiento de botones
    private void configurarListeners() {

        // ⭐ GESTIÓN DE USUARIOS
        btnGestionUsuarios.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo gestión de usuarios...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, GestionUsuarios.class));
        });

        // ⭐ HISTORIAL GLOBAL
        btnHistorialGlobal.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo historial global...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HistorialGlobal.class));
        });

        // ⭐ CONFIGURACIÓN
        btnConfigAdmin.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo configuración...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Configuracion.class));
        });
    }
}
