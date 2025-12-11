package com.example.appiot12;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GestionUsuarios extends AppCompatActivity {

    private ListView listUsuarios;
    private UsuarioAdapter adapter;
    private ArrayList<Usuario> usuariosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestion_usuarios);

        // Ajuste del borde superior/inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listUsuarios = findViewById(R.id.listUsuarios);
        usuariosList = new ArrayList<>();

        adapter = new UsuarioAdapter(this, usuariosList);
        listUsuarios.setAdapter(adapter);

        cargarUsuarios();
    }

    private void cargarUsuarios() {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        usuariosList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Usuario u = snap.getValue(Usuario.class);

                            if (u == null) continue;

                            // Asignar UID manualmente (Firebase no lo asigna)
                            u.setId(snap.getKey());

                            // ⭐ MOSTRAR SOLO USUARIOS NORMALES (NO ADMIN)
                            if (u.getRol() != null &&
                                    u.getRol().equalsIgnoreCase("usuario")) {

                                usuariosList.add(u);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }
    public void volver(View v) {
        finish();
    }

}
