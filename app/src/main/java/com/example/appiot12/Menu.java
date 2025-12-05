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
// Desde aquí el usuario puede ir a: ver lista de tanques, agregar tanque, configurar, etc.
public class Menu extends AppCompatActivity {

    private FirebaseAuth mAuth;        // 🔐 Controlador de la autenticación Firebase
    private TextView tvCorreoUsuario;  // ✉️ Texto donde mostramos el correo del usuario logueado

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 🎬 Se ejecuta cuando abrimos el menú
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 📱 Activa diseño que usa toda la pantalla
        setContentView(R.layout.activity_menu); // 🎨 Carga el diseño XML del menú

        // 📐 Ajustamos los márgenes para que la UI no se esconda detrás de la barra de estado o navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // 📏 Obtenemos tamaño de las barras
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); // 🧱 Agregamos espacio para que todo se vea bien
            return insets;
        });

        // 🔐 Obtenemos la instancia de FirebaseAuth para saber qué usuario está logueado
        mAuth = FirebaseAuth.getInstance();

        // 🔍 Buscamos el TextView en el layout donde mostraremos el correo
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario);

        // 📩 Cargamos y mostramos el correo del usuario actual
        cargarCorreoUsuario();
    }

    // 📩 Carga el correo del usuario autenticado y lo muestra en el nav del menú
    private void cargarCorreoUsuario() {
        FirebaseUser user = mAuth.getCurrentUser(); // 👤 Obtenemos el usuario actual

        if (user != null) { // ✅ Si hay alguien logueado...
            String correo = user.getEmail(); // ✉️ Obtenemos su correo

            if (correo != null && !correo.isEmpty()) { // 📌 Si el correo no es vacío...
                tvCorreoUsuario.setText(correo); // ✅ Lo mostramos tal cual
            } else {
                // 🤷‍♂️ Si no pudimos leer el correo, dejamos un mensaje genérico
                tvCorreoUsuario.setText("Sesión activa");
            }
        } else {
            // 😢 No hay sesión activa
            tvCorreoUsuario.setText("Sin sesión");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
        }
    }

    // ⚙️ Abrir pantalla de Configuración cuando el usuario toca el NAV superior
    public void abrirConfiguracion(View v) {
        Intent intent = new Intent(this, Configuracion.class); // 🚪 Queremos ir a Configuracion
        startActivity(intent); // ▶ Iniciamos la nueva pantalla
    }

    // ➕ Botón para ir a la pantalla de AGREGAR un nuevo tanque
    public void agregar(View v) {
        startActivity(new Intent(this, Agregar.class)); // ▶ Abrimos la Activity Agregar
    }

    // 📋 Botón para ver la LISTA de tanques registrados por el usuario
    public void lista(View v) {
        startActivity(new Intent(this, Lista.class)); // ▶ Abrimos la Activity Lista
    }

    // 🚪 Botón SALIR: cierra sesión y vuelve a la pantalla de inicio (MainActivity)
    public void salir(View v) {
        mAuth.signOut(); // 🔐 Cerramos la sesión del usuario en Firebase

        // 🚀 Creamos un Intent para ir a la pantalla principal (login)
        Intent intent = new Intent(this, MainActivity.class);

        // 🧹 Limpiamos el stack de Activities para que no pueda volver con "back"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent); // ▶ Abrimos la pantalla de login
        finish(); // 🚪 Cerramos el menú para que no quede en segundo plano
    }
}
