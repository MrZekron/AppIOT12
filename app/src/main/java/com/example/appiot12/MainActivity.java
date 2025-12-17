package com.example.appiot12;
// 📦 Activity inicial del sistema AguaSegura.
// Es la pantalla de bienvenida: no hace lógica, solo redirige 👋💧

import android.content.Intent; // 🚪 Para cambiar de pantalla
import android.os.Bundle; // 🎒 Datos del ciclo de vida
import android.view.View; // 👆 Detectar clics

import androidx.activity.EdgeToEdge; // 📱 UI moderna sin bordes
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity base
import androidx.core.graphics.Insets; // 📐 Márgenes del sistema
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 🏠 MAIN ACTIVITY
 *
 * Explicado para un niño 👶:
 * 👉 Esta pantalla es como la puerta de entrada a una casa 🏡
 * 👉 Aquí no hacemos nada complicado
 * 👉 Solo mostramos bienvenida y enviamos al login 🔐
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 📱 Activamos modo pantalla completa (edge-to-edge)
        EdgeToEdge.enable(this);

        // 🎨 Cargamos el diseño visual de la pantalla principal
        setContentView(R.layout.activity_main);

        // 🧩 Ajustamos la vista para que no quede debajo
        // de la barra superior o inferior del celular
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // 📏 Aplicamos los márgenes correctos
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    /**
     * ▶️ iniciar()
     *
     * Método conectado al botón del XML:
     * android:onClick="iniciar"
     *
     * ¿Qué hace?
     * 👉 Envía al usuario a la pantalla de inicio de sesión 🔐
     */
    public void iniciar(View view) {
        startActivity(new Intent(this, IniciarSesion.class));
        // 🚀 Lanzamos la pantalla de login
    }
}
