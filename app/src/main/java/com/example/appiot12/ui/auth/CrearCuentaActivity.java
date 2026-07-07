package com.example.appiot12.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appiot12.ui.BaseActivity;

import com.example.appiot12.ui.menu.Menu;
import com.example.appiot12.ui.menu.MenuAdmin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CrearCuentaActivity extends BaseActivity {

    private EditText etNombre, etEmail, etPass, etPassConfirm;
    private Button btnCrear;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.appiot12.R.layout.cliente_crear_cuenta);

        etNombre = findViewById(com.example.appiot12.R.id.etNombre);
        etEmail = findViewById(com.example.appiot12.R.id.etEmailCrear);
        etPass = findViewById(com.example.appiot12.R.id.etPassCrear);
        etPassConfirm = findViewById(com.example.appiot12.R.id.etPassConfirm);
        btnCrear = findViewById(com.example.appiot12.R.id.btnCrearCuenta);

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creando cuenta...");
        progressDialog.setCancelable(false);

        btnCrear.setOnClickListener(this::crearCuenta);
    }

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

            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(nombre).build());
            user.sendEmailVerification();

            boolean esAdmin = email.endsWith("@aguasegura.cl");
            guardarUsuario(user.getUid(), nombre, email, esAdmin ? "admin" : "usuario", esAdmin);
        });
    }

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

    private boolean passwordValida(String pass) {
        return pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!¡?*._-]).{8,20}$");
    }

    private void guardarUsuario(String uid, String nombre, String email, String rol, boolean esAdmin) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("id", uid);
        usuario.put("nombre", nombre);
        usuario.put("correo", email);
        usuario.put("rol", rol);
        usuario.put("activo", true);
        usuario.put("tanques", new HashMap<>());
        usuario.put("createdAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
                .setValue(usuario)
                .addOnCompleteListener(task -> {
                    bloquearUI(false);
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Cuenta creada, pero hubo un error al guardar los datos", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, esAdmin ? MenuAdmin.class : Menu.class));
                    finish();
                });
    }

    private void bloquearUI(boolean bloquear) {
        btnCrear.setEnabled(!bloquear);
        if (bloquear) progressDialog.show();
        else progressDialog.dismiss();
    }

    public void cancelCreateAccount(View view) {
        finish();
    }
}
