package com.example.appiot12;
// 📦 Pantalla encargada de mostrar el historial de acciones del usuario.
// Aquí vive el "SAP de auditoría" del proyecto 😎📊

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class HistorialAcciones extends AppCompatActivity {

    private ListView lvHistorial;               // 📋 Lista visual para mostrar acciones
    private ArrayList<AccionLog> acciones = new ArrayList<>(); // 🗂 Contenedor dinámico de logs
    private AccionAdapter adapter;              // 🎨 Adaptador para transformar logs → UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 📱 Pantalla full edge
        setContentView(R.layout.activity_historial_acciones); // 🎨 Dibujamos UI

        lvHistorial = findViewById(R.id.lvHistorial); // Unimos ListView del XML

        adapter = new AccionAdapter(this, acciones);  // Creamos adaptador con lista vacía
        lvHistorial.setAdapter(adapter);              // Asignamos el adaptador al ListView

        cargarHistorial(); // 🚀 Descargamos historial desde Firebase
    }

    // ============================================================================
    // 📥 CARGAR HISTORIAL DESDE FIREBASE (solo últimos 30 días)
    // ============================================================================
    private void cargarHistorial() {

        // Obtenemos UID del usuario actual
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referencia: usuarios/{uid}/historial/
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        // Calculamos timestamp de hace 30 días:
        long treintaDias = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        // 🧠 Fórmula empresarial: 30 días * 24 horas * 60 min * 60 seg * 1000 ms

        // Consulta: traer registros ordenados por timestamp y solo desde hace 30 días
        ref.orderByChild("timestamp")
                .startAt(treintaDias) // 👉 Filtrado temporal
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        acciones.clear(); // 🔄 Limpiamos lista anterior

                        // Recorremos cada acción encontrada en Firebase
                        for (DataSnapshot s : snapshot.getChildren()) {

                            AccionLog log = s.getValue(AccionLog.class);
                            // Convertimos el JSON → objeto AccionLog

                            if (log != null) acciones.add(log); // Añadimos a la lista
                        }

                        // Si no hubo registros recientes
                        if (acciones.isEmpty()) {
                            Toast.makeText(HistorialAcciones.this,
                                    "No hay acciones registradas en los últimos 30 días",
                                    Toast.LENGTH_LONG).show();
                        }

                        // Notificamos al adaptador que la data ha cambiado
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Error silencioso (podríamos agregar Log.e si fuera necesario)
                    }
                });
    }
}
