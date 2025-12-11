package com.example.appiot12; // 📦 Paquete base de la app

// ===== IMPORTS UI Y ANDROID =====
import android.content.DialogInterface; // 🗨️ Manejo de diálogos tradicionales
import android.content.Intent; // 🚪 Navegación entre Activities
import android.os.Bundle; // 🎒 Estado guardado
import android.view.View; // 👆 Eventos de interacción del usuario
import android.widget.Button; // 🔘 Botones
import android.widget.TextView; // ✏️ Textos para mostrar info
import android.widget.Toast; // 🍞 Mensajes rápidos para el usuario

import androidx.activity.EdgeToEdge; // ↔️ UI adaptada a toda la pantalla
import androidx.appcompat.app.AlertDialog; // ⚠️ Diálogo de confirmación
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity principal
import androidx.core.graphics.Insets; // 📐 Manejo de bordes
import androidx.core.view.ViewCompat; // 🛠 Herramientas de vista
import androidx.core.view.WindowInsetsCompat; // 🪟 Insets del sistema

// ===== IMPORTS FIREBASE =====
import com.google.firebase.auth.FirebaseAuth; // 🔐 Manejo de autenticación
import com.google.firebase.auth.FirebaseUser; // 👤 Información del usuario
import com.google.firebase.database.FirebaseDatabase; // 💾 Realtime Database

public class Configuracion extends AppCompatActivity {
    // 🧩 Activity que gestiona configuración de cuenta:
    // visualizar datos del usuario, historial, editar perfil y eliminar cuenta.
    // Un mini "panel administrativo" personal 🔧👤

    private FirebaseAuth mAuth; // 🔐 Autenticación actual del usuario
    private TextView tvCorreoConfig, tvUidConfig; // ✏️ Etiquetas que muestran correo y UID
    private Button btnEditarPerfil, btnHistorial, btnEliminarCuenta, btnVolverMenu; // 🔘 Botones de acción

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Habilita UI fullscreen profesional
        setContentView(R.layout.activity_configuracion); // 🖼️ Carga layout asociado

        // 🛠 Ajuste automático de márgenes según barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v,insets)->{
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left,sb.top,sb.right,sb.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance(); // 🔐 Obtenemos instancia de autenticación

        // 🔗 Vincular elementos visuales con XML
        tvCorreoConfig = findViewById(R.id.tvCorreoConfig);
        tvUidConfig = findViewById(R.id.tvUidConfig);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);

        cargarDatosUsuario(); // 📥 Carga correo + UID en etiquetas

        // ========================
        // ⚙️ BOTÓN EDITAR PERFIL
        // ========================
        btnEditarPerfil.setOnClickListener(v -> {
            Toast.makeText(this, "Abrir pantalla: Editar Perfil ⚙️", Toast.LENGTH_SHORT).show();
            // Aquí se debería abrir EditarPerfilActivity cuando exista
        });

        // ========================
        // 📜 HISTORIAL
        // ========================
        btnHistorial.setOnClickListener(v -> {
            Intent i = new Intent(this, HistorialAcciones.class); // 🚪 Ir al historial
            startActivity(i);
        });

        // ========================
        // ☠️ ELIMINAR CUENTA
        // ========================
        btnEliminarCuenta.setOnClickListener(v -> mostrarDialogoEliminar());

        // ========================
        // 🔙 VOLVER AL MENÚ
        // ========================
        btnVolverMenu.setOnClickListener(v -> finish()); // Cierra esta activity y vuelve atrás
    }

    // ========================================================
    // 📥 Cargar datos del usuario autenticado (correo + UID)
    // ========================================================
    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser(); // Obtenemos sesión activa

        if (user != null) {
            tvCorreoConfig.setText("Correo: " + user.getEmail()); // 📧 Mostrar correo
            tvUidConfig.setText("UID: " + user.getUid()); // 🔑 Mostrar UID
        }
    }

    // ========================================================
    // ☠️ Diálogo de confirmación: "¿Eliminar cuenta?"
    // ========================================================
    private void mostrarDialogoEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta ☠️") // ⚠️ Advertencia clara
                .setMessage("¿Seguro que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarCuenta()) // Acciona borrado
                .setNegativeButton("Cancelar", null) // ❌ Cancela operación
                .show();
    }

    // ========================================================
    // 🔥 ELIMINAR CUENTA: FirebaseAuth + FirebaseDatabase
    // ========================================================
    private void eliminarCuenta() {
        FirebaseUser user = mAuth.getCurrentUser(); // Obtenemos usuario actual

        if (user == null) {
            Toast.makeText(this, "Error: no hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1️⃣ BORRAR datos del usuario en Realtime Database (tanques, dispositivos, todo) 🧹
        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(user.getUid())
                .removeValue();

        // 2️⃣ BORRAR cuenta de autenticación 🔐❌
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) { // ✔️ Eliminación exitosa
                Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show();

                mAuth.signOut(); // 🚪 Cerrar sesión
                startActivity(new Intent(this, MainActivity.class)); // Volver al inicio
                finish();
            } else {
                Toast.makeText(this, "No se pudo eliminar la cuenta.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
