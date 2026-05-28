package com.example.appiot12.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.appiot12.ui.menu.Menu;
import com.example.appiot12.ui.menu.MenuAdmin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;

public class IniciarSesion extends AppCompatActivity {

    private EditText etCorreo, etContrasena;
    private Button btnIngresar;
    private FirebaseAuth auth;
    private DatabaseReference usuariosRef;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.appiot12.R.layout.cliente_iniciar_sesion);

        etCorreo = findViewById(com.example.appiot12.R.id.tvCorreo);
        etContrasena = findViewById(com.example.appiot12.R.id.tvContrasena);
        btnIngresar = findViewById(com.example.appiot12.R.id.btnIngresar);

        auth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        btnIngresar.setOnClickListener(v -> login());
    }

    private void login() {
        String correo = etCorreo.getText().toString().trim();
        String pass = etContrasena.getText().toString().trim();

        if (!validarCampos(correo, pass)) return;

        bloquearUI(true);

        auth.signInWithEmailAndPassword(correo, pass).addOnCompleteListener(task -> {
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

    private void validarUsuarioDB(String uid, String correoIngresado) {
        usuariosRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
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

                if (correoDB == null || !correoDB.equalsIgnoreCase(correoIngresado)) {
                    toast("Correo no coincide");
                    return;
                }

                if (bloqueado != null && bloqueado) {
                    auth.signOut();
                    toast("Cuenta bloqueada");
                    return;
                }

                FirebaseMessaging.getInstance().getToken()
                        .addOnSuccessListener(token -> {
                            if (token != null)
                                FirebaseDatabase.getInstance()
                                        .getReference("usuarios").child(uid).child("fcmToken")
                                        .setValue(token);
                        });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                            IniciarSesion.this,
                            new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
                }

                irMenu(rol, correoDB);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                error("Error al leer datos");
            }
        });
    }

    private void irMenu(String rol, String correo) {
        Intent i = "admin".equalsIgnoreCase(rol)
                ? new Intent(this, MenuAdmin.class)
                : new Intent(this, Menu.class);
        i.putExtra("usuarioCorreo", correo);
        startActivity(i);
        finish();
    }

    private void error(String msg) {
        bloquearUI(false);
        toast(msg);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void bloquearUI(boolean bloquear) {
        btnIngresar.setEnabled(!bloquear);
        if (bloquear) {
            progress.setMessage("Ingresando...");
            progress.show();
        } else {
            progress.dismiss();
        }
    }

    public void goToCrearCuenta(View v) {
        startActivity(new Intent(this, CrearCuentaActivity.class));
    }

    public void goToOlvideContrasena(View v) {
        startActivity(new Intent(this, OlvideContrasenaActivity.class));
    }
}
