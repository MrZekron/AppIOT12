package com.example.appiot12;
// 📦 Pantalla “Olvidé mi contraseña”
// Aquí ayudamos al usuario a recuperar el acceso a su cuenta 🔐✉️

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

/**
 * ⭐ OLVIDÉ MI CONTRASEÑA ⭐
 *
 * Explicado para un niño 👶:
 * 👉 Escribes tu correo
 * 👉 La app le pide a Firebase que te mande un email ✉️
 * 👉 Si el correo existe → llega el mensaje
 * 👉 Si no existe → Firebase avisa
 *
 * REGLA DE ORO:
 * 👉 NO revisamos la base de datos
 * 👉 Firebase Auth ya sabe todo lo necesario 🧠
 */
public class OlvideContrasenaActivity extends AppCompatActivity {

    // ✉️ Campo donde el usuario escribe su correo
    private EditText etEmail;

    // 🔘 Botones
    private Button btnEnviar, btnCancelar;

    // 🔐 Firebase Authentication
    private FirebaseAuth auth;

    // ⏳ Ventana de progreso
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 📱 Activar pantalla completa moderna
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_olvide_contrasena);

        // 📐 Ajustar márgenes para no chocar con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // 🔗 Vincular UI
        etEmail = findViewById(R.id.etEmailOlvide);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnCancelar = findViewById(R.id.btnCancelarOlvide);

        // 🔐 Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();

        // ⏳ Configurar diálogo de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 🎯 Acciones de botones
        btnEnviar.setOnClickListener(this::enviarCorreoReset);
        btnCancelar.setOnClickListener(v -> finish());
    }

    // ============================================================
    // ✉️ ENVIAR CORREO DE RECUPERACIÓN
    // ============================================================
    private void enviarCorreoReset(View view) {

        String email = etEmail.getText().toString().trim();

        // 🚨 Validaciones básicas
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese su correo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⏳ Mostrar progreso
        progressDialog.setMessage("Enviando enlace de recuperación... ✉️");
        progressDialog.show();
        btnEnviar.setEnabled(false);

        // 🔐 Firebase se encarga de todo
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    progressDialog.dismiss();
                    btnEnviar.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Revisa tu correo para restablecer la contraseña 📬",
                                Toast.LENGTH_LONG
                        ).show();
                        finish();
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Error desconocido";

                        Toast.makeText(
                                this,
                                "No se pudo enviar el correo ❌\n" + msg,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
