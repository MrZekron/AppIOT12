package com.example.appiot12;

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

    private FirebaseDatabase fdbd;
    private DatabaseReference dbrf;

    private EditText txtNombre, txtCapasidad, txtColor, txtDireccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);

        txtNombre = findViewById(R.id.txtNombre);
        txtCapasidad = findViewById(R.id.txtCapasidad);
        txtColor = findViewById(R.id.txtColor);
        txtDireccion = findViewById(R.id.txtDireccion);

        iniciarFirebase();
    }

    private void iniciarFirebase() {
        FirebaseApp.initializeApp(this);
        fdbd = FirebaseDatabase.getInstance();
        dbrf = fdbd.getReference();
    }

    public void enviarDatosUsuario(View view) {

        String nombre = txtNombre.getText().toString().trim();
        String color = txtColor.getText().toString().trim();
        String capacidad = txtCapasidad.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();

        if (nombre.isEmpty() || color.isEmpty() || capacidad.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // === 1️⃣ Crear ID único para tanque
        String idTanque = UUID.randomUUID().toString();

        // === 2️⃣ Crear un dispositivo inicial
        String idDispositivo = UUID.randomUUID().toString();

        Dispositivo d1 = new Dispositivo(idDispositivo, 7.0, 500.0, 1.0, 150.0);

        // === 3️⃣ Guardar dispositivo en Firebase
        dbrf.child("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(idDispositivo)
                .setValue(d1);

        // === 4️⃣ Crear objeto TANQUE
        TanqueAgua tanque = new TanqueAgua();
        tanque.setIdTanque(idTanque);
        tanque.setNombre(nombre);
        tanque.setCapacidad(capacidad);
        tanque.setColor(color);
        tanque.setIdDispositivo(idDispositivo);

        // === 5️⃣ Guardar tanque en Firebase
        dbrf.child("usuarios")
                .child(uid)
                .child("tanques")
                .child(idTanque)
                .setValue(tanque)
                .addOnSuccessListener(aVoid -> {

                    // === 6️⃣ Registrar acción en HISTORIAL
                    HistorialLogger.registrarAccion(
                            "crear",
                            "Se creó el tanque: " + nombre
                    );

                    Toast.makeText(Agregar.this, "Tanque creado correctamente.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Agregar.this, Lista.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(Agregar.this, "Error al enviar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    public void verLista(View v) {
        startActivity(new Intent(this, Lista.class));
    }

    public void cancelar(View view) {
        startActivity(new Intent(this, Menu.class));
        finish();
    }
}
