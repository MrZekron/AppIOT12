package com.example.appiot12.ui.tanque;
// 📦 Pantalla Editor del proyecto Agua Segura.
// Aquí se editan los datos de un tanque existente 💧✏️

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * 🛠️ Editor
 *
 * ¿Qué hace esta pantalla?
 * 👉 Permite editar un tanque existente
 * 👉 Cambiar nombre, capacidad y color
 * 👉 Guardar SOLO los cambios en Firebase
 */
public class Editor extends AppCompatActivity {

    private EditText etNombre;
    private EditText etCapacidad;
    private EditText etColor;

    private String idTanque;

    private DatabaseReference refTanques;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        FirebaseApp.initializeApp(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();

        refTanques = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");

        inicializarVistas();
        cargarDatosIntent();
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etCapacidad = findViewById(R.id.etCapacidad);
        etColor = findViewById(R.id.etColor);
    }

    private void cargarDatosIntent() {

        Intent intent = getIntent();

        idTanque = intent.getStringExtra("tanqueId");
        String nombre = intent.getStringExtra("tanqueNombre");
        String capacidad = intent.getStringExtra("tanqueCapacidad");
        String color = intent.getStringExtra("tanqueColor");

        etNombre.setText(nombre != null ? nombre : "");
        etCapacidad.setText(capacidad != null ? capacidad : "");
        etColor.setText(color != null ? color : "");
    }

    public void guardarTanque(View view) {

        String nombre = etNombre.getText().toString().trim();
        String capacidad = etCapacidad.getText().toString().trim();
        String color = etColor.getText().toString().trim();

        if (nombre.isEmpty() || capacidad.isEmpty() || color.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos 📝", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idTanque == null) {
            Toast.makeText(this, "Error: tanque no identificado ❌", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("nombre", nombre);
        cambios.put("capacidad", capacidad);
        cambios.put("color", color);

        refTanques.child(idTanque)
                .updateChildren(cambios)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cambios guardados ✔️", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar ❌", Toast.LENGTH_SHORT).show()
                );
    }

    public void volverAlMenu(View view) {
        finish();
    }
}
