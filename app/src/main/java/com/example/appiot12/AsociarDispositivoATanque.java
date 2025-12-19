package com.example.appiot12;
// 🔗 Asocia un dispositivo existente a un tanque existente 💧🤖

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AsociarDispositivoATanque extends AppCompatActivity {

    private Spinner spnTanques;
    private Spinner spnDispositivos;
    private Button btnAsociar;

    private final List<TanqueAgua> tanquesDisponibles = new ArrayList<>();
    private final List<Dispositivo> dispositivosLibres = new ArrayList<>();

    private DatabaseReference refUsuario;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asociar_dispositivo_atanque);

        inicializarVistas();

        uid = obtenerUidUsuario();
        if (uid == null) {
            toast("Usuario no autenticado ❌");
            finish();
            return;
        }

        refUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid);

        cargarTanques();
        cargarDispositivosLibres();

        btnAsociar.setOnClickListener(v -> asociar());
    }

    private void inicializarVistas() {
        spnTanques = findViewById(R.id.spnTanques);
        spnDispositivos = findViewById(R.id.spnDispositivos);
        btnAsociar = findViewById(R.id.btnAsociar);
    }

    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // ==========================
    // 🛢️ TANQUES
    // ==========================
    private void cargarTanques() {

        refUsuario.child("tanques")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        tanquesDisponibles.clear();
                        List<String> nombres = new ArrayList<>();

                        for (DataSnapshot s : snapshot.getChildren()) {
                            TanqueAgua t = s.getValue(TanqueAgua.class);
                            if (t == null) continue;

                            if (t.getIdTanque() == null) {
                                t.setIdTanque(s.getKey());
                            }

                            tanquesDisponibles.add(t);
                            nombres.add(t.getNombre());
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(AsociarDispositivoATanque.this,
                                        android.R.layout.simple_spinner_item,
                                        nombres);

                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);

                        spnTanques.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        toast("Error al cargar tanques");
                    }
                });
    }

    // ==========================
    // 📡 DISPOSITIVOS LIBRES
    // ==========================
    private void cargarDispositivosLibres() {

        refUsuario.child("dispositivos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        dispositivosLibres.clear();
                        List<String> textos = new ArrayList<>();

                        for (DataSnapshot s : snapshot.getChildren()) {

                            Dispositivo d = s.getValue(Dispositivo.class);
                            if (d == null) continue;

                            if (d.getIdTanque() == null || d.getIdTanque().isEmpty()) {
                                dispositivosLibres.add(d);
                                textos.add("Dispositivo: " + d.getId());
                            }
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(AsociarDispositivoATanque.this,
                                        android.R.layout.simple_spinner_item,
                                        textos);

                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);

                        spnDispositivos.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        toast("Error al cargar dispositivos");
                    }
                });
    }

    // ==========================
    // 🤝 ASOCIAR
    // ==========================
    private void asociar() {

        TanqueAgua tanque =
                tanquesDisponibles.get(spnTanques.getSelectedItemPosition());

        Dispositivo dispositivo =
                dispositivosLibres.get(spnDispositivos.getSelectedItemPosition());

        // TANQUE → DISPOSITIVO
        refUsuario.child("tanques")
                .child(tanque.getIdTanque())
                .child("idDispositivo")
                .setValue(dispositivo.getId());

        // DISPOSITIVO → TANQUE
        refUsuario.child("dispositivos")
                .child(dispositivo.getId())
                .child("idTanque")
                .setValue(tanque.getIdTanque());

        toast("Dispositivo asociado correctamente 🤝");
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
