package com.example.appiot12;

// =====================================================
// 📦 PANTALLA AGREGAR TANQUE
// =====================================================
// Esta pantalla permite crear un nuevo tanque de agua.
//
// ¿Qué hace?
// 1. Lee los datos escritos por el usuario
// 2. Valida que los campos obligatorios estén completos
// 3. Valida que la capacidad tenga solo números
// 4. Crea un dispositivo inicial para el tanque
// 5. Guarda dispositivo y tanque en Firebase
// 6. Registra la acción en el historial
// 7. Redirige a la lista de tanques
//
// IMPORTANTE:
// La capacidad del tanque ahora solo puede contener números.
// No acepta letras, espacios vacíos raros ni signos.
// =====================================================

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class Agregar extends AppCompatActivity {

    // =====================================================
    // ☁️ FIREBASE
    // =====================================================
    // Base de datos y referencia raíz.
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    // =====================================================
    // 📝 CAMPOS DEL FORMULARIO
    // =====================================================
    // txtNombre     -> nombre del tanque
    // txtCapasidad  -> capacidad del tanque (solo números)
    // txtColor      -> color del tanque
    // txtDireccion  -> dirección o ubicación
    private EditText txtNombre, txtCapasidad, txtColor, txtDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);

        // Conecta los EditText del XML con Java.
        txtNombre = findViewById(R.id.txtNombre);
        txtCapasidad = findViewById(R.id.txtCapasidad);
        txtColor = findViewById(R.id.txtColor);
        txtDireccion = findViewById(R.id.txtDireccion);

        // Inicializa Firebase.
        iniciarFirebase();
    }

    // =====================================================
    // 🔗 INICIALIZAR FIREBASE
    // =====================================================
    // Prepara Firebase Realtime Database para poder guardar datos.
    private void iniciarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    // =====================================================
    // 💾 ENVIAR DATOS DEL TANQUE
    // =====================================================
    // Este método:
    // 1. Lee los campos
    // 2. Valida datos
    // 3. Obtiene el usuario actual
    // 4. Crea IDs únicos
    // 5. Guarda dispositivo y tanque
    public void enviarDatosUsuario(View view) {

        // Lee y limpia espacios.
        String nombre = txtNombre.getText().toString().trim();
        String capacidad = txtCapasidad.getText().toString().trim();
        String color = txtColor.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();

        // Valida campos obligatorios.
        if (!validarCampos(nombre, capacidad, color)) return;

        // Obtiene usuario autenticado.
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();

        // Si no hay usuario logueado, no se puede guardar.
        if (usuarioActual == null) {
            toast("Error: usuario no autenticado.");
            return;
        }

        // UID del usuario actual.
        String uid = usuarioActual.getUid();

        // Crea un ID único para el tanque.
        String idTanque = UUID.randomUUID().toString();

        // Crea un ID único para el dispositivo inicial.
        String idDispositivo = UUID.randomUUID().toString();

        // =================================================
        // 📡 CREAR DISPOSITIVO INICIAL
        // =================================================
        // Valores iniciales de ejemplo para el dispositivo.
        Dispositivo dispositivo = new Dispositivo(
                idDispositivo,
                7.0,    // pH inicial
                500.0,  // conductividad inicial
                1.0,    // turbidez inicial
                150.0   // nivel inicial
        );

        // Guarda el dispositivo dentro del usuario.
        databaseReference.child("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(idDispositivo)
                .setValue(dispositivo);

        // =================================================
        // 🚰 CREAR TANQUE
        // =================================================
        // Crea el objeto tanque y le asigna datos.
        TanqueAgua tanque = new TanqueAgua();
        tanque.setIdTanque(idTanque);
        tanque.setNombre(nombre);
        tanque.setCapacidad(capacidad);
        tanque.setColor(color);
        tanque.setIdDispositivo(idDispositivo);

        // Si tu clase TanqueAgua tiene setDireccion(), descomenta esta línea:
        // tanque.setDireccion(direccion);

        // =================================================
        // ☁️ GUARDAR TANQUE EN FIREBASE
        // =================================================
        databaseReference.child("usuarios")
                .child(uid)
                .child("tanques")
                .child(idTanque)
                .setValue(tanque)
                .addOnSuccessListener(aVoid -> {

                    // Registra en historial que se creó un tanque.
                    HistorialLogger.registrarAccion(
                            "crear",
                            "Se creó el tanque: " + nombre
                    );

                    toast("Tanque creado correctamente.");

                    // Abre la lista de tanques.
                    startActivity(new Intent(Agregar.this, Lista.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        toast("Error al enviar datos: " + e.getMessage()));
    }

    // =====================================================
    // ✅ VALIDAR CAMPOS
    // =====================================================
    // Valida:
    // - nombre obligatorio
    // - capacidad obligatoria
    // - color obligatorio
    // - capacidad solo numérica
    // - capacidad mayor a cero
    private boolean validarCampos(String nombre, String capacidad, String color) {

        // Valida nombre.
        if (nombre.isEmpty()) {
            txtNombre.setError("Ingrese el nombre del tanque");
            txtNombre.requestFocus();
            return false;
        }

        // Valida capacidad.
        if (capacidad.isEmpty()) {
            txtCapasidad.setError("Ingrese la capacidad del tanque");
            txtCapasidad.requestFocus();
            return false;
        }

        // Solo se permiten números enteros.
        if (!capacidad.matches("\\d+")) {
            txtCapasidad.setError("Solo se permiten números");
            txtCapasidad.requestFocus();
            return false;
        }

        // Convierte la capacidad a número para validar que sea mayor a 0.
        int capacidadNumero = Integer.parseInt(capacidad);
        if (capacidadNumero <= 0) {
            txtCapasidad.setError("La capacidad debe ser mayor a 0");
            txtCapasidad.requestFocus();
            return false;
        }

        // Valida color.
        if (color.isEmpty()) {
            txtColor.setError("Ingrese el color del tanque");
            txtColor.requestFocus();
            return false;
        }

        return true;
    }

    // =====================================================
    // 📋 IR A LA LISTA
    // =====================================================
    // Abre la pantalla donde se listan los tanques.
    public void verLista(View v) {
        startActivity(new Intent(this, Lista.class));
    }

    // =====================================================
    // ❌ CANCELAR
    // =====================================================
    // Cancela la creación y vuelve al menú.
    public void cancelar(View view) {
        startActivity(new Intent(this, Menu.class));
        finish();
    }

    // =====================================================
    // 🍞 TOAST
    // =====================================================
    // Método auxiliar para mostrar mensajes rápidos.
    private void toast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}