package com.example.appiot12;
// 📦 Pantalla reservada para administración: gestionar usuarios del sistema

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
// 🧰 Componentes de UI: contenedor de lista y botones

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// 🎨 Ajustes modernos de UI que hacen feliz al diseñador UX

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// ☁️ Firebase Realtime Database: donde residen todos los usuarios

import java.util.ArrayList;

public class GestionUsuarios extends AppCompatActivity {

    private ListView listUsuarios;              // 📋 Lista visual donde aparecerán los usuarios
    private UsuarioAdapter adapter;             // 🎨 Adaptador personalizado para mostrar cada item
    private ArrayList<Usuario> usuariosList;    // 🗂 Lista interna con datos de usuarios normales

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 📱 Pantalla completa elegante
        setContentView(R.layout.activity_gestion_usuarios); // 🎨 Dibujamos el layout

        // Ajuste automático del contenido para no chocar con la barra del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🎯 Vinculamos el ListView
        listUsuarios = findViewById(R.id.listUsuarios);

        // Preparamos nuestra lista dinámica
        usuariosList = new ArrayList<>();

        // Creamos el adaptador visual
        adapter = new UsuarioAdapter(this, usuariosList);
        listUsuarios.setAdapter(adapter);

        // 🚀 Cargar usuarios desde Firebase
        cargarUsuarios();
    }

    // ================================================================
    // 📥 DESCARGAR LISTA DE USUARIOS DESDE FIREBASE
    // ================================================================
    private void cargarUsuarios() {

        FirebaseDatabase.getInstance()
                .getReference("usuarios") // Carpeta principal donde viven todos los usuarios
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        usuariosList.clear(); // 🧹 Limpieza previa de la lista

                        // 🔄 Recorremos todos los usuarios del sistema
                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Usuario u = snap.getValue(Usuario.class);

                            if (u == null) continue; // Seguridad básica

                            // Firebase NO rellena el campo ID del usuario, así que lo agregamos manual:
                            u.setId(snap.getKey()); // 🆔 Autocompletado elegante

                            // ⭐ Solo mostramos usuarios NORMALES, NO administradores
                            if (u.getRol() != null &&
                                    u.getRol().equalsIgnoreCase("usuario")) {

                                usuariosList.add(u); // Agregamos a la lista visible
                            }
                        }

                        // Notificamos al adaptador que hubo cambios
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // ⚠️ Error silencioso: aquí podrías agregar logs si deseas
                    }
                });
    }

    // ================================================================
    // 🔙 BOTÓN VOLVER AL MENÚ ADMIN
    // ================================================================
    public void volver(View v) {
        finish(); // 🚪 Cierra esta pantalla y vuelve atrás
    }
}
