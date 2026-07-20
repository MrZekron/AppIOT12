package com.example.appiot12.ui.tanque;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.appiot12.ui.BaseActivity;

import com.example.appiot12.R;
import com.example.appiot12.model.Dispositivo;
import com.example.appiot12.model.TanqueAgua;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AsociarDispositivoATanque extends BaseActivity {

    private Spinner spnTanques, spnDispositivos;
    private Button btnAsociar;

    private final List<TanqueAgua> tanquesDisponibles = new ArrayList<>();
    private final List<Dispositivo> dispositivosLibres = new ArrayList<>();

    private DatabaseReference refUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_asociar_dispositivo);

        spnTanques = findViewById(R.id.spnTanques);
        spnDispositivos = findViewById(R.id.spnDispositivos);
        btnAsociar = findViewById(R.id.btnAsociar);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            toast("Usuario no autenticado");
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        refUsuario = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        cargarTanques();
        cargarDispositivosLibres();

        btnAsociar.setOnClickListener(v -> asociar());
    }

    private void cargarTanques() {
        refUsuario.child("tanques").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tanquesDisponibles.clear();
                List<String> nombres = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    TanqueAgua t = s.getValue(TanqueAgua.class);
                    if (t == null) continue;
                    if (t.getIdTanque() == null) t.setIdTanque(s.getKey());
                    tanquesDisponibles.add(t);
                    nombres.add(t.getNombre());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AsociarDispositivoATanque.this,
                        android.R.layout.simple_spinner_item, nombres);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnTanques.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                toast("Error al cargar tanques");
            }
        });
    }

    private void cargarDispositivosLibres() {
        refUsuario.child("dispositivos").addListenerForSingleValueEvent(new ValueEventListener() {
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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AsociarDispositivoATanque.this,
                        android.R.layout.simple_spinner_item, textos);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnDispositivos.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                toast("Error al cargar dispositivos");
            }
        });
    }

    private void asociar() {
        TanqueAgua tanque = tanquesDisponibles.get(spnTanques.getSelectedItemPosition());
        Dispositivo dispositivo = dispositivosLibres.get(spnDispositivos.getSelectedItemPosition());

        refUsuario.child("tanques").child(tanque.getIdTanque()).child("idDispositivo").setValue(dispositivo.getId());
        refUsuario.child("dispositivos").child(dispositivo.getId()).child("idTanque").setValue(tanque.getIdTanque());

        toast("Dispositivo asociado correctamente");
        finish();
    }

    public void volverAlMenu(android.view.View view) {
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
