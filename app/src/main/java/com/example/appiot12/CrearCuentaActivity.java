package com.example.appiot12; // 📦 Aquí vive esta clase dentro del proyecto

// === IMPORTS ===
// Son como herramientas que pedimos prestadas para construir nuestra app 🛠️

import android.app.ProgressDialog; // ⏳ Ventanita de cargando
import android.content.Intent; // 🚪 Para cambiar de pantalla
import android.os.Bundle; // 👜 Datos que pasan entre pantallas
import android.util.Patterns; // 🔍 Para validar correos electrónicos
import android.view.View; // 👆 Escuchar clics
import android.widget.Button; // 🔘 Botones
import android.widget.EditText; // 📝 Cajitas de texto
import android.widget.Toast; // 🍞 Mensajes tipo “snack”

import androidx.activity.EdgeToEdge; // 📱 Pantallas completas modernas
import androidx.appcompat.app.AppCompatActivity; // 🏛️ Clase base de una pantalla
import androidx.core.graphics.Insets; // 🧱 Para no tapar nada con barras del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener; // 📬 Saber cuando Firebase termina algo
import com.google.android.gms.tasks.Task; // 📦 Resultado de tareas
import com.google.firebase.auth.AuthResult; // 🔐 Resultado de crear usuario
import com.google.firebase.auth.FirebaseAuth; // 🔐 Gestor de usuarios (login)
import com.google.firebase.auth.FirebaseAuthUserCollisionException; // 💥 Si el correo ya existe
import com.google.firebase.auth.FirebaseUser; // 👤 Usuario creado
import com.google.firebase.auth.UserProfileChangeRequest; // 🎨 Cambiar nombre
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Guardar info en la base de datos

import java.util.HashMap; // 🧱 Mapas para guardar datos
import java.util.Map; // 🗂 Map genérico


// 🎇✨ PANTALLA PARA CREAR UNA CUENTA NUEVA ✨🎇
public class CrearCuentaActivity extends AppCompatActivity {

    // 🧪 Campos del formulario donde escribimos datos
    private EditText etNombre, etEmail, etPass, etPassConfirm;

    // 🔘 Botón para crear usuario
    private Button btnCrear;

    // 🔐 Controlador del login de Firebase
    private FirebaseAuth mAuth;

    // ⏳ Ventanita de "cargando..."
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this); // 📱 Pantalla completa
        setContentView(R.layout.activity_crear_cuenta); // 🎨 Layout visual

        // 🧱 Ajustar la pantalla para que no se esconda nada detrás de la barra superior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🏗️ Conectar XML con variables de Java
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmailCrear);
        etPass = findViewById(R.id.etPassCrear);
        etPassConfirm = findViewById(R.id.etPassConfirm);

        btnCrear = findViewById(R.id.btnCrearCuenta);

        // 🔐 Iniciar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // ⏳ Crear ventanita “Cargando”
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false); // ❌ No dejar cancelar

        // 🎯 Cuando presionan el botón, se ejecuta createAccount()
        btnCrear.setOnClickListener(this::createAccount);
    }

    // 📌 Paso 1: validar datos y preparar la creación de la cuenta
    public void createAccount(View view) {

        // 🧪 Tomar datos del usuario
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String pass = etPass.getText().toString().trim();
        String passConfirm = etPassConfirm.getText().toString().trim();

        // 🚨 Verificaciones básicas
        if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || passConfirm.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔍 Validar correo
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔑 Confirmar password
        if (!pass.equals(passConfirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // 📏 Largo mínimo
        if (pass.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⏳ Mostrar cargando...
        progressDialog.setMessage("Creando cuenta... 😎");
        progressDialog.show();
        btnCrear.setEnabled(false); // 🙅‍♂️ Evitar doble clic

        // 📨 Revisar si el correo ya está registrado
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(fetchTask -> {

            // ⚠️ Si falla la consulta, igual intentamos crear
            if (!fetchTask.isSuccessful() || fetchTask.getResult() == null) {
                proceedCreateUser(nombre, email, pass);
                return;
            }

            // ⚠️ Si ya tiene métodos de inicio, significa que el correo existe
            if (fetchTask.getResult().getSignInMethods() != null &&
                    !fetchTask.getResult().getSignInMethods().isEmpty()) {

                progressDialog.dismiss();
                btnCrear.setEnabled(true);
                Toast.makeText(this, "El correo ya está registrado.", Toast.LENGTH_LONG).show();
            }
            else {
                proceedCreateUser(nombre, email, pass);
            }
        });
    }

    // 📌 Paso 2: Crear el usuario REAL en Firebase Auth
    private void proceedCreateUser(String nombre, String email, String pass) {

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, (OnCompleteListener<AuthResult>) task -> {

                    // ❌ Error al crear usuario
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

                    String uid = firebaseUser.getUid(); // 🆔 ID único del usuario

                    // 🎨 Configurar el nombre del usuario
                    UserProfileChangeRequest profileUpdates =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();
                    firebaseUser.updateProfile(profileUpdates);

                    // 📧 Enviar verificación
                    firebaseUser.sendEmailVerification();

                    // ⭐ REGLA: Si el correo termina en @aguasegura.cl → es administrador
                    boolean esAdmin = email.endsWith("@aguasegura.cl");
                    String rolAsignado = esAdmin ? "admin" : "usuario";

                    // 🧱 Armar estructura del usuario para Firebase Database
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", uid);
                    userMap.put("correo", email);
                    userMap.put("rol", rolAsignado);
                    userMap.put("tanques", new HashMap<>()); // 🔹 Parte sin tanques
                    userMap.put("createdAt", System.currentTimeMillis());

                    // 💾 Guardar en Realtime Database
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

                                // ⭐ REDIRECCIONAR SEGÚN ROL ⭐
                                Intent intent;

                                if (esAdmin) {
                                    // 🚨 Jefazo detectado → entrar a MenuAdmin
                                    intent = new Intent(this, MenuAdmin.class);
                                } else {
                                    // 👤 Usuario normal → Menú principal
                                    intent = new Intent(this, Menu.class);
                                }

                                startActivity(intent);
                                finish();
                            });
                });
    }

    // ❌ Botón "Cancelar"
    public void cancelCreateAccount(View view) {
        finish(); // 🚪 Cerrar pantalla
    }
}
