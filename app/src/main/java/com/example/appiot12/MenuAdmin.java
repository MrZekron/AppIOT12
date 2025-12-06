package com.example.appiot12; // 📦 Aquí vive nuestro super panel de admin

import android.content.Intent; // 🚪 Para movernos entre pantallas
import android.os.Bundle; // 🎒 Datos que recibe la Activity
import android.view.View; // 👆 Para detectar clics
import android.widget.Button; // 🔘 Botones del panel
import android.widget.Toast; // 🍞 Mensajitos

import androidx.activity.EdgeToEdge; // 📱 UI moderna
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity base
import androidx.core.graphics.Insets; // 📐 Bordes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 🌟 MENU ADMINISTRADOR 🌟
 *
 * Aquí el emperador del sistema maneja:
 * - Gestión de usuarios
 * - Configuración del sistema
 *
 * (Versión reducida según tus nuevos requisitos)
 */

public class MenuAdmin extends AppCompatActivity {

    private Button btnGestionUsuarios, btnConfigAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_admin);

        // 🧱 Ajuste de márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔗 Conexión de botones reales del XML
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnConfigAdmin = findViewById(R.id.btnConfigAdmin);

        configurarListeners();
    }

    // 🔧 Listener de botones
    private void configurarListeners() {

        // ⭐ GESTIONAR USUARIOS ⭐
        btnGestionUsuarios.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo gestor de usuarios...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, GestionUsuarios.class));
        });

        // ⭐ CONFIGURACIÓN ⭐
        btnConfigAdmin.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo configuración...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Configuracion.class));
        });
    }
}
