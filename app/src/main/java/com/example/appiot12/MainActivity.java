package com.example.appiot12;
// 📦 Paquete principal donde vive la Activity “lobby” del sistema AguaSegura

// === IMPORTS ANDROID / UI ===
import android.content.Intent; // 🚪 Permite saltar a otras pantallas
import android.os.Bundle; // 🎒 Estado de la Activity al crearse
import android.view.View; // 👆 Para manejar clics

// === LIBRERÍAS DE UI MODERNAS ===
import androidx.activity.EdgeToEdge; // ↔️ Permite UI a pantalla completa estilo moderno
import androidx.appcompat.app.AppCompatActivity; // 🏛 Activity con compatibilidad extendida
import androidx.core.graphics.Insets; // 📐 Márgenes de sistema
import androidx.core.view.ViewCompat; // 🛠 Utilidades para Views
import androidx.core.view.WindowInsetsCompat; // 🪟 Manejo de insets del sistema (notch / barra)

// 🎯 Esta es la Activity inicial del proyecto (la pantalla de bienvenida).
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        // ↗️ Activa el modo “edge-to-edge”: la UI puede usar toda la pantalla,
        // respetando notch, barras y curvas del teléfono 😎📱

        setContentView(R.layout.activity_main);
        // 🖼️ Infla el layout XML que define la pantalla principal

        // 🔧 Ajustar automáticamente padding para evitar que la UI
        // quede debajo de la barra de estado o la barra de navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 📏 Obtenemos los márgenes reales del sistema (status bar, nav bar)

            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            // ↔️ Aplicamos esos márgenes al contenedor principal

            return insets;
            // 🔁 Devolvemos insets sin consumirlos (para que otros listeners puedan usarlos)
        });
    }

    /**
     * 🔐 MÉTODO iniciar()
     * Este método está vinculado al botón en el layout via:
     *      android:onClick="iniciar"
     *
     * Su misión:
     * 👉 Enviar al usuario a la pantalla de login (IniciarSesion)
     *    donde validará su identidad.
     */
    public void iniciar(View view) {
        startActivity(new Intent(this, IniciarSesion.class));
        // ▶️ Lanzamos la Activity de inicio de sesión
        // y dejamos esta como “pantalla de bienvenida”.
    }

}
