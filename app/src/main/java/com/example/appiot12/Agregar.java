package com.example.appiot12;
// 📦 Pantalla para agregar un tanque nuevo 💧📦
// Dirección opcional, pero si existe debe ser REAL 🌍✅

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class Agregar extends AppCompatActivity {

    // ☁️ Firebase
    private DatabaseReference database;

    // 📝 Inputs
    private EditText txtNombre;
    private EditText txtCapacidad;
    private EditText txtColor;
    private EditText txtDireccion; // 📍 OPCIONAL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);

        inicializarVistas();
        inicializarFirebase();
    }

    // =====================================================
    // 🔗 VINCULAR XML → JAVA
    // =====================================================
    private void inicializarVistas() {
        txtNombre = findViewById(R.id.txtNombre);
        txtCapacidad = findViewById(R.id.txtCapasidad);
        txtColor = findViewById(R.id.txtColor);
        txtDireccion = findViewById(R.id.txtDireccion);
    }

    // =====================================================
    // ☁️ FIREBASE
    // =====================================================
    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance().getReference();
    }

    // =====================================================
    // 💾 BOTÓN GUARDAR
    // =====================================================
    public void enviarDatosUsuario(View view) {

        String nombre = txtNombre.getText().toString().trim();
        String capacidadStr = txtCapacidad.getText().toString().trim();
        String color = txtColor.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();

        // 🛑 Validaciones base
        if (nombre.isEmpty() || capacidadStr.isEmpty() || color.isEmpty()) {
            toast("Completa los campos obligatorios");
            return;
        }

        // 🔢 Capacidad numérica
        int capacidadLitros;
        try {
            capacidadLitros = Integer.parseInt(capacidadStr);
            if (capacidadLitros <= 0) {
                toast("La capacidad debe ser mayor a 0 litros 💧");
                return;
            }
        } catch (NumberFormatException e) {
            toast("La capacidad debe ser un número (ej: 500)");
            return;
        }

        // 👤 Usuario
        String uid = obtenerUidUsuario();
        if (uid == null) {
            toast("Usuario no autenticado");
            return;
        }

        // 📍 Validar dirección SI fue ingresada
        if (!direccion.isEmpty()) {

            new Thread(() -> {

                double[] coords =
                        GeocodingService.obtenerCoordenadas(this, direccion);

                runOnUiThread(() -> {
                    if (coords == null) {
                        toast("La dirección no existe ❌\nIngresa una dirección real");
                    } else {
                        guardarTanque(uid, nombre, capacidadLitros, color, direccion);
                    }
                });

            }).start();

        } else {
            // ✔ Dirección opcional
            guardarTanque(uid, nombre, capacidadLitros, color, null);
        }
    }

    // =====================================================
    // 💾 GUARDAR TANQUE (SIN DISPOSITIVO)
    // =====================================================
    private void guardarTanque(String uid,
                               String nombre,
                               int capacidadLitros,
                               String color,
                               String direccion) {

        String idTanque = UUID.randomUUID().toString();

        // 💧 Tanque SIN dispositivo
        TanqueAgua tanque = new TanqueAgua();
        tanque.setIdTanque(idTanque);
        tanque.setNombre(nombre);
        tanque.setCapacidad(String.valueOf(capacidadLitros));
        tanque.setColor(color);
        tanque.setDireccion(direccion);
        tanque.setIdDispositivo(null); // 🚫 SE ASIGNA DESPUÉS

        // ☁️ Firebase
        database.child("usuarios")
                .child(uid)
                .child("tanques")
                .child(idTanque)
                .setValue(tanque)
                .addOnSuccessListener(aVoid -> {

                    HistorialLogger.registrarAccion(
                            "crear",
                            "Se creó el tanque: " + nombre
                    );

                    toast("Tanque creado correctamente 💧");
                    startActivity(new Intent(this, Lista.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        toast("Error al guardar: " + e.getMessage())
                );
    }

    // =====================================================
    // 👤 UID
    // =====================================================
    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // 🍞 Toast simple
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ❌ Cancelar
    public void cancelar(View view) {
        startActivity(new Intent(this, Menu.class));
        finish();
    }
}
