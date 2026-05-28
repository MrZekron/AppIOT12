package com.example.appiot12.ui.perfil;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditorPerfil extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etNombrePerfil, etCorreoPerfil;
    private Button btnGuardarPerfil, btnCancelarPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_editor_perfil);

        auth = FirebaseAuth.getInstance();

        etNombrePerfil = findViewById(R.id.etNombrePerfil);
        etCorreoPerfil = findViewById(R.id.etCorreoPerfil);
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil);
        btnCancelarPerfil = findViewById(R.id.btnCancelarPerfil);

        cargarDatosUsuario();

        btnGuardarPerfil.setOnClickListener(v -> guardarCambios());
        btnCancelarPerfil.setOnClickListener(v -> finish());
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            toast("No hay sesión activa");
            finish();
            return;
        }

        etCorreoPerfil.setText(safe(user.getEmail()));

        FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid()).child("nombre")
                .get()
                .addOnSuccessListener(snapshot -> cargarNombre(snapshot, user))
                .addOnFailureListener(e -> {
                    etNombrePerfil.setText(safe(user.getDisplayName()));
                    toast("No se pudo cargar el nombre desde la base de datos");
                });
    }

    private void cargarNombre(DataSnapshot snapshot, FirebaseUser user) {
        String nombreDB = snapshot.getValue(String.class);
        if (nombreDB != null && !nombreDB.trim().isEmpty()) {
            etNombrePerfil.setText(nombreDB.trim());
        } else {
            etNombrePerfil.setText(safe(user.getDisplayName()));
        }
    }

    private void guardarCambios() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            toast("No hay sesión activa");
            return;
        }

        String nombre = etNombrePerfil.getText().toString().trim();
        if (nombre.isEmpty()) {
            etNombrePerfil.setError("Ingrese su nombre");
            etNombrePerfil.requestFocus();
            return;
        }

        bloquearBotones(false);

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("ultimaActualizacion", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("usuarios").child(user.getUid())
                .updateChildren(datos)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        bloquearBotones(true);
                        toast("No se pudo guardar el nombre");
                        return;
                    }
                    actualizarDisplayName(user, nombre);
                });
    }

    private void actualizarDisplayName(FirebaseUser user, String nombre) {
        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(nombre).build())
                .addOnCompleteListener(task -> {
                    bloquearBotones(true);
                    if (task.isSuccessful()) {
                        toast("Perfil actualizado correctamente");
                        finish();
                    } else {
                        toast("Se guardó el nombre, pero no se actualizó el perfil de autenticación");
                    }
                });
    }

    private void bloquearBotones(boolean activar) {
        btnGuardarPerfil.setEnabled(activar);
        btnCancelarPerfil.setEnabled(activar);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
