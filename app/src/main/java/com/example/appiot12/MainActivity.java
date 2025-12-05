package com.example.appiot12; // 📦 paquete del proyecto

import android.content.Intent; // 🔁 crea Intents para navegar entre Activities
import android.os.Bundle; // 🗂️ Bundle para estado guardado de la Activity
import android.view.View; // 👆 View usada en onClick y listeners

import androidx.activity.EdgeToEdge; // ↔️ helper para UI edge-to-edge
import androidx.appcompat.app.AppCompatActivity; // 🧭 Activity con compatibilidad
import androidx.core.graphics.Insets; // 📐 representación de insets (márgenes del sistema)
import androidx.core.view.ViewCompat; // 🛠️ utilidades compatibles para Views
import androidx.core.view.WindowInsetsCompat; // 🪟 manejo compatible de WindowInsets

public class MainActivity extends AppCompatActivity { // 🧩 Activity principal

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ▶️ punto de entrada al crear la Activity
        super.onCreate(savedInstanceState); // ☑️ llama al onCreate de la superclase

        EdgeToEdge.enable(this); // ↗️ habilita que la UI use todo el área de pantalla (edge-to-edge) 📱

        setContentView(R.layout.activity_main); // 🖼️ infla el layout activity_main.xml

        // 🔧 Ajusta el padding para respetar status bar y navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // 📏 obtiene insets de systemBars
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom); // ↔️ aplica padding para evitar solapamientos
            return insets; // 🔁 devuelve insets sin consumir (otros pueden usarlo)
        });
    } // 🛑 fin onCreate

    // 🔐 Método ligado a android:onClick="iniciar" en el layout
    public void iniciar(View view) {
        startActivity(new Intent(this, IniciarSesion.class)); // ▶️ abre IniciarSesion (login) 🔒
    }

} // ✅ fin clase MainActivity
