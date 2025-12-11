package com.example.appiot12;
// 📦 Aquí vive esta pantalla dentro del ecosistema AguaSegura

// === IMPORTS ANDROID ===
import android.content.Intent; // 🚪 Permite recibir datos desde otra pantalla
import android.os.Bundle; // 🎒 Estado y datos al crear Activity
import android.view.View; // 👆 Detectar clics de botones
import android.widget.EditText; // 📝 Entradas de texto editables
import android.widget.Toast; // 🍞 Mensajes breves al usuario

import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity principal

// === IMPORTS FIREBASE ===
import com.google.firebase.FirebaseApp; // 🚀 Inicializar Firebase
import com.google.firebase.auth.FirebaseAuth; // 🔐 Autenticación del usuario
import com.google.firebase.auth.FirebaseUser; // 👤 Objeto usuario logueado
import com.google.firebase.database.DatabaseReference; // 🗂 Referencia a un nodo
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Base de datos en la nube

// === IMPORTS PARA ESTRUCTURAS ===
import java.util.HashMap; // 🧱 Mapa clave/valor
import java.util.Map; // 📋 Mapa genérico

/**
 * 🛠️ EDITOR DE TANQUES
 * Esta pantalla permite editar los datos de un tanque específico.
 *
 * Es como un **taller mecánico de datos**: cambiamos nombre, capacidad o color
 * sin alterar el resto del modelo en Firebase.
 */
public class Editor extends AppCompatActivity {

    // 📝 Campos editables
    private EditText etNombre, etCapacidad, etColor;

    // 🔗 ID del tanque que estamos editando
    private String tanqueId;

    // 🗺 Referencia al nodo: usuarios/{uid}/tanques
    private DatabaseReference dbrf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor); // 🎨 Dibujamos el layout

        // ⚡ Asegurar que Firebase esté inicializado
        FirebaseApp.initializeApp(this);

        // 🔐 Usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // Si no hay sesión → NO podemos editar nada
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtenemos ID del usuario
        String uid = user.getUid();

        // 🛣 Nos posicionamos en: usuarios/{uid}/tanques
        dbrf = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");

        // 🎯 Vincular variables con XML
        etNombre = findViewById(R.id.etNombre);
        etCapacidad = findViewById(R.id.etCapacidad);
        etColor = findViewById(R.id.etColor);

        // 📥 Recibir datos enviados desde Lista.java (o cualquier otra pantalla)
        Intent intent = getIntent();

        tanqueId   = intent.getStringExtra("tanqueId");        // 🆔 ID del tanque
        String nombre    = intent.getStringExtra("tanqueNombre");
        String capacidad = intent.getStringExtra("tanqueCapacidad");
        String color     = intent.getStringExtra("tanqueColor");

        // ✍️ Mostrar valores actuales en los EditText
        etNombre.setText(nombre != null ? nombre : "");
        etCapacidad.setText(capacidad != null ? capacidad : "");
        etColor.setText(color != null ? color : "");
    }

    // =======================================================================
    // 💾 GUARDAR CAMBIOS — Se ejecuta cuando el usuario presiona “Guardar”
    // =======================================================================
    public void guardarTanque(View view) {

        // 📥 Leer nuevos valores del formulario
        String nombre = etNombre.getText().toString().trim();
        String capacidad = etCapacidad.getText().toString().trim();
        String color = etColor.getText().toString().trim();

        // 🚨 Validación básica
        if (nombre.isEmpty() || capacidad.isEmpty() || color.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🧱 Preparamos un mapa solo con los campos a modificar
        Map<String, Object> tanqueMap = new HashMap<>();
        tanqueMap.put("nombre", nombre);
        tanqueMap.put("capacidad", capacidad);
        tanqueMap.put("color", color);

        // 🔁 Actualizamos el tanque especificado:
        // usuarios/{uid}/tanques/{tanqueId}
        dbrf.child(tanqueId)
                .updateChildren(tanqueMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(Editor.this, "Cambios guardados ✔️", Toast.LENGTH_SHORT).show();
                        finish(); // 🏁 Volver atrás
                    } else {
                        Toast.makeText(Editor.this, "Error al actualizar ❌", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================================================================
    // 🔙 BOTÓN “VOLVER” — Cancela sin modificar nada
    // ================================================================
    public void volverAlMenu(View view) {
        finish(); // 🚪 Salimos del editor sin guardar
    }
}
