package com.example.appiot12;

// Pantalla de inicio de sesión de Agua Segura.

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class IniciarSesion extends AppCompatActivity {

    // Campos UI
    private EditText etCorreo, etContrasena;
    private Button btnIngresar;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;

    // Carga
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);

        // Vincular vistas
        etCorreo = findViewById(R.id.tvCorreo);
        etContrasena = findViewById(R.id.tvContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);

        // Firebase
        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // Progress
        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        // Evento login
        btnIngresar.setOnClickListener(v -> login());
    }

    // =========================
    // 🔐 LOGIN PRINCIPAL
    // =========================
    private void login() {

        String correo = etCorreo.getText().toString().trim();
        String pass = etContrasena.getText().toString().trim();

        // Validaciones mínimas (NO regex compleja aquí)
        if (!validarCampos(correo, pass)) return;

        bloquearUI(true);

        auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        error("Correo o contraseña incorrectos");
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();

                    if (user == null) {
                        error("Error inesperado");
                        return;
                    }

                    validarUsuarioDB(user.getUid(), correo);
                });
    }

    // =========================
    // 🧹 VALIDACIÓN CAMPOS
    // =========================
    private boolean validarCampos(String correo, String pass) {

        if (correo.isEmpty()) {
            etCorreo.setError("Ingrese su correo");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo inválido");
            return false;
        }

        if (pass.isEmpty()) {
            etContrasena.setError("Ingrese su contraseña");
            return false;
        }

        return true;
    }

    // =========================
    // 📂 VALIDAR EN BASE DE DATOS
    // =========================
    private void validarUsuarioDB(String uid, String correoIngresado) {

        usuariosRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot s) {

                        bloquearUI(false);

                        if (!s.exists()) {
                            toast("Perfil no encontrado");
                            return;
                        }

                        String correoDB = s.child("correo").getValue(String.class);
                        String rol = s.child("rol").getValue(String.class);
                        Boolean bloqueado = s.child("bloqueado").getValue(Boolean.class);

                        // Validar correo
                        if (correoDB == null || !correoDB.equalsIgnoreCase(correoIngresado)) {
                            toast("Correo no coincide");
                            return;
                        }

                        // Usuario bloqueado
                        if (bloqueado != null && bloqueado) {
                            auth.signOut();
                            toast("Cuenta bloqueada");
                            return;
                        }

                        // Redirección
                        irMenu(rol, correoDB);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        error("Error al leer datos");
                    }
                });
    }

    // =========================
    // 🚦 REDIRECCIÓN
    // =========================
    private void irMenu(String rol, String correo) {

        Intent i = "admin".equalsIgnoreCase(rol)
                ? new Intent(this, MenuAdmin.class)
                : new Intent(this, Menu.class);

        i.putExtra("usuarioCorreo", correo);

        startActivity(i);
        finish();
    }

    // =========================
    // 🔧 UTILIDADES
    // =========================

    // Mostrar error y desbloquear UI
    private void error(String msg) {
        bloquearUI(false);
        toast(msg);
    }

    // Toast corto
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // Bloquear interfaz
    private void bloquearUI(boolean bloquear) {
        btnIngresar.setEnabled(!bloquear);
        if (bloquear) {
            progress.setMessage("Ingresando...");
            progress.show();
        } else {
            progress.dismiss();
        }
    }

    // =========================
    // 🔗 NAVEGACIÓN
    // =========================
    public void goToCrearCuenta(View v) {
        startActivity(new Intent(this, CrearCuentaActivity.class));
    }

    public void goToOlvideContrasena(View v) {
        startActivity(new Intent(this, OlvideContrasenaActivity.class));
    }
}