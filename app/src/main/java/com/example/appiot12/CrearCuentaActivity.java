package com.example.appiot12; // 📦 Aquí vive esta clase dentro del proyecto

// === IMPORTS ===
// Son como herramientas que pedimos prestadas para construir nuestra app 🛠️

import android.app.ProgressDialog; // ⏳ Ventanita "cargando..."
import android.content.Intent; // 🚪 Navegación entre pantallas
import android.os.Bundle; // 🎒 Datos transportados entre Activities
import android.util.Patterns; // 🔍 Validación elegante de correos
import android.view.View; // 👆 Reconocer clics
import android.widget.Button; // 🔘 Botoncitos felices
import android.widget.EditText; // 📝 Entrada de texto
import android.widget.Toast; // 🍞 Notificaciones rápidas

import androidx.activity.EdgeToEdge; // 📱 UI que se expande hasta los bordes
import androidx.appcompat.app.AppCompatActivity; // 🏛 La madre de todas las pantallas
import androidx.core.graphics.Insets; // 📐 Gestión de bordes del sistema
import androidx.core.view.ViewCompat; // 🛠 Utilidades de vista
import androidx.core.view.WindowInsetsCompat; // 🪟 Insets del sistema

// === FIREBASE ===
import com.google.android.gms.tasks.OnCompleteListener; // 📬 Saber cuándo Firebase terminó una tarea
import com.google.android.gms.tasks.Task; // 📦 Resultado de operaciones asíncronas
import com.google.firebase.auth.AuthResult; // 🔐 Resultado de creación de usuario
import com.google.firebase.auth.FirebaseAuth; // 🔐 Control total de sesiones
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // 💥 Correo ya registrado
import com.google.firebase.auth.FirebaseUser; // 👤 Usuario autenticado
import com.google.firebase.auth.UserProfileChangeRequest; // 🎨 Asignar nombre al usuario
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Realtime Database

import java.util.HashMap; // 🧱 Mapa clave-valor para insertar datos
import java.util.Map; // 🗂️ Mapa genérico

// 🎇✨ PANTALLA PARA CREAR UNA CUENTA NUEVA ✨🎇
public class CrearCuentaActivity extends AppCompatActivity {

    // 🧪 Campos del formulario donde escribimos datos
    private EditText etNombre, etEmail, etPass, etPassConfirm;

    // 🔘 Botón principal
    private Button btnCrear;

    // 🔐 Autenticador de Firebase
    private FirebaseAuth mAuth;

    // ⏳ Ventanita con “Cargando...”
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this); // 📱 Pantalla completa moderna
        setContentView(R.layout.activity_crear_cuenta); // 🎨 UI cargada

        // Ajustar contenido a los bordes del sistema para evitar recortes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🏗️ Vincular componentes con el XML
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmailCrear);
        etPass = findViewById(R.id.etPassCrear);
        etPassConfirm = findViewById(R.id.etPassConfirm);
        btnCrear = findViewById(R.id.btnCrearCuenta);

        // Iniciamos Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Crear ventanita de progreso
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false); // ❌ Evitar cerrar accidentalmente

        // Cuando se presiona el botón, creamos cuenta
        btnCrear.setOnClickListener(this::createAccount);
    }

    // ============================================================
    // 📌 VALIDAR CAMPOS Y PREPARAR CREACIÓN DE CUENTA
    // ============================================================
    public void createAccount(View view) {

        // Tomar valores del formulario
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String pass = etPass.getText().toString().trim();
        String passConfirm = etPassConfirm.getText().toString().trim();

        // Validaciones básicas
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || passConfirm.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato del correo
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar coincidencia de contraseñas
        if (!pass.equals(passConfirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Requisitos mínimos de seguridad
        if (pass.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progreso
        progressDialog.setMessage("Creando cuenta... 😎");
        progressDialog.show();
        btnCrear.setEnabled(false); // Evita doble registro

        // Comprobar si el correo ya existe
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(fetchTask -> {

            if (!fetchTask.isSuccessful() || fetchTask.getResult() == null) {
                proceedCreateUser(nombre, email, pass);
                return;
            }

            // Si Firebase devuelve métodos de inicio → ya está registrado
            if (fetchTask.getResult().getSignInMethods() != null &&
                    !fetchTask.getResult().getSignInMethods().isEmpty()) {

                progressDialog.dismiss();
                btnCrear.setEnabled(true);
                Toast.makeText(this, "El correo ya está registrado.", Toast.LENGTH_LONG).show();
            } else {
                proceedCreateUser(nombre, email, pass);
            }
        });
    }

    // ============================================================
    // 📌 CREAR USUARIO EN FIREBASE AUTH
    // ============================================================
    private void proceedCreateUser(String nombre, String email, String pass) {

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, (OnCompleteListener<AuthResult>) task -> {

                    // ❌ Falló la creación
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        btnCrear.setEnabled(true);

                        String msg;
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            msg = "El correo ya está registrado.";
                        } else {
                            msg = (task.getException() != null ?
                                    task.getException().getMessage() :
                                    "Error al crear cuenta");
                        }

                        Toast.makeText(this, "Registro fallido: " + msg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✔ Usuario creado correctamente
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                    if (firebaseUser == null) {
                        progressDialog.dismiss();
                        btnCrear.setEnabled(true);
                        Toast.makeText(this, "Error interno: usuario nulo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = firebaseUser.getUid(); // 🆔 ID del usuario en Firebase

                    // Actualizar nombre del perfil
                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();
                    firebaseUser.updateProfile(profileUpdates);

                    // Enviar correo de verificación
                    firebaseUser.sendEmailVerification();

                    // REGLA DE ORO: correos con @aguasegura.cl → administradores
                    boolean esAdmin = email.endsWith("@aguasegura.cl");
                    String rolAsignado = esAdmin ? "admin" : "usuario";

                    // 🚀 Preparar estructura en la base de datos
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", uid);
                    userMap.put("correo", email);
                    userMap.put("rol", rolAsignado);
                    userMap.put("tanques", new HashMap<>()); // 🧱 Comienza sin tanques
                    userMap.put("createdAt", System.currentTimeMillis());

                    // Guardar en Realtime Database
                    FirebaseDatabase.getInstance()
                            .getReference("usuarios")
                            .child(uid)
                            .setValue(userMap)
                            .addOnCompleteListener(dbTask -> {

                                progressDialog.dismiss();
                                btnCrear.setEnabled(true);

                                if (!dbTask.isSuccessful()) {
                                    Toast.makeText(this,
                                            "Error al guardar datos del usuario.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Toast.makeText(this,
                                        "Cuenta creada correctamente 🎉",
                                        Toast.LENGTH_LONG).show();

                                // Redirección según rol
                                Intent intent = esAdmin
                                        ? new Intent(this, MenuAdmin.class)
                                        : new Intent(this, Menu.class);

                                startActivity(intent);
                                finish();
                            });
                });
    }

    // ============================================================
    // ❌ CANCELAR REGISTRO
    // ============================================================
    public void cancelCreateAccount(View view) {
        finish(); // Cierra la pantalla
    }
}
