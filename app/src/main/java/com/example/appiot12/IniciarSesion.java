package com.example.appiot12; // 📦 Paquete principal donde vive esta pantalla crítica

// === IMPORTS ANDROID BÁSICOS ===
import android.app.ProgressDialog; // ⏳ Ventana modal de “Cargando...”
import android.content.Intent; // 🚪 Navegación entre Activities
import android.os.Bundle; // 🎒 Estado del Activity
import android.text.TextUtils; // 🧹 Utilidad para validar vacío
import android.view.View; // 👆 Detectar clics en botones
import android.widget.Button; // 🔘 Botón principal
import android.widget.EditText; // 📝 Input de usuario
import android.widget.Toast; // 🍞 Mensajes sutiles al usuario

// === LIBRERÍAS UI MODERNAS ===
import androidx.activity.EdgeToEdge; // 📱 UI estilo “pantalla completa sin bordes”
import androidx.appcompat.app.AppCompatActivity; // 🏛 Superclase de Activities estándar
import androidx.core.graphics.Insets; // 📐 Manejo de márgenes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// === FIREBASE AUTH & DATABASE ===
import com.google.firebase.auth.FirebaseAuth; // 🔐 Encargado del login
import com.google.firebase.auth.FirebaseUser; // 👤 Representación del usuario autenticado
import com.google.firebase.database.DataSnapshot; // 📦 Resultado de una lectura
import com.google.firebase.database.DatabaseError; // ❌ Error DB
import com.google.firebase.database.DatabaseReference; // 📍 Ruta a Firebase
import com.google.firebase.database.FirebaseDatabase; // 🛢️ Base de datos completa
import com.google.firebase.database.ValueEventListener; // 👂 Listener de eventos

/**
 * 🧑‍💻 PANTALLA DE INICIO DE SESIÓN (LOGIN)
 *
 * Aquí validamos:
 * ✔ Correo + contraseña
 * ✔ Que exista en Auth
 * ✔ Que el perfil esté en la base de datos
 * ✔ Que NO esté bloqueado
 * ✔ Que rol tiene (admin / usuario)
 *
 * Dependiendo de eso → redirige a Menu o MenuAdmin 🚀
 */
public class IniciarSesion extends AppCompatActivity {

    // === REFERENCIAS UI ===
    private EditText etCorreo, etContrasena; // ✏️ Inputs del usuario
    private Button btnIngresar;             // 🔘 Botón para iniciar sesión

    // === FIREBASE ===
    private FirebaseAuth mAuth;             // 🔐 Controlador de autenticación
    private DatabaseReference usuariosRef;  // 📍 Ruta a /usuarios

    // === UI AUXILIAR ===
    private ProgressDialog progressDialog;  // ⏳ Modal de progreso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 📱 Activar modo pantalla completa
        setContentView(R.layout.activity_iniciar_sesion);

        // Ajustar UI a los bordes del sistema (notch-friendly)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom); // 📏 Aplicar márgenes correctos
            return insets;
        });

        // === UNIR XML → JAVA ===
        etCorreo = findViewById(R.id.tvCorreo);
        etContrasena = findViewById(R.id.tvContrasena);
        btnIngresar = findViewById(R.id.btnIngresar);

        // === FIREBASE ===
        mAuth = FirebaseAuth.getInstance();                  // Obtenemos instancia del login
        usuariosRef = FirebaseDatabase.getInstance()         // Ruta base /usuarios
                .getReference("usuarios");

        // Modal de carga
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false); // ❌ Evita que se cierre accidentalmente

        // Listener del botón ingresar
        btnIngresar.setOnClickListener(v -> attemptLogin());
    }

    /**
     * ============================================================
     * 🔐 INTENTAR LOGIN DEL USUARIO
     * ============================================================
     */
    private void attemptLogin() {

        // 1️⃣ Obtener inputs
        String emailInput = etCorreo.getText().toString().trim();
        String passInput = etContrasena.getText().toString().trim();

        // 2️⃣ Validar campos vacíos
        if (TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passInput)) {
            Toast.makeText(this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3️⃣ Mostrar cargando
        progressDialog.setMessage("Autenticando... 🔐");
        progressDialog.show();
        btnIngresar.setEnabled(false);

        // 4️⃣ Iniciar sesión en Firebase Auth
        mAuth.signInWithEmailAndPassword(emailInput, passInput)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        // ❌ Error de autenticación
                        progressDialog.dismiss();
                        btnIngresar.setEnabled(true);
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✔ Usuario autenticado correctamente
                    FirebaseUser fbUser = mAuth.getCurrentUser();

                    if (fbUser == null) {
                        progressDialog.dismiss();
                        btnIngresar.setEnabled(true);
                        Toast.makeText(this, "Error inesperado: usuario nulo", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = fbUser.getUid(); // 🆔

                    // 5️⃣ Leer perfil completo desde Realtime Database
                    usuariosRef.child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot snapshot) {

                                    progressDialog.dismiss();
                                    btnIngresar.setEnabled(true);

                                    // Si no existe usuario en DB → no tiene perfil
                                    if (!snapshot.exists()) {
                                        Toast.makeText(IniciarSesion.this,
                                                "Perfil de usuario no encontrado",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    // Recuperar datos importantes del perfil
                                    String correoDb = snapshot.child("correo").getValue(String.class);
                                    String rolDb = snapshot.child("rol").getValue(String.class);
                                    Boolean bloqueadoDb = snapshot.child("bloqueado").getValue(Boolean.class);

                                    // Verificar que email coincide con DB
                                    if (correoDb == null || !correoDb.equals(emailInput)) {
                                        Toast.makeText(IniciarSesion.this,
                                                "El correo no coincide con la base de datos",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    // 🚫 Usuario bloqueado
                                    if (bloqueadoDb != null && bloqueadoDb) {
                                        mAuth.signOut();
                                        Toast.makeText(IniciarSesion.this,
                                                "Tu cuenta está BLOQUEADA ❌\nContacta a un administrador.",
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    // ========= DETERMINAR ROL =========
                                    if ("admin".equalsIgnoreCase(rolDb)) {

                                        Intent adminIntent = new Intent(IniciarSesion.this, MenuAdmin.class);
                                        adminIntent.putExtra("usuarioCorreo", correoDb);
                                        startActivity(adminIntent);
                                        finish(); // Cerrar login
                                        return;
                                    }

                                    // Usuario normal → ir a Menú estándar
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

    // ============================================================
    // 🌱 NAVEGAR A CREAR CUENTA
    // ============================================================
    public void goToCrearCuenta(View view) {
        startActivity(new Intent(IniciarSesion.this, CrearCuentaActivity.class));
    }

    // ============================================================
    // 🔑 NAVEGAR A "OLVIDÉ MI CONTRASEÑA"
    // ============================================================
    public void goToOlvideContrasena(View view) {
        startActivity(new Intent(IniciarSesion.this, OlvideContrasenaActivity.class));
    }
}
