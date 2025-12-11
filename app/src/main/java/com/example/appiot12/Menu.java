package com.example.appiot12;
// 📦 Clase ubicada en el paquete central del proyecto AguaSegura

// === IMPORTS ANDROID ===
import android.content.Intent; // 🚪 Permite navegar entre Activities
import android.os.Bundle; // 🎒 Estado y datos enviados a la Activity
import android.view.View; // 👆 Detectar clics
import android.widget.TextView; // 📝 Mostrar correo del usuario
import android.widget.Toast; // 🍞 Mensajes cortos informativos

// === UI MODERNA ===
import androidx.activity.EdgeToEdge; // 📱 Modo pantalla completa moderno
import androidx.appcompat.app.AppCompatActivity; // 🏛 Clase base
import androidx.core.graphics.Insets; // 📐 Márgenes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// === FIREBASE ===
import com.google.firebase.auth.FirebaseAuth; // 🔐 Control de autenticación
import com.google.firebase.auth.FirebaseUser; // 👤 Usuario logueado

/**
 * 🏠 MENU PRINCIPAL DEL USUARIO
 *
 * Esta pantalla funciona como el "Dashboard" inicial del cliente.
 * Desde aquí puede:
 *   ✔ Gestionar tanques
 *   ✔ Ver sensores en tiempo real
 *   ✔ Agregar dispositivos
 *   ✔ Revisar pagos
 *   ✔ Configurar su cuenta
 *
 * Es el hub central del ecosistema AguaSegura 💧🚀.
 */
public class Menu extends AppCompatActivity {

    private FirebaseAuth mAuth;           // 🔐 Controlador de sesión Firebase
    private TextView tvCorreoUsuario;     // ✉️ Zona para mostrar quién está conectado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);          // 📱 Activa modo moderno del layout
        setContentView(R.layout.activity_menu);

        // Ajuste automático según barras del sistema (notch-friendly)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // === VINCULAR VARIABLES ===
        mAuth = FirebaseAuth.getInstance();
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario);

        cargarCorreoUsuario(); // Mostrar correo en la parte superior 🎯
    }

    // ============================================================================
    // 📌 Mostrar correo del usuario logueado en la UI
    // ============================================================================
    private void cargarCorreoUsuario() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            String correo = user.getEmail();

            // Si existe correo → lo mostramos. Si no → "Sesión activa".
            if (correo != null && !correo.isEmpty()) {
                tvCorreoUsuario.setText(correo);
            } else {
                tvCorreoUsuario.setText("Sesión activa");
            }

        } else {
            // No debería pasar normalmente
            tvCorreoUsuario.setText("Sin sesión");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    // ============================================================================
    // 🚀 NAVEGACIÓN A TODAS LAS FUNCIONES PRINCIPALES
    // ============================================================================

    // ⚙ CONFIGURACIÓN DE CUENTA
    public void abrirConfiguracion(View v) {
        startActivity(new Intent(this, Configuracion.class));
    }

    // ➕ REGISTRAR UN NUEVO TANQUE
    public void agregar(View v) {
        startActivity(new Intent(this, Agregar.class));
    }

    // 📋 LISTA DE TANQUES DEL USUARIO
    public void lista(View v) {
        startActivity(new Intent(this, Lista.class));
    }

    // 💸 HISTORIAL DE COMPRAS DEL USUARIO
    public void pagos(View v) {
        startActivity(new Intent(this, HistorialCompra.class));
    }

    // 🛒 COMPRAR DISPOSITIVO NUEVO
    public void comprarDispositivo(View v) {
        startActivity(new Intent(this, ComprarDispositivo.class));
    }

    // 🔗 ASOCIAR DISPOSITIVO A UN TANQUE
    public void asociarDispositivo(View v) {
        startActivity(new Intent(this, AsociarDispositivoATanque.class));
    }

    // 🧾 CENTRO DE PAGOS (Pagar cuotas / total)
    public void centroPagos(View v) {
        startActivity(new Intent(this, CentroPagos.class));
    }

    // ============================================================================
    // 🚪 CERRAR SESIÓN
    // ============================================================================
    public void salir(View v) {

        mAuth.signOut();  // 🔐 Cerramos sesión Firebase

        // Redirigimos a la pantalla inicial limpiando el stack
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish(); // Cerrar menú para evitar regresar con BACK
    }
}
