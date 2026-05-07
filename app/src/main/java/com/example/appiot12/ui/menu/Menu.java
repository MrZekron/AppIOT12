package com.example.appiot12.ui.menu;
// 📦 Menú principal del usuario en AguaSegura.
// Es el "panel de control" donde el usuario decide qué hacer 💧🎛️

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.ui.pago.CentroPagos;
import com.example.appiot12.ui.pago.ComprarDispositivo;
import com.example.appiot12.ui.pago.HistorialCompra;
import com.example.appiot12.ui.perfil.Configuracion;
import com.example.appiot12.ui.tanque.Agregar;
import com.example.appiot12.ui.tanque.AsociarDispositivoATanque;
import com.example.appiot12.ui.tanque.Lista;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * 🏠 MENU DEL USUARIO
 *
 * Explicado para un niño 👶:
 * 👉 Esta pantalla es como el control remoto 📺
 * 👉 Desde aquí eliges qué parte de la app usar
 * 👉 No guarda datos, no calcula nada, solo te lleva a otros lugares 🚪
 */
public class Menu extends AppCompatActivity {

    // 🔐 Firebase controla quién está conectado
    private FirebaseAuth auth;

    // ✉️ Texto donde mostramos el correo del usuario
    private TextView tvCorreoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 📱 Activamos pantalla completa moderna
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // 📐 Ajustar márgenes para no chocar con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                }
        );

        // 🔗 Inicializar variables
        auth = FirebaseAuth.getInstance();
        tvCorreoUsuario = findViewById(R.id.tvCorreoUsuario);

        mostrarCorreoUsuario(); // 🎯 Mostrar quién está conectado
    }

    // ============================================================
    // ✉️ MOSTRAR CORREO DEL USUARIO CONECTADO
    // ============================================================
    private void mostrarCorreoUsuario() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            // Caso raro: no hay sesión
            tvCorreoUsuario.setText("Sin sesión activa");
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar correo si existe, si no → mensaje genérico
        String correo = user.getEmail();
        tvCorreoUsuario.setText(
                (correo != null && !correo.isEmpty())
                        ? correo
                        : "Sesión activa"
        );
    }

    // ============================================================
    // 🚀 MÉTODO GENÉRICO PARA NAVEGAR ENTRE PANTALLAS
    // ============================================================
    private void irA(Class<?> destino) {
        startActivity(new Intent(this, destino));
    }

    // ============================================================
    // 📌 OPCIONES DEL MENÚ (solo navegación)
    // ============================================================

    public void abrirConfiguracion(View v) {
        irA(Configuracion.class);
    }

    public void agregar(View v) {
        irA(Agregar.class);
    }

    public void lista(View v) {
        irA(Lista.class);
    }

    public void pagos(View v) {
        irA(HistorialCompra.class);
    }

    public void comprarDispositivo(View v) {
        irA(ComprarDispositivo.class);
    }

    public void asociarDispositivo(View v) {
        irA(AsociarDispositivoATanque.class);
    }

    public void centroPagos(View v) {
        irA(CentroPagos.class);
    }

    // ============================================================
    // 🚪 CERRAR SESIÓN
    // ============================================================
    public void salir(View v) {

        auth.signOut(); // 🔐 Cerrar sesión

        // Volvemos a la pantalla principal limpiando el historial
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivity(intent);
        finish(); // 🚫 Evita volver con botón atrás
    }
}
