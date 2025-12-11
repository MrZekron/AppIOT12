package com.example.appiot12;
// 📦 Activity destinada a mostrar TODOS los tanques registrados en el sistema.
// Ideal para administradores o dashboards globales 🌍💧

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 🚀 LISTA GLOBAL DE TANQUES
 *
 * Esta pantalla actualmente solo carga el layout vacío.
 * Es el “esqueleto” inicial para un módulo más grande:
 *
 *   ✔ Ver todos los tanques de todos los usuarios
 *   ✔ Permitir filtros globales
 *   ✔ Mostrar alertas por color (pH, turbidez, etc.)
 *   ✔ Panel tipo “smart city” o “control operacional”
 *
 * Aún no hay lógica, pero se deja la estructura lista para crecer.
 */
public class ListaGlobalTanques extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this); // 🖥️ Pantalla completa moderna
        setContentView(R.layout.activity_lista_global_tanques); // 🎨 Layout visual base

        // Ajuste elegante para que la UI no quede detrás de la barra superior/inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🚧 Aquí próximamente irá:
        //    - ListView o RecyclerView centrado en tanques globales
        //    - Lectura de todos los usuarios y sus tanques
        //    - Estadísticas completas del sistema
        //    - Filtros avanzados por estado, color o valores de sensor
        //
        //   Básicamente: el “Command Center” de AguaSegura 😎
    }
}
