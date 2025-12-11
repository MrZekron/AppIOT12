package com.example.appiot12;
// 📦 Pantalla principal donde se listan los tanques del usuario.
// El "CRM del agua" 💧📊

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// ☁️ Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Lista extends AppCompatActivity {

    // UI
    private ListView listView;                   // 📋 Vista donde mostramos tanques
    private ArrayList<TanqueAgua> listaTanques;  // 🗂 Lista dinámica
    private TanqueAdapter adapter;               // 🎨 Adaptador personalizado

    // Firebase
    private FirebaseAuth mAuth;                  // 🔐 Usuario actual
    private DatabaseReference usuariosRef;       // 📍 Ruta a /usuarios/{uid}/tanques

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);                 // 📱 Pantalla moderna elegante
        setContentView(R.layout.activity_lista);

        // Ajustar pantalla según barras del sistema (notch, barra inferior)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // ============================
        // 🔗 Conectar elementos UI
        // ============================
        listView = findViewById(R.id.listaTanques);
        listaTanques = new ArrayList<>();

        adapter = new TanqueAdapter(this, listaTanques); // Adaptador visual
        listView.setAdapter(adapter);

        // ============================
        // 🔐 Obtener UID actual
        // ============================
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        // ============================
        // 🔗 Ruta a los tanques del usuario
        // ============================
        usuariosRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");

        // Cargar tanques desde Firebase
        cargarTanques();

        // ============================
        // 👆 Evento al tocar un tanque
        // ============================
        listView.setOnItemClickListener((parent, view, position, id) -> {

            TanqueAgua tanqueSeleccionado = listaTanques.get(position);

            if (tanqueSeleccionado == null) {
                Toast.makeText(Lista.this, "Error al seleccionar tanque", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear Intent para abrir la pantalla Informacion.java
            Intent intent = new Intent(Lista.this, Informacion.class);

            // Mandamos los datos relevantes del tanque
            intent.putExtra("tanqueId", tanqueSeleccionado.getIdTanque());
            intent.putExtra("tanqueNombre", tanqueSeleccionado.getNombre());
            intent.putExtra("tanqueCapacidad", tanqueSeleccionado.getCapacidad());
            intent.putExtra("tanqueColor", tanqueSeleccionado.getColor());
            intent.putExtra("idDispositivo", tanqueSeleccionado.getIdDispositivo());

            startActivity(intent); // Abrir información del tanque
        });
    }

    // ============================================================
    // 📥 Obtener los tanques desde Firebase
    // ============================================================
    private void cargarTanques() {

        // ⭐ Listener que trae la lista una sola vez (evita duplicar al volver atrás)
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                listaTanques.clear(); // 💡 Siempre limpiar antes de cargar

                if (snapshot.exists()) {

                    // 🔄 Iterar tanques del usuario
                    for (DataSnapshot tanqueSnap : snapshot.getChildren()) {

                        TanqueAgua tanque = tanqueSnap.getValue(TanqueAgua.class);

                        if (tanque != null) {

                            // Firebase no carga el ID → lo ponemos manualmente
                            if (tanque.getIdTanque() == null) {
                                tanque.setIdTanque(tanqueSnap.getKey());
                            }

                            listaTanques.add(tanque);
                        }
                    }

                    adapter.notifyDataSetChanged(); // 🔃 Actualizar lista en UI

                } else {
                    Toast.makeText(Lista.this,
                            "No hay tanques registrados",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

                Toast.makeText(Lista.this,
                        "Error al cargar tanques: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
