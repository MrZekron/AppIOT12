package com.example.appiot12.ui.admin;

// Pantalla de administración de usuarios.
// El admin puede ver usuarios, ordenarlos y calcular datos de compras.

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.appiot12.R;
import com.example.appiot12.adapter.UsuarioAdapter;
import com.example.appiot12.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionUsuarios extends AppCompatActivity {

    // Vistas.
    private ListView listUsuarios;
    private Button btnFiltrarUsuarios;

    // Adaptador.
    private UsuarioAdapter adapter;

    // Listas base y visual.
    private final List<Usuario> usuariosOriginales = new ArrayList<>();
    private final List<Usuario> usuariosFiltrados = new ArrayList<>();

    // Datos calculados por usuario.
    private final Map<String, Integer> tanquesPorUsuario = new HashMap<>();
    private final Map<String, Integer> dispositivosPorUsuario = new HashMap<>();
    private final Map<String, Integer> deudaPorUsuario = new HashMap<>();

    // Firebase.
    private DatabaseReference refUsuarios;
    private DatabaseReference refCompras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // Vincula XML.
        listUsuarios = findViewById(R.id.listUsuarios);
        btnFiltrarUsuarios = findViewById(R.id.btnFiltrarUsuarios);

        // Referencias Firebase.
        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        refCompras = FirebaseDatabase.getInstance().getReference("compras");

        // Adaptador.
        adapter = new UsuarioAdapter(this, usuariosFiltrados);
        listUsuarios.setAdapter(adapter);

        // Carga datos.
        cargarUsuarios();
        cargarCompras();

        // Filtros.
        btnFiltrarUsuarios.setOnClickListener(this::mostrarMenuFiltros);
    }

    // Carga usuarios desde Firebase.
    private void cargarUsuarios() {
        refUsuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usuariosOriginales.clear();
                usuariosFiltrados.clear();
                tanquesPorUsuario.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Usuario u = snap.getValue(Usuario.class);
                    if (u == null) continue;

                    u.setId(snap.getKey());

                    if ("usuario".equalsIgnoreCase(u.getRol())) {
                        usuariosOriginales.add(u);
                        calcularTanquesUsuario(u.getId());
                    }
                }

                usuariosFiltrados.addAll(usuariosOriginales);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // Cuenta tanques por usuario.
    private void calcularTanquesUsuario(String uid) {
        refUsuarios.child(uid).child("tanques")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        tanquesPorUsuario.put(uid, (int) snapshot.getChildrenCount());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        tanquesPorUsuario.put(uid, 0);
                    }
                });
    }

    // Carga compras y calcula dispositivos y deuda por usuario.
    private void cargarCompras() {
        refCompras.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                dispositivosPorUsuario.clear();
                deudaPorUsuario.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String uid = snap.child("uidUsuario").getValue(String.class);
                    Integer monto = snap.child("monto").getValue(Integer.class);
                    String estado = snap.child("estado").getValue(String.class);

                    if (uid == null) continue;

                    // Cuenta dispositivos comprados, excepto cancelados.
                    if (!"cancelado".equalsIgnoreCase(estado)) {
                        int total = dispositivosPorUsuario.getOrDefault(uid, 0);
                        dispositivosPorUsuario.put(uid, total + 1);
                    }

                    // Suma deuda solo si la compra aún no está pagada.
                    if (monto != null && esDeudaPendiente(estado)) {
                        int deuda = deudaPorUsuario.getOrDefault(uid, 0);
                        deudaPorUsuario.put(uid, deuda + monto);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // Define si una compra sigue contando como deuda.
    private boolean esDeudaPendiente(String estado) {
        if (estado == null) return false;

        return estado.equalsIgnoreCase("pendiente_pago")
                || estado.equalsIgnoreCase("comprobante_enviado");
    }

    // Muestra menú de filtros.
    private void mostrarMenuFiltros(View view) {
        PopupMenu menu = new PopupMenu(this, view);

        menu.getMenu().add("Nombre A-Z");
        menu.getMenu().add("Nombre Z-A");
        menu.getMenu().add("Más tanques");
        menu.getMenu().add("Menos tanques");
        menu.getMenu().add("Más dispositivos");
        menu.getMenu().add("Menos dispositivos");
        menu.getMenu().add("Mayor deuda");
        menu.getMenu().add("Menor deuda");

        menu.setOnMenuItemClickListener(item -> {
            aplicarFiltro(item.getTitle().toString());
            return true;
        });

        menu.show();
    }

    // Aplica filtro a la lista visible.
    private void aplicarFiltro(String filtro) {
        usuariosFiltrados.clear();
        usuariosFiltrados.addAll(usuariosOriginales);

        switch (filtro) {
            case "Nombre A-Z":
                usuariosFiltrados.sort(Comparator.comparing(u -> safe(u.getCorreo()).toLowerCase()));
                break;

            case "Nombre Z-A":
                usuariosFiltrados.sort((a, b) ->
                        safe(b.getCorreo()).compareToIgnoreCase(safe(a.getCorreo())));
                break;

            case "Más tanques":
                usuariosFiltrados.sort((a, b) ->
                        tanquesPorUsuario.getOrDefault(b.getId(), 0)
                                - tanquesPorUsuario.getOrDefault(a.getId(), 0));
                break;

            case "Menos tanques":
                usuariosFiltrados.sort((a, b) ->
                        tanquesPorUsuario.getOrDefault(a.getId(), 0)
                                - tanquesPorUsuario.getOrDefault(b.getId(), 0));
                break;

            case "Más dispositivos":
                usuariosFiltrados.sort((a, b) ->
                        dispositivosPorUsuario.getOrDefault(b.getId(), 0)
                                - dispositivosPorUsuario.getOrDefault(a.getId(), 0));
                break;

            case "Menos dispositivos":
                usuariosFiltrados.sort((a, b) ->
                        dispositivosPorUsuario.getOrDefault(a.getId(), 0)
                                - dispositivosPorUsuario.getOrDefault(b.getId(), 0));
                break;

            case "Mayor deuda":
                usuariosFiltrados.sort((a, b) ->
                        deudaPorUsuario.getOrDefault(b.getId(), 0)
                                - deudaPorUsuario.getOrDefault(a.getId(), 0));
                break;

            case "Menor deuda":
                usuariosFiltrados.sort((a, b) ->
                        deudaPorUsuario.getOrDefault(a.getId(), 0)
                                - deudaPorUsuario.getOrDefault(b.getId(), 0));
                break;
        }

        adapter.notifyDataSetChanged();
    }

    // Evita null.
    private String safe(String s) {
        return s == null ? "" : s;
    }

    // Métodos públicos para que el adapter pueda mostrar datos calculados.
    public int getTanquesUsuario(String uid) {
        return tanquesPorUsuario.getOrDefault(uid, 0);
    }

    public int getDispositivosUsuario(String uid) {
        return dispositivosPorUsuario.getOrDefault(uid, 0);
    }

    public int getDeudaUsuario(String uid) {
        return deudaPorUsuario.getOrDefault(uid, 0);
    }

    // Cierra pantalla.
    public void volver(View view) {
        finish();
    }
}
