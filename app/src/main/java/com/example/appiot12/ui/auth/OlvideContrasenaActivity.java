package com.example.appiot12.ui.auth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class OlvideContrasenaActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnEnviar, btnCancelar;
    private FirebaseAuth auth;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.appiot12.R.layout.activity_olvide_contrasena);

        etEmail = findViewById(com.example.appiot12.R.id.etEmailOlvide);
        btnEnviar = findViewById(com.example.appiot12.R.id.btnEnviar);
        btnCancelar = findViewById(com.example.appiot12.R.id.btnCancelarOlvide);

        auth = FirebaseAuth.getInstance();

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("Enviando enlace de recuperación...");

        btnEnviar.setOnClickListener(this::enviarCorreoReset);
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void enviarCorreoReset(View view) {
        String email = etEmail.getText().toString().trim();
        if (!validarEmail(email)) return;

        bloquearUI(true);

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            bloquearUI(false);
            if (task.isSuccessful()) {
                toast("Revisa tu correo para restablecer la contraseña");
                finish();
            } else {
                String msg = task.getException() != null
                        ? task.getException().getMessage()
                        : "No se pudo enviar el correo";
                toast(msg);
            }
        });
    }

    private boolean validarEmail(String email) {
        if (email.isEmpty()) {
            etEmail.setError("Ingrese su correo");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo válido");
            etEmail.requestFocus();
            return false;
        }
        return true;
    }

    private void bloquearUI(boolean bloquear) {
        btnEnviar.setEnabled(!bloquear);
        btnCancelar.setEnabled(!bloquear);
        if (bloquear) progress.show();
        else progress.dismiss();
    }

    private void toast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}
