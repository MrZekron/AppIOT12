package com.example.appiot12;

// Pantalla para crear una cuenta nueva en Agua Segura.

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CrearCuentaActivity extends AppCompatActivity {

    // Campos del formulario.
    private EditText etNombre, etEmail, etPass, etPassConfirm;

    // Botón para registrar.
    private Button btnCrear;

    // Firebase Auth para crear usuarios.
    private FirebaseAuth auth;

    // Ventana de carga.
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        // Conecta variables con el XML.
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmailCrear);
        etPass = findViewById(R.id.etPassCrear);
        etPassConfirm = findViewById(R.id.etPassConfirm);
        btnCrear = findViewById(R.id.btnCrearCuenta);

        // Inicializa Firebase.
        auth = FirebaseAuth.getInstance();

        // Configura diálogo de carga.
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando cuenta...");
        progressDialog.setCancelable(false);

        // Evento del botón crear cuenta.
        btnCrear.setOnClickListener(this::crearCuenta);
    }

    // Lee datos, valida y crea la cuenta.
    private void crearCuenta(View view) {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String pass = etPass.getText().toString().trim();
        String confirmPass = etPassConfirm.getText().toString().trim();

        if (!validarCampos(nombre, email, pass, confirmPass)) return;

        bloquearUI(true);

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                bloquearUI(false);

                String mensaje = "No se pudo crear la cuenta";
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    mensaje = "Ese correo ya está registrado";
                } else if (task.getException() != null) {
                    mensaje = task.getException().getMessage();
                }

                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseUser user = auth.getCurrentUser();
            if (user == null) {
                bloquearUI(false);
                Toast.makeText(this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guarda el nombre en el perfil de Firebase.
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre)
                    .build();
            user.updateProfile(profile);

            // Envía verificación al correo.
            user.sendEmailVerification();

            // Define rol según el dominio del correo.
            boolean esAdmin = email.endsWith("@aguasegura.cl");
            String rol = esAdmin ? "admin" : "usuario";

            guardarUsuario(user.getUid(), nombre, email, rol, esAdmin);
        });
    }

    // Valida todos los campos del formulario.
    private boolean validarCampos(String nombre, String email, String pass, String confirmPass) {

        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa tu nombre");
            etNombre.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Ingresa tu correo");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            etEmail.requestFocus();
            return false;
        }

        if (pass.isEmpty()) {
            etPass.setError("Ingresa una contraseña");
            etPass.requestFocus();
            return false;
        }

        if (!passwordValida(pass)) {
            etPass.setError("Debe tener 8-20 caracteres, mayúscula, minúscula, número y carácter especial");
            etPass.requestFocus();
            return false;
        }

        if (confirmPass.isEmpty()) {
            etPassConfirm.setError("Confirma la contraseña");
            etPassConfirm.requestFocus();
            return false;
        }

        if (!pass.equals(confirmPass)) {
            etPassConfirm.setError("Las contraseñas no coinciden");
            etPassConfirm.requestFocus();
            return false;
        }

        return true;
    }

    // Regla de contraseña de Agua Segura.
    private boolean passwordValida(String pass) {
        return pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!¡?*._-]).{8,20}$");
    }

    // Guarda usuario en Realtime Database.
    private void guardarUsuario(String uid, String nombre, String email, String rol, boolean esAdmin) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("id", uid);
        usuario.put("nombre", nombre);
        usuario.put("correo", email);
        usuario.put("rol", rol);
        usuario.put("tanques", new HashMap<>());
        usuario.put("createdAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .setValue(usuario)
                .addOnCompleteListener(task -> {
                    bloquearUI(false);

                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Cuenta creada, pero hubo un error al guardar los datos", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_LONG).show();

                    // Redirige según rol.
                    Intent intent = esAdmin
                            ? new Intent(this, MenuAdmin.class)
                            : new Intent(this, Menu.class);

                    startActivity(intent);
                    finish();
                });
    }

    // Bloquea o desbloquea botón y diálogo de carga.
    private void bloquearUI(boolean bloquear) {
        btnCrear.setEnabled(!bloquear);
        if (bloquear) progressDialog.show();
        else progressDialog.dismiss();
    }

    // Cierra la pantalla al cancelar.
    public void cancelCreateAccount(View view) {
        finish();
    }
}