package com.example.appiot12; // 📦 Este archivo pertenece al paquete principal de la app

import android.content.Intent; // 🚪 Para cambiar de pantalla (Activities)
import android.os.Bundle; // 🎒 Información al crear la Activity
import android.view.View; // 👆 Para manejar clics en botones o vistas
import android.widget.TextView; // 📝 Para mostrar el correo del usuario en pantalla
import android.widget.Toast; // 🍞 Mensajes cortos que aparecen abajo

import androidx.activity.EdgeToEdge; // 📱 Para usar el diseño de borde a borde
import androidx.appcompat.app.AppCompatActivity; // 🏛️ Clase base de una Activity moderna
import androidx.core.graphics.Insets; // 📐 Márgenes de barras del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth; // 🔐 Manejo de sesión del usuario en Firebase
import com.google.firebase.auth.FirebaseUser; // 👤 Representa al usuario que está logueado

// 🏠 Pantalla MENÚ PRINCIPAL de la app
// Desde aquí el usuario puede ir a: ver lista de tanques, agregar tanque, pagos, compra, configuración, etc.
public class Menu extends AppCompatActivity {

    private FirebaseAuth mAuth;        // 🔐 Controlador de la autenticación Firebase
    private TextView tvCorreoUsuario;  // ✉️ Texto donde mostramos el correo del usuario logueado

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 🎬 Se ejecuta al abrir el menú
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 📱 Activa diseño que usa toda la pantalla
        setContentView(R.layout.activity_menu); // 🎨 Carga el diseño XML del menú

        // 📐 Ajustar márgenes para que nada quede escondido
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance(); // 🔐 Instancia Firebase Auth
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario); // 📨 Mostrar correo

        cargarCorreoUsuario(); // 📩 Mostrar correo en pantalla
    }

    // 📩 Carga el correo del usuario autenticado y lo muestra en el menú
    private void cargarCorreoUsuario() {
        FirebaseUser user = mAuth.getCurrentUser(); // 👤 Usuario actual

        if (user != null) {
            String correo = user.getEmail();

            if (correo != null && !correo.isEmpty()) {
                tvCorreoUsuario.setText(correo); // ✔ Muestra el correo real
            } else {
                tvCorreoUsuario.setText("Sesión activa");
            }
        } else {
            tvCorreoUsuario.setText("Sin sesión");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    // ⚙️ Abrir pantalla de Configuración al tocar el nav superior
    public void abrirConfiguracion(View v) {
        startActivity(new Intent(this, Configuracion.class));
    }

    // ➕ Pantalla para agregar un tanque nuevo
    public void agregar(View v) {
        startActivity(new Intent(this, Agregar.class));
    }

    // 📋 Ver lista de tanques
    public void lista(View v) {
        startActivity(new Intent(this, Lista.class));
    }

    // 💸 ⭐ Abrir historial de pagos
    public void pagos(View v) {
        startActivity(new Intent(this, HistorialCompra.class));
    }

    // 🛒 ⭐ NUEVO: Comprar dispositivo
    public void comprarDispositivo(View v) {
        startActivity(new Intent(this, ComprarDispositivo.class));
    }

    // 🚪 Cerrar sesión y volver al login
    public void salir(View v) {
        mAuth.signOut();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }
}
