package com.example.appiot12;
// Paquete raíz donde habita esta Activity. Mantiene la gobernanza del proyecto 🏢⚙️

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
// Importamos las herramientas esenciales para UI, navegación y notificaciones al usuario 🛠️📱

import androidx.appcompat.app.AppCompatActivity;
// Activity base moderna con soporte AppCompat para mantener estándares enterprise 🎖️

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// Módulos Firebase: autenticación y base de datos. El core de nuestro backend ☁️🔥

import java.util.UUID;
// UUID = IDs únicos estilo "nivel ejecutivo" 🧬

public class Agregar extends AppCompatActivity {
    // Activity donde el usuario registra un nuevo tanque.
    // La “mesa de operaciones” para aumentar el inventario de activos acuáticos 💧📦

    private FirebaseDatabase fdbd;       // Instancia de la base de datos Firebase (edificio completo) 🏢
    private DatabaseReference dbrf;      // Referencia de escritura/lectura (puerta de acceso principal) 🔑

    private EditText txtNombre, txtCapasidad, txtColor, txtDireccion;
    // Campos donde el usuario ingresará los datos del tanque. El formulario oficial 📄

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                // Inicializa contexto Android ⚙️
        setContentView(R.layout.activity_agregar);         // Pintamos el layout en pantalla 🎨

        // Vinculamos cada EditText con su elemento en el XML (formulario de registro)
        txtNombre = findViewById(R.id.txtNombre);
        txtCapasidad = findViewById(R.id.txtCapasidad);
        txtColor = findViewById(R.id.txtColor);
        txtDireccion = findViewById(R.id.txtDireccion);

        iniciarFirebase(); // Configuramos la base de datos para operar 🏗️🔥
    }

    private void iniciarFirebase() {
        FirebaseApp.initializeApp(this);      // Inicializa Firebase en el contexto actual ⚡
        fdbd = FirebaseDatabase.getInstance(); // Conecta con la instancia global de la BD 🌐
        dbrf = fdbd.getReference();           // Obtenemos referencia raíz (nivel gerencial) 🗂️
    }

    public void enviarDatosUsuario(View view) {
        // Función que se ejecuta al presionar "Guardar" 🆕💾
        // Aquí ocurre la magia: validación → creación → escritura → historial.

        String nombre = txtNombre.getText().toString().trim();      // Nombre del tanque ✏️
        String color = txtColor.getText().toString().trim();        // Color permitido 🎨
        String capacidad = txtCapasidad.getText().toString().trim();// Capacidad declarada 📦
        String direccion = txtDireccion.getText().toString().trim();// Dirección física (si aplica) 🗺️

        // === VALIDACIÓN DE CAMPOS OBLIGATORIOS ===
        if (nombre.isEmpty() || color.isEmpty() || capacidad.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            // Comunicación corporativa para incentivar cumplimiento de requisitos 📢
            return;
        }

        // Obtenemos el UID del usuario autenticado
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado.", Toast.LENGTH_SHORT).show();
            // Si no existe UID, nadie sabe quién está creando el tanque → riesgo auditivo 🔒😅
            return;
        }

        // === 1️⃣ Crear ID único para el tanque (activo nuevo en el inventario) ===
        String idTanque = UUID.randomUUID().toString();

        // === 2️⃣ Crear un dispositivo asociado con valores iniciales (Dummy IoT) ===
        String idDispositivo = UUID.randomUUID().toString();
        // Creamos un objeto Dispositivo con valores iniciales aceptables
        Dispositivo d1 = new Dispositivo(idDispositivo, 7.0, 500.0, 1.0, 150.0);
        // Esto simula un dispositivo IoT inicial antes de vincular un ESP32 real 🤖📡

        // === 3️⃣ Registrar dispositivo en Firebase bajo el usuario ===
        dbrf.child("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(idDispositivo)
                .setValue(d1);
        // Guardamos el dispositivo como parte del inventario del usuario 💾🔥

        // === 4️⃣ Crear el objeto TanqueAgua dinámicamente ===
        TanqueAgua tanque = new TanqueAgua();
        tanque.setIdTanque(idTanque);            // ID único
        tanque.setNombre(nombre);                // Nombre ingresado
        tanque.setCapacidad(capacidad);          // Capacidad declarada
        tanque.setColor(color);                  // Color corporativo 😄
        tanque.setIdDispositivo(idDispositivo);  // Asociamos el dispositivo creado

        // === 5️⃣ Guardar tanque en Firebase dentro del usuario ===
        dbrf.child("usuarios")
                .child(uid)
                .child("tanques")
                .child(idTanque)
                .setValue(tanque)
                .addOnSuccessListener(aVoid -> {
                    // Operación exitosa → Se puede proceder con alta gerencia 😎📈

                    // === 6️⃣ Registrar acción en historial ===
                    HistorialLogger.registrarAccion(
                            "crear",
                            "Se creó el tanque: " + nombre
                    );
                    // Trazabilidad asegurada para auditoría interna 📘✨

                    Toast.makeText(Agregar.this, "Tanque creado correctamente.", Toast.LENGTH_SHORT).show();
                    // Feedback positivo al usuario 👍

                    startActivity(new Intent(Agregar.this, Lista.class));
                    finish(); // Cerramos esta Activity para evitar duplicación 🔁
                })
                .addOnFailureListener(e ->
                        Toast.makeText(Agregar.this, "Error al enviar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        // Manejo de fallos tipo corporativo con detalle incluido ⚠️
    }

    public void verLista(View v) {
        // Botón para visualizar lista de tanques 📋
        startActivity(new Intent(this, Lista.class));
    }

    public void cancelar(View view) {
        // Acción tipo “Abortar misión” ❌
        // Vuelve al menú principal sin guardar nada
        startActivity(new Intent(this, Menu.class));
        finish();
    }
}
