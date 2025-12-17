package com.example.appiot12;
// 📦 Activity destinada a mostrar TODOS los tanques registrados en el sistema.
// Este es el **Command Center** del agua 💧📊 (modo administrador activado).

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 🚀 LISTA GLOBAL DE TANQUES
 *
 * 👉 ¿Qué ES hoy?
 * - Una pantalla base (sin lógica todavía).
 *
 * 👉 ¿Qué SERÁ mañana? (visión de futuro 🔮)
 * - Ver TODOS los tanques del sistema 🌍
 * - Filtros por estado (verde / amarillo / rojo 🚦)
 * - Alertas globales tipo Smart City 🏙️
 * - KPIs para administradores (CEOs felices 😎)
 *
 * Explicado para un niño 👶:
 * 👉 Es como una lista gigante con TODOS los estanques del mundo 💧💧💧
 */
public class ListaGlobalTanques extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🖥️ Activamos pantalla completa moderna (sin bordes feos)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_global_tanques);

        // 📐 Ajuste automático para que nada quede debajo de la barra del celular
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        // 🚧 Estado actual del módulo
        mostrarMensajeEnConstruccion();

        // 🧠 NOTA DE ARQUITECTURA (importante):
        // Esta Activity NO carga lógica todavía para evitar:
        // ❌ Código muerto
        // ❌ Lógica duplicada
        // ❌ Firebase innecesario
        //
        // Se implementará cuando:
        // ✔ Exista rol admin estable
        // ✔ Exista volumen real de datos
        // ✔ Se decida RecyclerView + Adapter global
    }

    // =========================================================
    // 🚧 MENSAJE TEMPORAL (UX amigable)
    // =========================================================
    private void mostrarMensajeEnConstruccion() {
        Toast.makeText(
                this,
                "Módulo en construcción 🚧\nPróximamente: Panel Global de Tanques 💧📊",
                Toast.LENGTH_LONG
        ).show();
    }
}
