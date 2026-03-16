package com.example.appiot12;

// 📦 Pantalla principal donde se listan los tanques del usuario.
// Aquí veremos los tanques ordenados por prioridad de mantención.

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
 *
 * Regla de orden:
 * 1. Primero van los tanques con mayor prioridad de mantención
 * 2. Si dos tanques tienen la misma prioridad, se ordenan por nombre
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

        // Ajuste para que la UI no se monte sobre barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // Inicialización general
        inicializarUI();
        prepararFirebase();
        cargarTanques();
        configurarClickLista();
    }

    // =====================================================
    // 🔗 INICIALIZAR UI
    // =====================================================
    private void inicializarUI() {
        listViewTanques = findViewById(R.id.listaTanques);

        // El adapter se alimenta de la lista en memoria "tanques"
        tanqueAdapter = new TanqueAdapter(this, tanques);
        listViewTanques.setAdapter(tanqueAdapter);
    }

    // =====================================================
    // 🔐 PREPARAR FIREBASE
    // =====================================================
    private void prepararFirebase() {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Validación básica de sesión
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "Usuario no autenticado ❌",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Ruta a los tanques del usuario actual
        tanquesRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");
    }

    // =====================================================
    // 📥 CARGAR TANQUES DESDE FIREBASE
    // =====================================================
    private void cargarTanques() {

        tanquesRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // Limpia la lista para evitar duplicados
                tanques.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(Lista.this,
                            "No tienes tanques registrados 💧",
                            Toast.LENGTH_SHORT).show();
                    tanqueAdapter.notifyDataSetChanged();
                    return;
                }

                // Recorremos cada nodo de tanque en Firebase
                for (DataSnapshot snap : snapshot.getChildren()) {

                    TanqueAgua tanque = snap.getValue(TanqueAgua.class);

                    if (tanque == null) {
                        continue;
                    }

                    // Aseguramos el ID real desde la key de Firebase
                    tanque.setIdTanque(snap.getKey());

                    tanques.add(tanque);
                }

                // Ordenar los tanques antes de mostrar
                ordenarTanquesPorMantencion();

                // Refrescar interfaz
                tanqueAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Lista.this,
                        "Error al cargar tanques ❌",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // =====================================================
    // 🧠 ORDENAR TANQUES POR PRIORIDAD
    // =====================================================
    /**
     * Ordena la lista usando la prioridad definida en TanqueAgua.
     *
     * Prioridad más alta primero:
     * - tanque + dispositivo en mantención
     * - solo tanque en mantención
     * - solo dispositivo en mantención
     * - ninguno en mantención
     *
     * Si hay empate, se ordena por nombre alfabéticamente.
     */
    private void ordenarTanquesPorMantencion() {
        Collections.sort(tanques, new Comparator<TanqueAgua>() {
            @Override
            public int compare(TanqueAgua a, TanqueAgua b) {

                // 1. Comparar prioridad de mantención (descendente)
                int comparacionPrioridad = Integer.compare(
                        b.getPrioridadMantencion(),
                        a.getPrioridadMantencion()
                );

                if (comparacionPrioridad != 0) {
                    return comparacionPrioridad;
                }

                // 2. Si empatan, comparar por nombre
                String nombreA = a.getNombre() == null ? "" : a.getNombre().trim();
                String nombreB = b.getNombre() == null ? "" : b.getNombre().trim();

                return nombreA.compareToIgnoreCase(nombreB);
            }
        });
    }

    // =====================================================
    // 👆 CONFIGURAR CLICK EN CADA TANQUE
    // =====================================================
    private void configurarClickLista() {

        listViewTanques.setOnItemClickListener((parent, view, position, id) -> {

            TanqueAgua tanque = tanques.get(position);

            if (tanque == null) {
                Toast.makeText(this,
                        "Tanque inválido ❌",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Abrir pantalla de información del tanque
            Intent intent = new Intent(this, Informacion.class);

            // Enviar datos principales del tanque
            intent.putExtra("tanqueId", tanque.getIdTanque());
            intent.putExtra("tanqueNombre", tanque.getNombre());
            intent.putExtra("tanqueCapacidad", tanque.getCapacidad());
            intent.putExtra("tanqueColor", tanque.getColor());
            intent.putExtra("tanqueDireccion", tanque.getDireccion());
            intent.putExtra("idDispositivo", tanque.getIdDispositivo());

            // Enviar también estados de mantención
            intent.putExtra("mantencionTanque", tanque.isMantencionTanque());
            intent.putExtra("mantencionDispositivo", tanque.isMantencionDispositivo());

            startActivity(intent);
        });
    }
}