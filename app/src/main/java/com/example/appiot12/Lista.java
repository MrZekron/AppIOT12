package com.example.appiot12;
// 📦 Pantalla principal donde se listan los tanques del usuario.
// Piensa en esto como una **agenda** donde vemos todos nuestros tanques 💧📒

import android.content.Intent;      // 🚪 Abrir otra pantalla
import android.os.Bundle;           // 🎒 Datos al iniciar la pantalla
import android.widget.ListView;     // 📋 Lista visual
import android.widget.Toast;        // 🍞 Mensajes rápidos

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// ☁️ Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

/**
 * 🧠 Lista
 *
 * ¿Qué hace esta pantalla?
 * 👉 Muestra todos los tanques del usuario
 * 👉 Permite tocar uno para ver su información
 *
 * Explicado para un niño 👶:
 * 👉 Es como una lista de mochilas 🎒
 * 👉 Tocás una mochila y ves qué tiene adentro 😄
 */
public class Lista extends AppCompatActivity {

    // ============================
    // 🖥️ ELEMENTOS DE LA UI
    // ============================
    private ListView listViewTanques;               // 📋 Lista donde aparecen los tanques
    private TanqueAdapter tanqueAdapter;            // 🎨 Dibuja cada tanque bonito
    private final ArrayList<TanqueAgua> tanques = new ArrayList<>();
    // 🗂 Lista en memoria (no se repite, no se duplica)

    // ============================
    // 🔐 FIREBASE
    // ============================
    private DatabaseReference tanquesRef;            // 📍 Ruta a /usuarios/{uid}/tanques

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);                     // 📱 Pantalla completa moderna
        setContentView(R.layout.activity_lista);

        // Ajustar pantalla para no chocar con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // 🔗 Conectar UI
        inicializarUI();

        // 🔐 Preparar Firebase
        prepararFirebase();

        // 📥 Cargar tanques una sola vez
        cargarTanques();

        // 👆 Acción al tocar un tanque
        configurarClickLista();
    }

    // =====================================================
    // 🔗 INICIALIZAR UI
    // =====================================================
    private void inicializarUI() {
        listViewTanques = findViewById(R.id.listaTanques);

        tanqueAdapter = new TanqueAdapter(this, tanques);
        listViewTanques.setAdapter(tanqueAdapter);
    }

    // =====================================================
    // 🔐 PREPARAR FIREBASE
    // =====================================================
    private void prepararFirebase() {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            // 🚨 No debería pasar, pero es buena práctica
            Toast.makeText(this,
                    "Usuario no autenticado ❌",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // 📍 Ruta directa a los tanques del usuario
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

                tanques.clear(); // 🧹 Limpieza antes de cargar (NO duplicados)

                if (!snapshot.exists()) {
                    Toast.makeText(Lista.this,
                            "No tienes tanques registrados 💧",
                            Toast.LENGTH_SHORT).show();
                    tanqueAdapter.notifyDataSetChanged();
                    return;
                }

                // 🔄 Recorremos cada tanque
                for (DataSnapshot snap : snapshot.getChildren()) {

                    TanqueAgua tanque = snap.getValue(TanqueAgua.class);

                    if (tanque == null) continue;

                    // 🆔 Firebase no guarda el ID dentro del objeto → lo seteamos
                    tanque.setIdTanque(snap.getKey());

                    tanques.add(tanque); // ➕ Agregar a la lista
                }

                tanqueAdapter.notifyDataSetChanged(); // 🔄 Refrescar UI
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

            // 🚀 Abrimos pantalla de información
            Intent intent = new Intent(this, Informacion.class);

            // 📦 Enviamos los datos necesarios
            intent.putExtra("tanqueId", tanque.getIdTanque());
            intent.putExtra("tanqueNombre", tanque.getNombre());
            intent.putExtra("tanqueCapacidad", tanque.getCapacidad());
            intent.putExtra("tanqueColor", tanque.getColor());
            intent.putExtra("idDispositivo", tanque.getIdDispositivo());

            startActivity(intent);
        });
    }
}
