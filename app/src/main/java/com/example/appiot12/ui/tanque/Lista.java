package com.example.appiot12.ui.tanque;

// 📦 Pantalla principal donde se listan los tanques del usuario.

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.adapter.TanqueAdapter;
import com.example.appiot12.model.TanqueAgua;
import com.example.appiot12.ui.perfil.Informacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 🧠 Lista
 *
 * Responsabilidades:
 * - Mostrar todos los tanques del usuario
 * - Ordenarlos por prioridad de mantención
 * - Permitir abrir el detalle de cada tanque
 */
public class Lista extends AppCompatActivity {

    // ============================
    // 🖥️ ELEMENTOS DE LA UI
    // ============================
    private ListView listViewTanques;
    private TanqueAdapter tanqueAdapter;
    private final ArrayList<TanqueAgua> tanques = new ArrayList<>();

    // ============================
    // 🔐 FIREBASE
    // ============================
    private DatabaseReference tanquesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        inicializarUI();
        prepararFirebase();
        cargarTanques();
        configurarClickLista();
    }

    private void inicializarUI() {
        listViewTanques = findViewById(R.id.listaTanques);
        tanqueAdapter = new TanqueAdapter(this, tanques);
        listViewTanques.setAdapter(tanqueAdapter);
    }

    private void prepararFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        tanquesRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");
    }

    private void cargarTanques() {

        tanquesRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                tanques.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(Lista.this, "No tienes tanques registrados 💧", Toast.LENGTH_SHORT).show();
                    tanqueAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot snap : snapshot.getChildren()) {

                    TanqueAgua tanque = snap.getValue(TanqueAgua.class);

                    if (tanque == null) {
                        continue;
                    }

                    tanque.setIdTanque(snap.getKey());
                    tanques.add(tanque);
                }

                ordenarTanquesPorMantencion();
                tanqueAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Lista.this, "Error al cargar tanques ❌", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ordenarTanquesPorMantencion() {
        Collections.sort(tanques, new Comparator<TanqueAgua>() {
            @Override
            public int compare(TanqueAgua a, TanqueAgua b) {

                int comparacionPrioridad = Integer.compare(
                        b.getPrioridadMantencion(),
                        a.getPrioridadMantencion()
                );

                if (comparacionPrioridad != 0) {
                    return comparacionPrioridad;
                }

                String nombreA = a.getNombre() == null ? "" : a.getNombre().trim();
                String nombreB = b.getNombre() == null ? "" : b.getNombre().trim();

                return nombreA.compareToIgnoreCase(nombreB);
            }
        });
    }

    private void configurarClickLista() {

        listViewTanques.setOnItemClickListener((parent, view, position, id) -> {

            TanqueAgua tanque = tanques.get(position);

            if (tanque == null) {
                Toast.makeText(this, "Tanque inválido ❌", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, Informacion.class);

            intent.putExtra("tanqueId", tanque.getIdTanque());
            intent.putExtra("tanqueNombre", tanque.getNombre());
            intent.putExtra("tanqueCapacidad", tanque.getCapacidad());
            intent.putExtra("tanqueColor", tanque.getColor());
            intent.putExtra("tanqueDireccion", tanque.getDireccion());
            intent.putExtra("idDispositivo", tanque.getIdDispositivo());

            intent.putExtra("mantencionTanque", tanque.isMantencionTanque());
            intent.putExtra("mantencionDispositivo", tanque.isMantencionDispositivo());

            startActivity(intent);
        });
    }
}
