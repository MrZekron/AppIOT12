package com.example.appiot12; // 📦 Paquete principal

import android.app.ProgressDialog; // ⏳ Ventana de carga
import android.content.Intent; // 🚪 Para navegar entre pantallas
import android.os.Bundle; // 🎒 Estado de la activity
import android.text.TextUtils; // 🧹 Validar campos vacíos
import android.view.View; // 👆 Manejar clics
import android.widget.Button; // 🔘 Botón iniciar sesión
import android.widget.EditText; // 📝 Campos de texto
import android.widget.Toast; // 🍞 Mensajes en pantalla

import androidx.activity.EdgeToEdge; // 📱 Vista completa moderna
import androidx.appcompat.app.AppCompatActivity; // 🏛️ Activity base
import androidx.core.graphics.Insets; // 📐 Márgenes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth; // 🔐 Login Firebase
import com.google.firebase.auth.FirebaseUser; // 👤 Usuario autenticado
import com.google.firebase.database.DataSnapshot; // 📦 Datos DB
import com.google.firebase.database.DatabaseError; // 🚫 Errores
import com.google.firebase.database.DatabaseReference; // 📍 Ruta DB
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Base de datos
import com.google.firebase.database.ValueEventListener; // 👂 Listener realtime

/**
 * 🧑‍💻 PANTALLA DE INICIO DE SESIÓN
 * Aquí el usuario ingresa correo + contraseña.
 * Se valida:
 *  - Usuario existe
 *  - Coincide con DB
 *  - NO esté bloqueado
 *  - Su rol para decidir Menu o MenuAdmin
 */
public class IniciarSesion extends AppCompatActivity {

    private EditText etCorreo, etContrasena;   // ✏️ Inputs de usuario
    private Button btnIngresar;                // 🔘 Botón ingresar
    private FirebaseAuth mAuth;                // 🔐 Auth Firebase
    private ProgressDialog progressDialog;     // ⏳ Ventana de carga
    private DatabaseReference usuariosRef;     // 📍 /usuarios en DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_iniciar_sesion);

        // Ajuste visual pantalla completa
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // Conectar UI con variables
        etCorreo = findViewById(R.id.tvCorreo);
        etContrasena = findViewById(R.id.tvContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

        // Ventana "cargando"
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnIngresar.setOnClickListener(v -> attemptLogin());
    }

    /**
     * 🔐 INTENTAR INICIAR SESIÓN
     */
    private void attemptLogin() {

        String emailInput = etCorreo.getText().toString().trim();
        String passInput = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passInput)) {
            Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Autenticando... 🔐");
        progressDialog.show();
        btnIngresar.setEnabled(false);

        // Paso 1: intentar login con Auth
        mAuth.signInWithEmailAndPassword(emailInput, passInput)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        btnIngresar.setEnabled(true);
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser fbUser = mAuth.getCurrentUser();

                    if (fbUser == null) {
                        progressDialog.dismiss();
                        btnIngresar.setEnabled(true);
                        Toast.makeText(this, "Error inesperado al obtener usuario", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = fbUser.getUid();

                    // Paso 2: leer datos del usuario en DB
                    usuariosRef.child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {

                                    progressDialog.dismiss();
                                    btnIngresar.setEnabled(true);

                                    if (!snapshot.exists()) {
                                        Toast.makeText(IniciarSesion.this,
                                                "Perfil de usuario no encontrado",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    // Extraer datos
                                    String correoDb = snapshot.child("correo").getValue(String.class);
                                    String rolDb = snapshot.child("rol").getValue(String.class);
                                    Boolean bloqueadoDb = snapshot.child("bloqueado").getValue(Boolean.class);

                                    // Validar correo
                                    if (correoDb == null || !correoDb.equals(emailInput)) {
                                        Toast.makeText(IniciarSesion.this,
                                                "El correo no coincide con la base de datos",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    // PASO EXTRA: verificar si está bloqueado
                                    if (bloqueadoDb != null && bloqueadoDb) {
                                        mAuth.signOut();
                                        Toast.makeText(IniciarSesion.this,
                                                "Tu cuenta está BLOQUEADA ❌\nContacta a un administrador.",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    // Decidir menú según rol
                                    if ("admin".equalsIgnoreCase(rolDb)) {

                                        Intent adminIntent = new Intent(IniciarSesion.this, MenuAdmin.class);
                                        adminIntent.putExtra("usuarioCorreo", correoDb);
                                        startActivity(adminIntent);
                                        finish();
                                        return;
                                    }

                                    // Usuario normal → Menú normal
                                    Intent menuIntent = new Intent(IniciarSesion.this, Menu.class);
                                    menuIntent.putExtra("usuarioCorreo", correoDb);
                                    startActivity(menuIntent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    progressDialog.dismiss();
                                    btnIngresar.setEnabled(true);
                                    Toast.makeText(IniciarSesion.this,
                                            "Error al leer usuario: " + error.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }

    // Navegar a crear cuenta
    public void goToCrearCuenta(View view) {
        startActivity(new Intent(IniciarSesion.this, CrearCuentaActivity.class));
    }

    // Navegar a "Olvidé mi contraseña"
    public void goToOlvideContrasena(View view) {
        startActivity(new Intent(IniciarSesion.this, OlvideContrasenaActivity.class));
    }
}
