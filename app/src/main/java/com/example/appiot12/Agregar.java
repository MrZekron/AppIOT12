package com.example.appiot12;
// 📦 Paquete raíz del proyecto Agua Segura.
// Aquí viven las Activities que controlan pantallas y acciones del usuario 🏢📱

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
// 🛠️ Herramientas básicas para interacción con el usuario

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
// 🎖️ Activity moderna compatible con versiones antiguas de Android

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// ☁️ Firebase: autenticación + base de datos en tiempo real

import java.util.UUID;
// 🔑 Generador de IDs únicos (sin duplicados, sin dolores)

/**
 * ➕ Agregar
 *
 * Esta pantalla permite:
 * 👉 Crear un nuevo tanque de agua
 * 👉 Asociarle un dispositivo
 * 👉 Guardar todo en Firebase
 * 👉 Registrar la acción en el historial
 *
 * En simple:
 * Es el formulario para agregar un tanque nuevo 💧📦
 */
public class Agregar extends AppCompatActivity {

    // ☁️ Firebase Database (una sola instancia, sin redundancia)
    private DatabaseReference database;

    // 📝 Campos del formulario
    private EditText txtNombre;
    private EditText txtCapacidad;
    private EditText txtColor;
    private EditText txtDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos los EditText con el XML
        inicializarVistas();

        // 🔥 Inicializamos Firebase
        inicializarFirebase();
    }

    /**
     * 🔗 Conecta los campos del formulario con el XML
     */
    private void inicializarVistas() {
        txtNombre = findViewById(R.id.txtNombre);
        txtCapacidad = findViewById(R.id.txtCapasidad); // ⚠️ Se mantiene ID original del XML
        txtColor = findViewById(R.id.txtColor);
        txtDireccion = findViewById(R.id.txtDireccion);
    }

    /**
     * ☁️ Inicializa Firebase una sola vez
     */
    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);          // ⚡ Arrancamos Firebase
        database = FirebaseDatabase.getInstance()
                .getReference();                  // 📂 Referencia raíz
    }

    /**
     * 💾 Se ejecuta cuando el usuario presiona el botón "Guardar"
     */
    public void enviarDatosUsuario(View view) {

        // ✏️ Leemos los datos escritos por el usuario
        String nombre = txtNombre.getText().toString().trim();
        String capacidad = txtCapacidad.getText().toString().trim();
        String color = txtColor.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim(); // (opcional por ahora)

        // 🛑 Validamos que los campos importantes no estén vacíos
        if (!camposValidos(nombre, capacidad, color)) {
            Toast.makeText(this,
                    "Completa todos los campos obligatorios.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 👤 Obtenemos el usuario actual
        String uid = obtenerUidUsuario();

        if (uid == null) {
            Toast.makeText(this,
                    "Error: usuario no autenticado.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 🆔 Generamos IDs únicos
        String idTanque = UUID.randomUUID().toString();
        String idDispositivo = UUID.randomUUID().toString();

        // 🤖 Creamos un dispositivo con valores iniciales
        Dispositivo dispositivo = crearDispositivoInicial(idDispositivo);

        // 💧 Creamos el tanque y lo asociamos al dispositivo
        TanqueAgua tanque = crearTanque(
                idTanque,
                nombre,
                capacidad,
                color,
                idDispositivo
        );

        // ☁️ Guardamos todo en Firebase
        guardarEnFirebase(uid, dispositivo, tanque, nombre);
    }

    /**
     * ✅ Revisa que los campos obligatorios estén completos
     */
    private boolean camposValidos(String nombre, String capacidad, String color) {
        return !nombre.isEmpty() && !capacidad.isEmpty() && !color.isEmpty();
    }

    /**
     * 👤 Obtiene el UID del usuario logueado
     */
    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * 🤖 Crea un dispositivo con valores iniciales simulados
     */
    private Dispositivo crearDispositivoInicial(@NonNull String idDispositivo) {

        // Valores iniciales seguros:
        // pH = 7 (neutral)
        // turbidez = 500
        // conductividad = 1
        // nivel = 150 cm
        return new Dispositivo(
                idDispositivo,
                7.0,
                500.0,
                1.0,
                150.0
        );
    }

    /**
     * 💧 Crea el objeto TanqueAgua
     */
    private TanqueAgua crearTanque(
            String idTanque,
            String nombre,
            String capacidad,
            String color,
            String idDispositivo
    ) {

        TanqueAgua tanque = new TanqueAgua();

        tanque.setIdTanque(idTanque);           // 🆔 ID único
        tanque.setNombre(nombre);               // 📛 Nombre
        tanque.setCapacidad(capacidad);         // 📦 Capacidad
        tanque.setColor(color);                 // 🎨 Color
        tanque.setIdDispositivo(idDispositivo); // 🔗 Asociación

        return tanque;
    }

    /**
     * ☁️ Guarda el dispositivo y el tanque en Firebase
     */
    private void guardarEnFirebase(
            String uid,
            Dispositivo dispositivo,
            TanqueAgua tanque,
            String nombreTanque
    ) {

        // 💾 Guardamos el dispositivo
        database.child("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(dispositivo.getIdDispositivo())
                .setValue(dispositivo);

        // 💾 Guardamos el tanque
        database.child("usuarios")
                .child(uid)
                .child("tanques")
                .child(tanque.getIdTanque())
                .setValue(tanque)
                .addOnSuccessListener(aVoid -> {

                    // 🧾 Registramos la acción en el historial
                    HistorialLogger.registrarAccion(
                            "crear",
                            "Se creó el tanque: " + nombreTanque
                    );

                    // 👍 Avisamos al usuario
                    Toast.makeText(this,
                            "Tanque creado correctamente.",
                            Toast.LENGTH_SHORT).show();

                    // 📋 Vamos a la lista de tanques
                    startActivity(new Intent(this, Lista.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error al guardar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * 📋 Botón para ver la lista de tanques
     */
    public void verLista(View view) {
        startActivity(new Intent(this, Lista.class));
    }

    /**
     * ❌ Cancela la operación y vuelve al menú
     */
    public void cancelar(View view) {
        startActivity(new Intent(this, Menu.class));
        finish();
    }
}
