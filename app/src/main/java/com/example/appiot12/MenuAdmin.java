package com.example.appiot12; // 📦 Aquí vive nuestro super panel de admin

import android.content.Intent; // 🚪 Para movernos entre pantallas
import android.os.Bundle; // 🎒 Datos que recibe la Activity
import android.view.View; // 👆 Para detectar clics
import android.widget.Button; // 🔘 Botones del panel
import android.widget.Toast; // 🍞 Mensajitos

import androidx.activity.EdgeToEdge; // 📱 UI moderna
import androidx.appcompat.app.AppCompatActivity; // 🏛️ Base de la Activity
import androidx.core.graphics.Insets; // 📐 Bordes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth; // 🔐 Para cerrar sesión

/**
 * 🌟 MENU ADMINISTRADOR 🌟
 *
 * Este es el centro de mando del emperador supremo de AguaSegura 👑💧.
 *
 * Desde aquí, el admin puede:
 * - Gestionar usuarios
 * - Ver todos los tanques del sistema
 * - Revisar historial global
 * - Configurar su panel
 * - Cerrar sesión (cuando ya no quiere gobernar más 😎)
 */

public class MenuAdmin extends AppCompatActivity {

    // 🔘 Botones que encontraremos en el XML
    private Button btnGestionUsuarios, btnTanquesGlobales, btnHistorialAdmin, btnConfigAdmin, btnCerrarSesionAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_admin);

        // 🧱 Ajustes para que nada se esconda detrás de la barra superior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🎯 Conectar botones del XML con Java
        btnGestionUsuarios = findViewById(R.id.btnGestionUsuarios);
        btnTanquesGlobales = findViewById(R.id.btnTanquesGlobales);
        btnHistorialAdmin = findViewById(R.id.btnHistorialAdmin);
        btnConfigAdmin = findViewById(R.id.btnConfigAdmin);
        btnCerrarSesionAdmin = findViewById(R.id.btnCerrarSesionAdmin);

        // 🧠 Asignar funciones a los botones
        configurarListeners();
    }

    // 🛠 Aquí conectamos cada botón con su acción correspondiente
    private void configurarListeners() {

        // ⭐ GESTIÓN DE USUARIOS ⭐
        btnGestionUsuarios.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo gestor de usuarios...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, GestionUsuarios.class));
            // ⚠️ Esta Activity debes crearla después
        });

        // ⭐ TANQUES GLOBALES ⭐
        btnTanquesGlobales.setOnClickListener(v -> {
            Toast.makeText(this, "Cargando todos los tanques del sistema...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ListaGlobalTanques.class));
            // ⚠️ También debes crear esta pantalla
        });

        // ⭐ HISTORIAL ⭐
        btnHistorialAdmin.setOnClickListener(v -> {
            Toast.makeText(this, "Mostrando historial global...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HistorialGlobal.class));
            // ⚠️ Para implementar después
        });

        // ⭐ CONFIGURACIÓN ADMIN ⭐
        btnConfigAdmin.setOnClickListener(v -> {
            Toast.makeText(this, "Abriendo configuración...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Configuracion.class));
        });

        // ⭐ CERRAR SESIÓN ⭐
        btnCerrarSesionAdmin.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // 🔐 Bye bye sesión
            Toast.makeText(this, "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show();

            // Volver a la pantalla principal
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
