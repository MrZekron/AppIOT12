package com.example.appiot12;
// 📦 Paquete base del proyecto Agua Segura.
// Aquí viven las pantallas de configuración y gestión de cuenta 🏢⚙️👤

// ===== IMPORTS ANDROID / UI =====
import android.content.Intent;          // 🚪 Navegación entre pantallas
import android.os.Bundle;               // 🎒 Estado de la Activity
import android.widget.Button;           // 🔘 Botones
import android.widget.TextView;         // ✏️ Textos informativos
import android.widget.Toast;            // 🍞 Mensajes cortos al usuario

import androidx.appcompat.app.AlertDialog;   // ⚠️ Diálogo de confirmación
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity base estable

// ===== IMPORTS FIREBASE =====
import com.google.firebase.auth.FirebaseAuth; // 🔐 Autenticación
import com.google.firebase.auth.FirebaseUser; // 👤 Usuario actual
import com.google.firebase.database.FirebaseDatabase; // ☁️ Base de datos

/**
 * ⚙️ Configuracion
 *
 * Esta pantalla permite:
 * 👉 Ver correo y UID del usuario
 * 👉 Acceder al historial de acciones
 * 👉 Eliminar la cuenta
 * 👉 Volver al menú principal
 *
 * En simple:
 * Es el panel de control personal del usuario 🧑‍💼🧩
 */
public class Configuracion extends AppCompatActivity {

    // 🔐 Firebase Auth
    private FirebaseAuth auth;

    // ✏️ Textos informativos
    private TextView tvCorreoConfig;
    private TextView tvUidConfig;

    // 🔘 Botones de acción
    private Button btnEditarPerfil;
    private Button btnHistorial;
    private Button btnEliminarCuenta;
    private Button btnVolverMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion); // 🎨 Mostramos la pantalla

        // 🔐 Inicializamos autenticación
        auth = FirebaseAuth.getInstance();

        // 🔗 Conectamos UI con el XML
        inicializarVistas();

        // 📥 Cargamos datos del usuario
        cargarDatosUsuario();

        // ⚙️ Configuramos acciones de botones
        configurarBotones();
    }

    /**
     * 🔗 Conecta los elementos visuales con el XML
     */
    private void inicializarVistas() {
        tvCorreoConfig = findViewById(R.id.tvCorreoConfig);
        tvUidConfig = findViewById(R.id.tvUidConfig);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);
    }

    /**
     * 📥 Muestra correo y UID del usuario autenticado
     */
    private void cargarDatosUsuario() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No hay sesión activa ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 📧 Mostramos correo
        tvCorreoConfig.setText("Correo: " + user.getEmail());

        // 🔑 Mostramos UID
        tvUidConfig.setText("UID: " + user.getUid());
    }

    /**
     * ⚙️ Configura el comportamiento de los botones
     */
    private void configurarBotones() {

        // 🛠 Editar perfil (placeholder)
        btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Editar perfil (próximamente) ⚙️",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // 📜 Ir al historial de acciones
        btnHistorial.setOnClickListener(v ->
                startActivity(new Intent(this, HistorialAcciones.class))
        );

        // ☠️ Eliminar cuenta
        btnEliminarCuenta.setOnClickListener(v ->
                mostrarDialogoEliminar()
        );

        // 🔙 Volver al menú
        btnVolverMenu.setOnClickListener(v -> finish());
    }

    // =====================================================
    // ☠️ DIÁLOGO DE CONFIRMACIÓN
    // =====================================================
    private void mostrarDialogoEliminar() {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta ☠️")
                .setMessage(
                        "¿Seguro que deseas eliminar tu cuenta?\n" +
                                "Esta acción NO se puede deshacer ⚠️"
                )
                .setPositiveButton("Sí, eliminar", (dialog, which) ->
                        eliminarCuenta()
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // =====================================================
    // 🔥 ELIMINAR CUENTA (DATOS + AUTH)
    // =====================================================
    private void eliminarCuenta() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Error: no hay sesión activa ❌", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        // 🧹 1) Eliminamos todos los datos del usuario en Firebase
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .removeValue();

        // 🔐 2) Eliminamos la cuenta de autenticación
        user.delete().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Toast.makeText(
                        this,
                        "Cuenta eliminada correctamente ✔️",
                        Toast.LENGTH_LONG
                ).show();

                auth.signOut(); // 🚪 Cerramos sesión

                // 🏠 Volvemos al inicio
                startActivity(new Intent(this, MainActivity.class));
                finish();

            } else {
                Toast.makeText(
                        this,
                        "No se pudo eliminar la cuenta ⚠️",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
