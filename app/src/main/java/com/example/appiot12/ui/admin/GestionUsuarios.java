package com.example.appiot12.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.example.appiot12.ui.BaseActivity;
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

public class GestionUsuarios extends BaseActivity {

    private ListView listUsuarios;
    private Button btnFiltrarUsuarios;
    private Button btnAsignarDispositivo;

    private UsuarioAdapter adapter;

    private final List<Usuario> usuariosOriginales = new ArrayList<>();
    private final List<Usuario> usuariosFiltrados = new ArrayList<>();

    private final Map<String, Integer> tanquesPorUsuario = new HashMap<>();
    private final Map<String, Integer> dispositivosPorUsuario = new HashMap<>();
    private final Map<String, Integer> deudaPorUsuario = new HashMap<>();

    private DatabaseReference refUsuarios;
    private DatabaseReference refCompras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_gestion_usuarios);

        listUsuarios = findViewById(R.id.listUsuarios);
        btnFiltrarUsuarios = findViewById(R.id.btnFiltrarUsuarios);
        btnAsignarDispositivo = findViewById(R.id.btnAsignarDispositivo);
        btnAsignarDispositivo.setOnClickListener(v -> startActivity(new Intent(this, AsignarDispositivoAdmin.class)));

        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        refCompras = FirebaseDatabase.getInstance().getReference("compras");

        adapter = new UsuarioAdapter(this, usuariosFiltrados);
        adapter.setOnBlockListener(this::confirmarBloqueo);
        listUsuarios.setAdapter(adapter);

        cargarUsuarios();
        cargarCompras();

        btnFiltrarUsuarios.setOnClickListener(this::mostrarMenuFiltros);
    }

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

                    if (!"cancelado".equalsIgnoreCase(estado)) {
                        dispositivosPorUsuario.put(uid, dispositivosPorUsuario.getOrDefault(uid, 0) + 1);
                    }

                    if (monto != null && esDeudaPendiente(estado)) {
                        deudaPorUsuario.put(uid, deudaPorUsuario.getOrDefault(uid, 0) + monto);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private boolean esDeudaPendiente(String estado) {
        if (estado == null) return false;
        return estado.equalsIgnoreCase("pendiente_pago") || estado.equalsIgnoreCase("comprobante_enviado");
    }

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

    private void aplicarFiltro(String filtro) {
        usuariosFiltrados.clear();
        usuariosFiltrados.addAll(usuariosOriginales);

        switch (filtro) {
            case "Nombre A-Z":
                usuariosFiltrados.sort(Comparator.comparing(u -> safe(u.getEmail()).toLowerCase()));
                break;
            case "Nombre Z-A":
                usuariosFiltrados.sort((a, b) -> safe(b.getEmail()).compareToIgnoreCase(safe(a.getEmail())));
                break;
            case "Más tanques":
                usuariosFiltrados.sort((a, b) -> tanquesPorUsuario.getOrDefault(b.getId(), 0) - tanquesPorUsuario.getOrDefault(a.getId(), 0));
                break;
            case "Menos tanques":
                usuariosFiltrados.sort((a, b) -> tanquesPorUsuario.getOrDefault(a.getId(), 0) - tanquesPorUsuario.getOrDefault(b.getId(), 0));
                break;
            case "Más dispositivos":
                usuariosFiltrados.sort((a, b) -> dispositivosPorUsuario.getOrDefault(b.getId(), 0) - dispositivosPorUsuario.getOrDefault(a.getId(), 0));
                break;
            case "Menos dispositivos":
                usuariosFiltrados.sort((a, b) -> dispositivosPorUsuario.getOrDefault(a.getId(), 0) - dispositivosPorUsuario.getOrDefault(b.getId(), 0));
                break;
            case "Mayor deuda":
                usuariosFiltrados.sort((a, b) -> deudaPorUsuario.getOrDefault(b.getId(), 0) - deudaPorUsuario.getOrDefault(a.getId(), 0));
                break;
            case "Menor deuda":
                usuariosFiltrados.sort((a, b) -> deudaPorUsuario.getOrDefault(a.getId(), 0) - deudaPorUsuario.getOrDefault(b.getId(), 0));
                break;
        }

        adapter.notifyDataSetChanged();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public int getTanquesUsuario(String uid) { return tanquesPorUsuario.getOrDefault(uid, 0); }
    public int getDispositivosUsuario(String uid) { return dispositivosPorUsuario.getOrDefault(uid, 0); }
    public int getDeudaUsuario(String uid) { return deudaPorUsuario.getOrDefault(uid, 0); }

    private void confirmarBloqueo(com.example.appiot12.model.Usuario usuario, boolean nuevoEstado) {
        String nombre = usuario.getNombre() != null ? usuario.getNombre() : usuario.getEmail();
        String accion = nuevoEstado ? "desbloquear" : "bloquear";
        String msg = "¿Desea " + accion + " la cuenta de " + nombre + "?";

        new AlertDialog.Builder(this)
                .setTitle(nuevoEstado ? "Desbloquear usuario" : "Bloquear usuario")
                .setMessage(msg)
                .setPositiveButton("Confirmar", (d, w) -> ejecutarBloqueo(usuario, nuevoEstado))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarBloqueo(com.example.appiot12.model.Usuario usuario, boolean nuevoEstado) {
        if (usuario.getId() == null) {
            Toast.makeText(this, "Error: ID de usuario no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        refUsuarios.child(usuario.getId()).child("activo")
                .setValue(nuevoEstado)
                .addOnSuccessListener(v -> {
                    String msg = nuevoEstado ? "Usuario desbloqueado" : "Usuario bloqueado";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Error al cambiar estado: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    public void volver(View view) {
        finish();
    }
}
