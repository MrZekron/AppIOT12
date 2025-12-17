package com.example.appiot12;
// 📦 Módulo de administración del proyecto Agua Segura.
// Esta pantalla permite a un ADMIN ver y gestionar a los usuarios 👥⚙️

// ===== IMPORTS ANDROID =====
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
// 📋 ListView: lista visual donde mostramos usuarios

import androidx.appcompat.app.AppCompatActivity;
// 🏛 Activity base moderna y estable

// ===== IMPORTS FIREBASE =====
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// ☁️ Firebase Realtime Database: fuente oficial de los usuarios

// ===== IMPORTS JAVA =====
import java.util.ArrayList;
import java.util.List;
// 🗂️ Listas dinámicas

/**
 * 👥 GestionUsuarios
 *
 * ¿Qué hace esta pantalla?
 * 👉 Muestra todos los usuarios registrados
 * 👉 Filtra SOLO usuarios normales (no admins)
 * 👉 Permite al administrador revisarlos
 *
 * Explicado para un niño:
 * 👉 Es como una lista de alumnos, pero solo vemos a los alumnos,
 *    no a los profesores 📋🙂
 */
public class GestionUsuarios extends AppCompatActivity {

    // 📋 Lista visual
    private ListView listUsuarios;

    // 🎨 Adaptador que dibuja cada usuario
    private UsuarioAdapter adapter;

    // 🗂️ Lista interna con usuarios normales
    private final List<Usuario> usuarios = new ArrayList<>();

    // ☁️ Referencia a Firebase
    private DatabaseReference refUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos UI con el XML
        inicializarVistas();

        // ☁️ Apuntamos al nodo raíz de usuarios
        refUsuarios = FirebaseDatabase.getInstance()
                .getReference("usuarios");

        // 🎨 Creamos el adaptador
        adapter = new UsuarioAdapter(this, usuarios);
        listUsuarios.setAdapter(adapter);

        // 📥 Cargamos usuarios desde Firebase
        cargarUsuarios();
    }

    /**
     * 🔗 Vincula los elementos visuales con el XML
     */
    private void inicializarVistas() {
        listUsuarios = findViewById(R.id.listUsuarios);
    }

    // =====================================================
    // 📥 CARGAR USUARIOS DESDE FIREBASE
    // =====================================================
    private void cargarUsuarios() {

        refUsuarios.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                usuarios.clear(); // ♻️ Limpiamos lista antes de recargar

                // 🔄 Recorremos todos los usuarios
                for (DataSnapshot snap : snapshot.getChildren()) {

                    Usuario usuario = snap.getValue(Usuario.class);

                    if (usuario == null) continue; // 🛑 Seguridad básica

                    // 🆔 Firebase no llena el ID automáticamente
                    usuario.setId(snap.getKey());

                    // ⭐ Mostramos SOLO usuarios normales
                    if ("usuario".equalsIgnoreCase(usuario.getRol())) {
                        usuarios.add(usuario);
                    }
                }

                // 🔄 Actualizamos la lista visual
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // ⚠️ Error al leer usuarios
                // Aquí podrías mostrar un Toast o log si lo deseas
            }
        });
    }

    // =====================================================
    // 🔙 VOLVER AL MENÚ ADMIN
    // =====================================================
    public void volver(View view) {
        finish(); // 🚪 Cerramos esta pantalla
    }
}
