package com.example.appiot12;
// 📦 Pantalla crítica de inicio de sesión del proyecto Agua Segura.
// Aquí se decide quién entra, quién no y a dónde va 🚦🔐

// ============================================================
// === IMPORTS ANDROID ===
// ============================================================
import android.app.ProgressDialog; // ⏳ Ventana de carga
import android.content.Intent;     // 🚪 Navegación entre pantallas
import android.os.Bundle;          // 🎒 Estado de la Activity
import android.text.TextUtils;     // 🧹 Validaciones simples
import android.view.View;          // 👆 Clicks del usuario
import android.widget.Button;      // 🔘 Botón principal
import android.widget.EditText;    // 📝 Inputs
import android.widget.Toast;       // 🍞 Mensajes cortos

import androidx.appcompat.app.AppCompatActivity;
// 🏛 Activity base estable y estándar

// ============================================================
// === IMPORTS FIREBASE ===
// ============================================================
import com.google.firebase.auth.FirebaseAuth;      // 🔐 Login
import com.google.firebase.auth.FirebaseUser;      // 👤 Usuario autenticado
import com.google.firebase.database.DataSnapshot;  // 📦 Lectura de datos
import com.google.firebase.database.DatabaseError; // ❌ Error Firebase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * 🧑‍💻 IniciarSesion
 *
 * ¿Qué hace esta pantalla?
 * 👉 Permite al usuario iniciar sesión
 * 👉 Verifica correo y contraseña
 * 👉 Valida que exista perfil en la base de datos
 * 👉 Revisa si está bloqueado
 * 👉 Decide si va a Menú normal o Menú Admin
 *
 * Explicado para un niño:
 * 👉 Es como la puerta del colegio:
 *    miran tu nombre, ven si estás castigado 😅
 *    y te mandan a tu sala correcta 🏫🙂
 */
public class IniciarSesion extends AppCompatActivity {

    // =====================================================
    // 🖥️ ELEMENTOS DE LA UI
    // =====================================================
    private EditText etCorreo;
    private EditText etContrasena;
    private Button btnIngresar;

    // =====================================================
    // 🔐 FIREBASE
    // =====================================================
    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;

    // =====================================================
    // ⏳ PROGRESO
    // =====================================================
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion); // 🎨 Cargamos la pantalla

        // 🔗 Vinculamos vistas
        inicializarVistas();

        // 🔐 Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance()
                .getReference("usuarios");

        // ⏳ Configuramos el diálogo de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 🔘 Acción del botón ingresar
        btnIngresar.setOnClickListener(v -> intentarLogin());
    }

    // =====================================================
    // 🔗 INICIALIZAR VISTAS
    // =====================================================
    private void inicializarVistas() {
        etCorreo = findViewById(R.id.tvCorreo);
        etContrasena = findViewById(R.id.tvContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);
    }

    // =====================================================
    // 🔐 INTENTAR INICIO DE SESIÓN
    // =====================================================
    private void intentarLogin() {

        // 📥 Leer datos ingresados
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        // 🛑 Validación básica
        if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(contrasena)) {
            Toast.makeText(this,
                    "Ingresa correo y contraseña 📝",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ⏳ Mostrar cargando
        progressDialog.setMessage("Autenticando... 🔐");
        progressDialog.show();
        btnIngresar.setEnabled(false);

        // 🔐 Login en Firebase Auth
        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        // ❌ Error de login
                        mostrarError("Correo o contraseña incorrectos ❌");
                        return;
                    }

                    // ✔ Usuario autenticado
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user == null) {
                        mostrarError("Error inesperado 😵");
                        return;
                    }

                    // 🔍 Validar perfil en base de datos
                    validarPerfilUsuario(user.getUid(), correo);
                });
    }

    // =====================================================
    // 📂 VALIDAR PERFIL EN REALTIME DATABASE
    // =====================================================
    private void validarPerfilUsuario(String uid, String correoIngresado) {

        usuariosRef.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        progressDialog.dismiss();
                        btnIngresar.setEnabled(true);

                        if (!snapshot.exists()) {
                            Toast.makeText(IniciarSesion.this,
                                    "Perfil no encontrado ❌",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        String correoDb = snapshot.child("correo").getValue(String.class);
                        String rol = snapshot.child("rol").getValue(String.class);
                        Boolean bloqueado = snapshot.child("bloqueado").getValue(Boolean.class);

                        // 📧 Validar correo
                        if (correoDb == null || !correoDb.equalsIgnoreCase(correoIngresado)) {
                            Toast.makeText(IniciarSesion.this,
                                    "El correo no coincide ⚠️",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 🚫 Usuario bloqueado
                        if (bloqueado != null && bloqueado) {
                            mAuth.signOut();
                            Toast.makeText(IniciarSesion.this,
                                    "Cuenta bloqueada ❌\nContacta a un administrador",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // 🚦 Redirigir según rol
                        redirigirSegunRol(rol, correoDb);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        mostrarError("Error al leer usuario ⚠️");
                    }
                });
    }

    // =====================================================
    // 🚦 REDIRECCIÓN SEGÚN ROL
    // =====================================================
    private void redirigirSegunRol(String rol, String correo) {

        Intent intent;

        if ("admin".equalsIgnoreCase(rol)) {
            intent = new Intent(this, MenuAdmin.class);
        } else {
            intent = new Intent(this, Menu.class);
        }

        intent.putExtra("usuarioCorreo", correo);
        startActivity(intent);
        finish(); // 🚪 Cerramos login
    }

    // =====================================================
    // ❌ MOSTRAR ERROR Y RESETEAR UI
    // =====================================================
    private void mostrarError(String mensaje) {
        progressDialog.dismiss();
        btnIngresar.setEnabled(true);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    // =====================================================
    // 🌱 IR A CREAR CUENTA
    // =====================================================
    public void goToCrearCuenta(View view) {
        startActivity(new Intent(this, CrearCuentaActivity.class));
    }

    // =====================================================
    // 🔑 IR A OLVIDÉ CONTRASEÑA
    // =====================================================
    public void goToOlvideContrasena(View view) {
        startActivity(new Intent(this, OlvideContrasenaActivity.class));
    }
}
