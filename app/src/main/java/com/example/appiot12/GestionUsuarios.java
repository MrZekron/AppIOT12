package com.example.appiot12;
// 📦 Módulo de administración del proyecto Agua Segura.
// Aquí el ADMIN ve, ordena y analiza usuarios 👥📊

// ===== IMPORTS ANDROID =====
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

// ===== IMPORTS FIREBASE =====
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// ===== IMPORTS JAVA =====
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 👥 GESTIÓN DE USUARIOS (ADMIN)
 *
 * Explicado fácil 👶:
 * 👉 Esta pantalla es una lista de personas
 * 👉 El admin puede:
 *    - Ver todos los usuarios
 *    - Ordenarlos por nombre
 *    - Ver quién tiene más o menos tanques
 *
 * Arquitectura correcta 🧱:
 * ✔ Modelo Usuario LIMPIO
 * ✔ Datos extra (tanques) se calculan desde Firebase
 * ✔ Filtros SIN volver a llamar Firebase
 */
public class GestionUsuarios extends AppCompatActivity {

    // ============================
    // 📋 UI
    // ============================
    private ListView listUsuarios;          // Lista visual
    private Button btnFiltrarUsuarios;      // Botón de filtros

    // ============================
    // 🎨 ADAPTADOR
    // ============================
    private UsuarioAdapter adapter;

    // ============================
    // 🗂 LISTAS
    // ============================

    // Lista ORIGINAL (datos crudos desde Firebase)
    private final List<Usuario> usuariosOriginales = new ArrayList<>();

    // Lista FILTRADA (lo que se muestra en pantalla)
    private final List<Usuario> usuariosFiltrados = new ArrayList<>();

    // ============================
    // 🧮 DATOS DERIVADOS (NO viven en Usuario)
    // ============================

    // uid → cantidad de tanques
    private final Map<String, Integer> tanquesPorUsuario = new HashMap<>();

    // ============================
    // ☁️ FIREBASE
    // ============================
    private DatabaseReference refUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // 🔗 Conectamos XML con Java
        inicializarVistas();

        // ☁️ Referencia raíz: /usuarios
        refUsuarios = FirebaseDatabase.getInstance()
                .getReference("usuarios");

        // 🎨 Adaptador usa SOLO la lista filtrada
        adapter = new UsuarioAdapter(this, usuariosFiltrados);
        listUsuarios.setAdapter(adapter);

        // 📥 Cargar usuarios desde Firebase
        cargarUsuarios();

        // 🔽 Botón para mostrar filtros
        btnFiltrarUsuarios.setOnClickListener(this::mostrarMenuFiltros);
    }

    /**
     * 🔗 Conecta variables Java con XML
     */
    private void inicializarVistas() {
        listUsuarios = findViewById(R.id.listUsuarios);
        btnFiltrarUsuarios = findViewById(R.id.btnFiltrarUsuarios);
    }

    // =====================================================
    // 📥 CARGAR USUARIOS DESDE FIREBASE
    // =====================================================
    private void cargarUsuarios() {

        refUsuarios.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                usuariosOriginales.clear();
                usuariosFiltrados.clear();
                tanquesPorUsuario.clear();

                // 🔄 Recorremos TODOS los usuarios
                for (DataSnapshot snap : snapshot.getChildren()) {

                    Usuario usuario = snap.getValue(Usuario.class);
                    if (usuario == null) continue; // 🛑 Seguridad

                    // 🆔 Firebase no llena el ID automáticamente
                    usuario.setId(snap.getKey());

                    // ⭐ SOLO usuarios normales (no admin)
                    if ("usuario".equalsIgnoreCase(usuario.getRol())) {

                        usuariosOriginales.add(usuario);

                        // 🔢 Contamos tanques reales desde Firebase
                        calcularTanquesUsuario(usuario.getId());
                    }
                }

                // 📋 Por defecto mostramos TODO
                usuariosFiltrados.addAll(usuariosOriginales);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // (Opcional) Toast o Log
            }
        });
    }

    // =====================================================
    // 🧮 CONTAR TANQUES DE UN USUARIO (Firebase real)
    // =====================================================
    private void calcularTanquesUsuario(String uid) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        // Cantidad de tanques del usuario
                        tanquesPorUsuario.put(uid, (int) snapshot.getChildrenCount());

                        // Refrescamos UI si está visible
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Si falla, asumimos 0 tanques
                        tanquesPorUsuario.put(uid, 0);
                    }
                });
    }

    // =====================================================
    // 🔽 MENÚ DE FILTROS (Popup)
    // =====================================================
    private void mostrarMenuFiltros(View view) {

        PopupMenu menu = new PopupMenu(this, view);

        // Filtros FUNCIONALES
        menu.getMenu().add("Nombre A-Z");
        menu.getMenu().add("Nombre Z-A");
        menu.getMenu().add("Más tanques");
        menu.getMenu().add("Menos tanques");

        // Filtros FUTUROS (no rompen nada)
        menu.getMenu().add("Pago al día (próximo)");
        menu.getMenu().add("Dispositivos adquiridos (próximo)");
        menu.getMenu().add("Fecha de creación (próximo)");

        menu.setOnMenuItemClickListener(item -> {
            aplicarFiltro(item.getTitle().toString());
            return true;
        });

        menu.show();
    }

    // =====================================================
    // 🎯 APLICAR FILTRO SIN VOLVER A FIREBASE
    // =====================================================
    private void aplicarFiltro(String filtro) {

        usuariosFiltrados.clear();
        usuariosFiltrados.addAll(usuariosOriginales);

        switch (filtro) {

            case "Nombre A-Z":
                usuariosFiltrados.sort(
                        Comparator.comparing(u -> safe(u.getCorreo()).toLowerCase())
                );
                break;

            case "Nombre Z-A":
                usuariosFiltrados.sort((a, b) ->
                        safe(b.getCorreo()).compareToIgnoreCase(safe(a.getCorreo()))
                );
                break;

            case "Más tanques":
                usuariosFiltrados.sort((a, b) ->
                        tanquesPorUsuario.getOrDefault(b.getId(), 0)
                                - tanquesPorUsuario.getOrDefault(a.getId(), 0)
                );
                break;

            case "Menos tanques":
                usuariosFiltrados.sort((a, b) ->
                        tanquesPorUsuario.getOrDefault(a.getId(), 0)
                                - tanquesPorUsuario.getOrDefault(b.getId(), 0)
                );
                break;

            default:
                // Filtros futuros → no hacen nada aún
                break;
        }

        adapter.notifyDataSetChanged();
    }

    // =====================================================
    // 🧹 UTILIDAD PARA EVITAR NULL
    // =====================================================
    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =====================================================
    // 🔙 VOLVER AL MENÚ ADMIN
    // =====================================================
    public void volver(View view) {
        finish(); // 🚪 Cerramos pantalla
    }
}
