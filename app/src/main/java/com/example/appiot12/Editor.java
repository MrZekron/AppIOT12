package com.example.appiot12; // 📦 Aquí vive esta pantalla dentro del proyecto

import android.content.Intent; // 🚪 Para recibir datos de otras pantallas
import android.os.Bundle; // 🎒 Datos y estado cuando se crea la pantalla
import android.view.View; // 👆 Para manejar clics en botones
import android.widget.EditText; // 📝 Cajas donde el usuario escribe
import android.widget.Toast; // 🍞 Mensajes cortos en pantalla

import androidx.appcompat.app.AppCompatActivity; // 🏛️ Clase base para Activities

import com.google.firebase.FirebaseApp; // 🚀 Para iniciar Firebase
import com.google.firebase.auth.FirebaseAuth; // 🔐 Para saber qué usuario está logueado
import com.google.firebase.auth.FirebaseUser; // 👤 Usuario actual
import com.google.firebase.database.DatabaseReference; // 🗂 Puntero a un lugar de la base de datos
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Base de datos en la nube

import java.util.HashMap; // 🧱 Para crear mapas clave/valor
import java.util.Map; // 📋 Mapa genérico

// 🛠 Esta pantalla permite EDITAR un tanque de agua 💧
// Es como un “taller mecánico” pero para tanques 😄
public class Editor extends AppCompatActivity {

    // 📝 Cajas de texto donde el usuario va a editar los datos del tanque
    private EditText etNombre, etCapacidad, etColor;

    private String tanqueId;          // 🆔 ID del tanque que estamos editando
    private DatabaseReference dbrf;   // 🗺 Referencia a "usuarios/{uid}/tanques" en Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 🎬 Se ejecuta al abrir esta pantalla
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor); // 🎨 Dibujamos el layout en la pantalla

        // 🚀 Iniciamos Firebase (por si aún no estaba inicializado)
        FirebaseApp.initializeApp(this);

        // 🔐 Obtenemos el usuario actual de Firebase Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // 😱 Si no hay usuario logueado, no podemos editar nada
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish(); // 🚪 Cerramos esta pantalla
            return;
        }

        String uid = user.getUid(); // 🆔 ID del usuario dueño del tanque

        // 🛣 Ruta a los tanques de ESTE usuario:
        // usuarios/{uid}/tanques
        dbrf = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");

        // 🎯 Conectamos las cajas de texto con el diseño XML
        etNombre = findViewById(R.id.etNombre);       // 📝 Nombre del tanque
        etCapacidad = findViewById(R.id.etCapacidad); // 💧 Capacidad del tanque
        etColor = findViewById(R.id.etColor);         // 🎨 Color del tanque

        // 📩 Recibimos los datos que vienen desde la otra pantalla (por Intent)
        Intent intent = getIntent();
        tanqueId = intent.getStringExtra("tanqueId"); // 🆔 ID del tanque a editar

        String nombre = intent.getStringExtra("tanqueNombre");         // 🧾 Nombre actual
        String capacidad = intent.getStringExtra("tanqueCapacidad");   // 💧 Capacidad actual
        String color = intent.getStringExtra("tanqueColor");           // 🎨 Color actual

        // ✍️ Mostramos los datos actuales en las cajas de texto
        etNombre.setText(nombre != null ? nombre : "");           // Si es null, ponemos vacío
        etCapacidad.setText(capacidad != null ? capacidad : "");
        etColor.setText(color != null ? color : "");
    }

    // 💾 Método que se ejecuta cuando el usuario toca el botón "Guardar"
    public void guardarTanque(View view) {

        // 📥 Leemos lo que el usuario escribió
        String nombre = etNombre.getText().toString().trim();
        String capacidad = etCapacidad.getText().toString().trim();
        String color = etColor.getText().toString().trim();

        // 🚨 Revisamos que no haya campos vacíos
        if (nombre.isEmpty() || capacidad.isEmpty() || color.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show();
            return; // 🛑 No seguimos si falta algo
        }

        // 🧱 Creamos un mapa con los valores nuevos para actualizar
        Map<String, Object> tanqueMap = new HashMap<>();
        tanqueMap.put("nombre", nombre);       // 📝 Nuevo nombre
        tanqueMap.put("capacidad", capacidad); // 💧 Nueva capacidad
        tanqueMap.put("color", color);         // 🎨 Nuevo color

        // 🔁 Actualizamos solo esos campos en:
        // usuarios/{uid}/tanques/{tanqueId}
        dbrf.child(tanqueId)
                .updateChildren(tanqueMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 🎉 Todo salió bien
                        Toast.makeText(Editor.this, "Cambios guardados", Toast.LENGTH_SHORT).show();
                        finish(); // 🚪 Cerramos esta pantalla y volvemos atrás
                    } else {
                        // 😢 Algo falló
                        Toast.makeText(Editor.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔙 Botón para volver sin guardar cambios (solo cierra la pantalla)
    public void volverAlMenu(View view) {
        finish(); // 🚪 Cerramos el editor y volvemos a la pantalla anterior
    }
}
