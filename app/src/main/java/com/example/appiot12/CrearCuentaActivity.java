package com.example.appiot12;
// 📦 Paquete base del proyecto Agua Segura.
// Aquí vive la pantalla para crear cuentas nuevas 👤✨

// ===== IMPORTS ANDROID =====
import android.app.ProgressDialog;      // ⏳ Ventanita de “cargando…”
import android.content.Intent;          // 🚪 Navegación entre pantallas
import android.os.Bundle;               // 🎒 Estado de la Activity
import android.util.Patterns;           // 🔍 Validación de correos
import android.view.View;               // 👆 Eventos de clic
import android.widget.Button;           // 🔘 Botones
import android.widget.EditText;         // 📝 Campos de texto
import android.widget.Toast;            // 🍞 Mensajes rápidos

import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity base

// ===== IMPORTS FIREBASE =====
import com.google.android.gms.tasks.Task;        // 📦 Resultado de tareas
import com.google.firebase.auth.AuthResult;      // 🔐 Resultado de Auth
import com.google.firebase.auth.FirebaseAuth;    // 🔐 Autenticación
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // 💥 Correo duplicado
import com.google.firebase.auth.FirebaseUser;    // 👤 Usuario
import com.google.firebase.auth.UserProfileChangeRequest; // 🎨 Nombre de perfil
import com.google.firebase.database.FirebaseDatabase; // ☁️ Base de datos

import java.util.HashMap;
import java.util.Map;
// 🧱 Mapas para guardar datos del usuario

/**
 * 🎇 CrearCuentaActivity 🎇
 *
 * Esta pantalla permite:
 * 👉 Crear una cuenta nueva
 * 👉 Validar datos básicos
 * 👉 Registrar el usuario en Firebase
 * 👉 Asignar rol (admin o usuario)
 *
 * En simple:
 * Es la puerta de entrada a Agua Segura 🚪💧
 */
public class CrearCuentaActivity extends AppCompatActivity {

    // 📝 Campos del formulario
    private EditText etNombre;
    private EditText etEmail;
    private EditText etPass;
    private EditText etPassConfirm;

    // 🔘 Botón principal
    private Button btnCrear;

    // 🔐 Firebase Auth
    private FirebaseAuth auth;

    // ⏳ Diálogo de carga
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos la UI con el XML
        inicializarVistas();

        // 🔐 Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance();

        // ⏳ Configuramos el diálogo de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 🔘 Acción principal: crear cuenta
        btnCrear.setOnClickListener(this::crearCuenta);
    }

    /**
     * 🔗 Conecta los EditText y botones con el XML
     */
    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmailCrear);
        etPass = findViewById(R.id.etPassCrear);
        etPassConfirm = findViewById(R.id.etPassConfirm);
        btnCrear = findViewById(R.id.btnCrearCuenta);
    }

    // =====================================================
    // 🧠 VALIDAR DATOS Y COMENZAR REGISTRO
    // =====================================================
    private void crearCuenta(View view) {

        // ✏️ Leemos lo que escribió el usuario
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String pass = etPass.getText().toString().trim();
        String passConfirm = etPassConfirm.getText().toString().trim();

        // 🛑 Validaciones básicas
        if (!datosValidos(nombre, email, pass, passConfirm)) {
            return; // ❌ Algo estaba mal
        }

        // ⏳ Mostramos carga
        progressDialog.setMessage("Creando cuenta... ⏳");
        progressDialog.show();
        btnCrear.setEnabled(false);

        // 🔍 Verificamos si el correo ya existe
        auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful() || task.getResult() == null) {
                        crearUsuarioFirebase(nombre, email, pass);
                        return;
                    }

                    if (task.getResult().getSignInMethods() != null &&
                            !task.getResult().getSignInMethods().isEmpty()) {

                        // 💥 Correo ya registrado
                        restaurarUI();
                        Toast.makeText(
                                this,
                                "El correo ya está registrado ❌",
                                Toast.LENGTH_LONG
                        ).show();
                    } else {
                        crearUsuarioFirebase(nombre, email, pass);
                    }
                });
    }

    /**
     * ✅ Valida los datos del formulario
     */
    private boolean datosValidos(
            String nombre,
            String email,
            String pass,
            String passConfirm
    ) {

        if (nombre.isEmpty() || email.isEmpty() ||
                pass.isEmpty() || passConfirm.isEmpty()) {

            Toast.makeText(
                    this,
                    "Completa todos los campos 📝",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                    this,
                    "Correo inválido 📧❌",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (!pass.equals(passConfirm)) {
            Toast.makeText(
                    this,
                    "Las contraseñas no coinciden 🔐",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (pass.length() < 6) {
            Toast.makeText(
                    this,
                    "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true; // ✔️ Todo bien
    }

    // =====================================================
    // 🔐 CREAR USUARIO EN FIREBASE AUTH
    // =====================================================
    private void crearUsuarioFirebase(String nombre, String email, String pass) {

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {
                        manejarErrorRegistro(task);
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();

                    if (user == null) {
                        restaurarUI();
                        Toast.makeText(
                                this,
                                "Error interno 😢",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    // 🎨 Asignamos nombre al perfil
                    UserProfileChangeRequest profile =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();
                    user.updateProfile(profile);

                    // 📧 Enviamos correo de verificación
                    user.sendEmailVerification();

                    // 🏷️ Asignamos rol
                    boolean esAdmin = email.endsWith("@aguasegura.cl");
                    String rol = esAdmin ? "admin" : "usuario";

                    // ☁️ Guardamos datos en la base
                    guardarUsuarioEnDatabase(user.getUid(), email, rol, esAdmin);
                });
    }

    /**
     * ☁️ Guarda la información del usuario en Realtime Database
     */
    private void guardarUsuarioEnDatabase(
            String uid,
            String email,
            String rol,
            boolean esAdmin
    ) {

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("id", uid);
        datosUsuario.put("correo", email);
        datosUsuario.put("rol", rol);
        datosUsuario.put("tanques", new HashMap<>());
        datosUsuario.put("createdAt", System.currentTimeMillis());

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .setValue(datosUsuario)
                .addOnCompleteListener(dbTask -> {

                    restaurarUI();

                    if (!dbTask.isSuccessful()) {
                        Toast.makeText(
                                this,
                                "Error al guardar usuario ❌",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    Toast.makeText(
                            this,
                            "Cuenta creada correctamente 🎉",
                            Toast.LENGTH_LONG
                    ).show();

                    // 🚪 Redirigimos según rol
                    Intent intent = esAdmin
                            ? new Intent(this, MenuAdmin.class)
                            : new Intent(this, Menu.class);

                    startActivity(intent);
                    finish();
                });
    }

    /**
     * ⚠️ Maneja errores al crear cuenta
     */
    private void manejarErrorRegistro(Task<AuthResult> task) {

        restaurarUI();

        String mensaje;

        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
            mensaje = "El correo ya está registrado ❌";
        } else if (task.getException() != null) {
            mensaje = task.getException().getMessage();
        } else {
            mensaje = "Error desconocido 😢";
        }

        Toast.makeText(
                this,
                "Registro fallido: " + mensaje,
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * ♻️ Restaura la UI luego de una operación
     */
    private void restaurarUI() {
        progressDialog.dismiss();
        btnCrear.setEnabled(true);
    }

    // =====================================================
    // ❌ CANCELAR REGISTRO
    // =====================================================
    public void cancelCreateAccount(View view) {
        finish(); // 🚪 Cerramos pantalla
    }
}
