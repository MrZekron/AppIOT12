package com.example.appiot12; // 📦 paquete

import android.content.DialogInterface; // 🗨️ diálogos
import android.content.Intent; // 🚪 navegación
import android.os.Bundle; // 🎒 estado
import android.view.View; // 👆 evento
import android.widget.Button; // 🔘 botón
import android.widget.TextView; // ✏️ texto
import android.widget.Toast; // 🍞 aviso

import androidx.activity.EdgeToEdge; // ↔️ UI completa
import androidx.appcompat.app.AlertDialog; // ⚠️ diálogo confirmación
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity
import androidx.core.graphics.Insets; // 📐 bordes
import androidx.core.view.ViewCompat; // 🛠 utilidades
import androidx.core.view.WindowInsetsCompat; // 🪟 bordes sistema

import com.google.firebase.auth.FirebaseAuth; // 🔐 auth
import com.google.firebase.auth.FirebaseUser; // 👤 usuario
import com.google.firebase.database.FirebaseDatabase; // 💾 realtime db

public class Configuracion extends AppCompatActivity {

    private FirebaseAuth mAuth; // 🔐 autenticación
    private TextView tvCorreoConfig, tvUidConfig;
    private Button btnEditarPerfil, btnHistorial, btnEliminarCuenta, btnVolverMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracion);

        // 🛠 Ajustar bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v,insets)->{
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left,sb.top,sb.right,sb.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // 🔗 Conectar elementos XML
        tvCorreoConfig = findViewById(R.id.tvCorreoConfig);
        tvUidConfig = findViewById(R.id.tvUidConfig);

        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCuenta);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);

        cargarDatosUsuario();

        // ⚙️ EDITAR PERFIL
        btnEditarPerfil.setOnClickListener(v -> {
            Toast.makeText(this, "Abrir pantalla: Editar Perfil ⚙️", Toast.LENGTH_SHORT).show();
            // Aquí deberás crear tu EditarPerfilActivity
        });

        // 📜 HISTORIAL
        btnHistorial.setOnClickListener(v -> {
            Toast.makeText(this, "Abrir Historial 📜", Toast.LENGTH_SHORT).show();
            // Aquí deberás crear tu HistorialActivity
        });

        // ☠️ ELIMINAR CUENTA
        btnEliminarCuenta.setOnClickListener(v -> mostrarDialogoEliminar());

        // 🔙 VOLVER
        btnVolverMenu.setOnClickListener(v -> finish());
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            tvCorreoConfig.setText("Correo: " + user.getEmail());
            tvUidConfig.setText("UID: " + user.getUid());
        }
    }

    private void mostrarDialogoEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta ☠️")
                .setMessage("¿Seguro que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarCuenta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarCuenta() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Error: no hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1️⃣ Borrar datos de la base de datos
        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(user.getUid())
                .removeValue();

        // 2️⃣ Borrar la cuenta del Authentication
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show();

                mAuth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "No se pudo eliminar la cuenta.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
