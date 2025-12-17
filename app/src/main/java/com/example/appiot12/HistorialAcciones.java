package com.example.appiot12;
// 📦 Pantalla de historial del proyecto Agua Segura.
// Aquí se muestran las acciones recientes del usuario 📊🧾

// ===== IMPORTS ANDROID =====
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
// 🏛 Activity base estable

// ===== IMPORTS FIREBASE =====
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// ☁️ Firebase Realtime Database

// ===== IMPORTS JAVA =====
import java.util.ArrayList;
import java.util.List;

/**
 * 📜 HistorialAcciones
 *
 * ¿Qué hace esta pantalla?
 * 👉 Muestra las acciones del usuario
 * 👉 Solo trae registros de los últimos 30 días
 * 👉 Usa un ListView con AccionAdapter
 *
 * Explicado para un niño:
 * 👉 Es como ver el cuaderno donde se anotan
 *    todas las cosas importantes que hiciste 📒🙂
 */
public class HistorialAcciones extends AppCompatActivity {

    // 📋 Lista visual
    private ListView lvHistorial;

    // 🗂️ Lista en memoria con las acciones
    private final List<AccionLog> acciones = new ArrayList<>();

    // 🎨 Adaptador que convierte acciones → filas
    private AccionAdapter adapter;

    // ☁️ Referencia al historial en Firebase
    private DatabaseReference refHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_acciones); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos UI con el XML
        inicializarVistas();

        // 👤 Obtenemos UID del usuario
        String uid = obtenerUidUsuario();

        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ☁️ Apuntamos al historial del usuario
        refHistorial = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        // 🎨 Creamos el adaptador
        adapter = new AccionAdapter(this, acciones);
        lvHistorial.setAdapter(adapter);

        // 📥 Cargamos historial
        cargarHistorialUltimos30Dias();
    }

    /**
     * 🔗 Vincula el ListView con el XML
     */
    private void inicializarVistas() {
        lvHistorial = findViewById(R.id.lvHistorial);
    }

    /**
     * 👤 Obtiene el UID del usuario autenticado
     */
    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // =====================================================
    // 📥 CARGAR HISTORIAL (ÚLTIMOS 30 DÍAS)
    // =====================================================
    private void cargarHistorialUltimos30Dias() {

        // 🧠 Calculamos la fecha de hace 30 días
        long haceTreintaDias =
                System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        // 🔍 Consultamos Firebase por timestamp
        refHistorial
                .orderByChild("timestamp")
                .startAt(haceTreintaDias)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        acciones.clear(); // ♻️ Limpiamos lista

                        // 🔄 Recorremos cada acción
                        for (DataSnapshot s : snapshot.getChildren()) {

                            AccionLog log = s.getValue(AccionLog.class);

                            if (log != null) {
                                acciones.add(log);
                            }
                        }

                        // 📭 Si no hay acciones recientes
                        if (acciones.isEmpty()) {
                            Toast.makeText(
                                    HistorialAcciones.this,
                                    "No hay acciones en los últimos 30 días 📭",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        // 🔄 Actualizamos la lista
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(
                                HistorialAcciones.this,
                                "Error al cargar historial ⚠️",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
