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
public class Menu extends AppCompatActivity {

    private FirebaseAuth mAuth;        // 🔐 Controlador de la autenticación Firebase
    private TextView tvCorreoUsuario;  // ✉️ Texto donde mostramos el correo del usuario logueado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // Ajustar márgenes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario);

        cargarCorreoUsuario();
    }

    // 📩 Mostrar correo
    private void cargarCorreoUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String correo = user.getEmail();

            if (correo != null && !correo.isEmpty()) {
                tvCorreoUsuario.setText(correo);
            } else {
                tvCorreoUsuario.setText("Sesión activa");
            }
        } else {
            tvCorreoUsuario.setText("Sin sesión");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    // ⚙ Configuración
    public void abrirConfiguracion(View v) {
        startActivity(new Intent(this, Configuracion.class));
    }

    // ➕ Agregar tanque
    public void agregar(View v) {
        startActivity(new Intent(this, Agregar.class));
    }

    // 📋 Lista de tanques
    public void lista(View v) {
        startActivity(new Intent(this, Lista.class));
    }

    // 💸 Mis pagos
    public void pagos(View v) {
        startActivity(new Intent(this, HistorialCompra.class));
    }

    // 🛒 Comprar dispositivo
    public void comprarDispositivo(View v) {
        startActivity(new Intent(this, ComprarDispositivo.class));
    }

    // ⭐ NUEVO: Asociar dispositivo a tanque ⭐
    public void asociarDispositivo(View v) {
        startActivity(new Intent(this, AsociarDispositivoATanque.class));
    }

    // 🚪 Cerrar sesión
    public void salir(View v) {
        mAuth.signOut();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }
}
