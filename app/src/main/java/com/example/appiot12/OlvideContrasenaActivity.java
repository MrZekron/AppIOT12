package com.example.appiot12; // 📦 Este archivo vive dentro del paquete principal de la app

import android.app.ProgressDialog; // ⏳ Ventana emergente que dice "cargando..."
import android.os.Bundle; // 🎒 Información del estado cuando inicia la pantalla
import android.util.Patterns; // 📧 Para validar que un correo realmente es un correo
import android.view.View; // 👆 Para detectar clics
import android.widget.Button; // 🔘 Botones de la pantalla
import android.widget.EditText; // ✏️ Campo donde se escribe el email
import android.widget.Toast; // 🍞 Mensajes cortos

import androidx.activity.EdgeToEdge; // 📱 Interfaz de pantalla completa
import androidx.appcompat.app.AppCompatActivity; // 🏛️ Clase base de las Activities
import androidx.core.graphics.Insets; // 📐 Límites visuales
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth; // 🔐 Para enviar correo de recuperación
import com.google.firebase.database.DataSnapshot; // 📦 Datos desde Firebase
import com.google.firebase.database.DatabaseError; // ❌ Error al leer DB
import com.google.firebase.database.DatabaseReference; // 📍 Referencia a un nodo
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Base de datos completa
import com.google.firebase.database.ValueEventListener; // 👂 Escuchar datos una sola vez

// ⭐ ACTIVIDAD "OLVIDÉ MI CONTRASEÑA" ⭐
// Aquí el usuario escribe su correo y recibe un email para recuperarla ✉️🛟
public class OlvideContrasenaActivity extends AppCompatActivity {

    EditText etEmailOlvide; // ✉️ Caja para escribir correo
    Button btnEnviar, btnCancelarOlvide; // ▶️ Enviar enlace / ✖ Cancelar y volver
    private FirebaseAuth mAuth; // 🔐 Sistema de autenticación Firebase
    private DatabaseReference usuariosRef; // 🔎 Ruta /usuarios
    private ProgressDialog progressDialog; // ⏳ Ventanita de “Espere…”

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 🎬 Inicia pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 📱 Pantalla completa activa
        setContentView(R.layout.activity_olvide_contrasena); // 🎨 Dibujamos el layout

        // 📐 Ajustamos para que la UI no se esconda detrás de la barra superior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔍 Buscamos elementos del XML
        etEmailOlvide = findViewById(R.id.etEmailOlvide); // ✉️ Campo donde escriben el correo
        btnEnviar = findViewById(R.id.btnEnviar); // ▶️ Botón “Enviar”
        btnCancelarOlvide = findViewById(R.id.btnCancelarOlvide); // ✖ Botón “Cancelar”

        // 🔐 Inicializamos Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // 🗺 Apuntamos a: /usuarios en Firebase Database
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // ⏳ Preparamos la ventana emergente de progreso
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false); // 🙅 No se puede cerrar tocando fuera

        // 🎯 Listeners de botones
        btnEnviar.setOnClickListener(this::sendResetPassword); // Enviar enlace
        btnCancelarOlvide.setOnClickListener(this::cancelReset); // Cancelar
    }

    // 📩 Enviar correo de recuperación
    public void sendResetPassword(View view) {

        // 📨 Leemos lo que escribió el usuario
        String email = etEmailOlvide.getText().toString().trim();

        // 🔎 Validaciones básicas
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese su correo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⏳ Mostramos mensaje de progreso
        progressDialog.setMessage("Verificando correo registrado...");
        progressDialog.show();
        btnEnviar.setEnabled(false); // 🚫 Desactivar botón para evitar spam

        // 📍 1) Verificamos si el correo existe en /usuarios
        usuariosRef
                .orderByChild("email") // 🔍 Buscar por el campo "email"
                .equalTo(email) // 🤝 Compararlo con el correo ingresado
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // 🎉 Si el correo está registrado…

                            progressDialog.setMessage("Enviando enlace de restablecimiento... ✉️");

                            // 2) Enviamos email de recuperación desde Firebase Auth
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        btnEnviar.setEnabled(true);

                                        if (task.isSuccessful()) {
                                            // ✔️ Enlace enviado
                                            Toast.makeText(
                                                    OlvideContrasenaActivity.this,
                                                    "Revise su correo para restablecer la contraseña",
                                                    Toast.LENGTH_LONG).show();

                                            finish(); // 🚪 Cerramos esta pantalla
                                        } else {
                                            // ❌ Error al enviar
                                            String msg = (task.getException() != null)
                                                    ? task.getException().getMessage()
                                                    : "Error al enviar";

                                            Toast.makeText(
                                                    OlvideContrasenaActivity.this,
                                                    "Error al enviar: " + msg,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
                            // ❌ Si no está en la base de datos…
                            progressDialog.dismiss();
                            btnEnviar.setEnabled(true);

                            Toast.makeText(
                                    OlvideContrasenaActivity.this,
                                    "Correo no registrado. Cree una cuenta primero.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // 💥 Error leyendo la base de datos
                        progressDialog.dismiss();
                        btnEnviar.setEnabled(true);

                        Toast.makeText(
                                OlvideContrasenaActivity.this,
                                "Error al verificar correo: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✖ Cancelar y cerrar pantalla
    public void cancelReset(View view) {
        finish(); // ➡️ Volvemos a la pantalla anterior
    }
}
