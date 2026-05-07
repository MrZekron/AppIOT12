package com.example.appiot12.ui.auth;

// Pantalla para enviar el correo de recuperación de contraseña.

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

    // Campo de correo y botones.
    private EditText etEmail;
    private Button btnEnviar, btnCancelar;

    // Firebase Auth.
    private FirebaseAuth auth;

    // Diálogo de carga.
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.appiot12.R.layout.activity_olvide_contrasena);

        // Vincula vistas del XML.
        etEmail = findViewById(com.example.appiot12.R.id.etEmailOlvide);
        btnEnviar = findViewById(com.example.appiot12.R.id.btnEnviar);
        btnCancelar = findViewById(com.example.appiot12.R.id.btnCancelarOlvide);

        // Inicializa Firebase.
        auth = FirebaseAuth.getInstance();

        // Configura diálogo de carga.
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage("Enviando enlace de recuperación...");

        // Eventos.
        btnEnviar.setOnClickListener(this::enviarCorreoReset);
        btnCancelar.setOnClickListener(v -> finish());
    }

    // Valida correo y envía email de recuperación.
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

    // Valida que el correo no esté vacío y tenga formato correcto.
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

    // Bloquea o desbloquea la interfaz mientras se procesa.
    private void bloquearUI(boolean bloquear) {
        btnEnviar.setEnabled(!bloquear);
        btnCancelar.setEnabled(!bloquear);

        if (bloquear) progress.show();
        else progress.dismiss();
    }

    // Muestra un mensaje al usuario.
    private void toast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}
