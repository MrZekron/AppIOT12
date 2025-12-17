package com.example.appiot12;
// 📦 Pantalla Editor del proyecto Agua Segura.
// Aquí se editan los datos de un tanque existente 💧✏️

// ===== IMPORTS ANDROID =====
import android.content.Intent;      // 🚪 Recibir datos desde otra pantalla
import android.os.Bundle;           // 🎒 Estado de la Activity
import android.view.View;           // 👆 Eventos de botones
import android.widget.EditText;     // 📝 Campos editables
import android.widget.Toast;        // 🍞 Mensajes rápidos al usuario

import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity base

// ===== IMPORTS FIREBASE =====
import com.google.firebase.FirebaseApp;        // 🚀 Inicializar Firebase
import com.google.firebase.auth.FirebaseAuth; // 🔐 Autenticación
import com.google.firebase.auth.FirebaseUser; // 👤 Usuario actual
import com.google.firebase.database.DatabaseReference; // 🗂 Referencia a nodo
import com.google.firebase.database.FirebaseDatabase;  // ☁️ Base de datos

// ===== IMPORTS JAVA =====
import java.util.HashMap;
import java.util.Map;
// 🧱 Mapas para actualizar solo lo necesario (sin sobrescribir todo)

/**
 * 🛠️ Editor
 *
 * ¿Qué hace esta pantalla?
 * 👉 Permite editar un tanque existente
 * 👉 Cambiar nombre, capacidad y color
 * 👉 Guardar SOLO los cambios en Firebase
 *
 * Explicado para un niño:
 * 👉 Es como corregir una ficha sin borrar el cuaderno completo 📒✏️
 */
public class Editor extends AppCompatActivity {

    // 📝 Campos editables del tanque
    private EditText etNombre;
    private EditText etCapacidad;
    private EditText etColor;

    // 🆔 ID del tanque que estamos editando
    private String idTanque;

    // ☁️ Referencia a /usuarios/{uid}/tanques
    private DatabaseReference refTanques;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor); // 🎨 Mostramos la pantalla

        // ⚡ Aseguramos que Firebase esté listo
        FirebaseApp.initializeApp(this);

        // 👤 Verificamos usuario autenticado
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔑 UID del usuario
        String uid = user.getUid();

        // 🗂 Apuntamos al nodo de tanques del usuario
        refTanques = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");

        // 🔗 Conectamos los EditText con el XML
        inicializarVistas();

        // 📥 Cargamos los datos enviados desde la pantalla anterior
        cargarDatosIntent();
    }

    /**
     * 🔗 Vincula los EditText con el XML
     */
    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etCapacidad = findViewById(R.id.etCapacidad);
        etColor = findViewById(R.id.etColor);
    }

    /**
     * 📥 Lee los datos enviados por Intent y los muestra en pantalla
     */
    private void cargarDatosIntent() {

        Intent intent = getIntent();

        idTanque = intent.getStringExtra("tanqueId");
        String nombre = intent.getStringExtra("tanqueNombre");
        String capacidad = intent.getStringExtra("tanqueCapacidad");
        String color = intent.getStringExtra("tanqueColor");

        // ✍️ Mostramos valores actuales (si vienen nulos, dejamos vacío)
        etNombre.setText(nombre != null ? nombre : "");
        etCapacidad.setText(capacidad != null ? capacidad : "");
        etColor.setText(color != null ? color : "");
    }

    // =====================================================
    // 💾 GUARDAR CAMBIOS DEL TANQUE
    // =====================================================
    public void guardarTanque(View view) {

        // 📥 Leemos los nuevos valores
        String nombre = etNombre.getText().toString().trim();
        String capacidad = etCapacidad.getText().toString().trim();
        String color = etColor.getText().toString().trim();

        // 🛑 Validación básica
        if (nombre.isEmpty() || capacidad.isEmpty() || color.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos 📝", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idTanque == null) {
            Toast.makeText(this, "Error: tanque no identificado ❌", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🧱 Creamos un mapa SOLO con lo que cambia
        Map<String, Object> cambios = new HashMap<>();
        cambios.put("nombre", nombre);
        cambios.put("capacidad", capacidad);
        cambios.put("color", color);

        // ☁️ Actualizamos el tanque en Firebase
        refTanques.child(idTanque)
                .updateChildren(cambios)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cambios guardados ✔️", Toast.LENGTH_SHORT).show();
                    finish(); // 🚪 Volvemos atrás
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Error al guardar ❌",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // =====================================================
    // 🔙 CANCELAR Y VOLVER
    // =====================================================
    public void volverAlMenu(View view) {
        finish(); // 🚪 Salimos sin guardar cambios
    }
}
