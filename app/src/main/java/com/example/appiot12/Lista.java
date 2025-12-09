package com.example.appiot12;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Lista extends AppCompatActivity {

    private ListView listView;
    private ArrayList<TanqueAgua> listaTanques;

    // ⭐ AHORA usamos TanqueAdapter (el que muestra sensores y colores)
    private TanqueAdapter adapter;

    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;

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

        listView = findViewById(R.id.listaTanques);
        listaTanques = new ArrayList<>();

        // ⭐ USAR EL ADAPTADOR PERSONALIZADO
        adapter = new TanqueAdapter(this, listaTanques);
        listView.setAdapter(adapter);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();

        usuariosRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");

        cargarTanques();

        listView.setOnItemClickListener((parent, view, position, id) -> {

            TanqueAgua tanqueSeleccionado = listaTanques.get(position);

            if (tanqueSeleccionado == null) {
                Toast.makeText(Lista.this, "Error al seleccionar tanque", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 Navegar al detalle del tanque
            Intent intent = new Intent(Lista.this, Informacion.class);
            intent.putExtra("tanqueId", tanqueSeleccionado.getIdTanque());
            intent.putExtra("tanqueNombre", tanqueSeleccionado.getNombre());
            intent.putExtra("tanqueCapacidad", tanqueSeleccionado.getCapacidad());
            intent.putExtra("tanqueColor", tanqueSeleccionado.getColor());
            intent.putExtra("idDispositivo", tanqueSeleccionado.getIdDispositivo());

            startActivity(intent);
        });
    }

    private void cargarTanques() {

        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                listaTanques.clear();

                if (snapshot.exists()) {

                    for (DataSnapshot tanqueSnap : snapshot.getChildren()) {
                        TanqueAgua tanque = tanqueSnap.getValue(TanqueAgua.class);

                        if (tanque != null) {

                            if (tanque.getIdTanque() == null) {
                                tanque.setIdTanque(tanqueSnap.getKey());
                            }

                            listaTanques.add(tanque);
                        }
                    }

                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(Lista.this, "No hay tanques registrados", Toast.LENGTH_SHORT).show();
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
