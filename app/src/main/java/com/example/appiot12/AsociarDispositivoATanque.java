package com.example.appiot12;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

public class AsociarDispositivoATanque extends AppCompatActivity {

    private Spinner spnTanques, spnDispositivos;
    private Button btnAsociar;

    private ArrayList<TanqueAgua> tanquesDisponibles = new ArrayList<>();
    private ArrayList<Dispositivo> dispositivosLibres = new ArrayList<>();

    private ArrayAdapter<String> adapterTanques;
    private ArrayAdapter<String> adapterDispositivos;

    private String uid;
    private DatabaseReference refUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_asociar_dispositivo_atanque);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        refUser = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        spnTanques = findViewById(R.id.spnTanques);
        spnDispositivos = findViewById(R.id.spnDispositivos);
        btnAsociar = findViewById(R.id.btnAsociar);

        cargarTanques();
        cargarDispositivos();

        btnAsociar.setOnClickListener(v -> asociar());
    }

    // ============================
    //  CARGAR TANQUES
    // ============================
    private void cargarTanques() {
        refUser.child("tanques").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tanquesDisponibles.clear();
                ArrayList<String> nombres = new ArrayList<>();

                for (DataSnapshot s : snapshot.getChildren()) {
                    TanqueAgua t = s.getValue(TanqueAgua.class);

                    if (t != null) {
                        if (t.getIdTanque() == null) t.setIdTanque(s.getKey());

                        tanquesDisponibles.add(t);
                        nombres.add(t.getNombre());
                    }
                }

                if (nombres.isEmpty()) nombres.add("No hay tanques disponibles");

                adapterTanques = new ArrayAdapter<>(AsociarDispositivoATanque.this,
                        android.R.layout.simple_spinner_item, nombres);

                adapterTanques.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnTanques.setAdapter(adapterTanques);
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    // ============================
    //  CARGAR DISPOSITIVOS
    // ============================
    private void cargarDispositivos() {
        refUser.child("dispositivos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                dispositivosLibres.clear();
                ArrayList<String> ids = new ArrayList<>();

                for (DataSnapshot s : snapshot.getChildren()) {

                    Dispositivo d = s.getValue(Dispositivo.class);
                    String idTanque = s.child("idTanque").getValue(String.class);

                    if (d != null && (idTanque == null || idTanque.isEmpty())) {
                        dispositivosLibres.add(d);
                        ids.add("ID: " + d.getId());
                    }
                }

                if (ids.isEmpty()) ids.add("No hay dispositivos disponibles");

                adapterDispositivos = new ArrayAdapter<>(AsociarDispositivoATanque.this,
                        android.R.layout.simple_spinner_item, ids);

                adapterDispositivos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnDispositivos.setAdapter(adapterDispositivos);
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    // ============================
    //  ASOCIAR DISPOSITIVO A TANQUE
    // ============================
    private void asociar() {

        if (tanquesDisponibles.isEmpty() || dispositivosLibres.isEmpty()) {
            Toast.makeText(this, "No hay tanques o dispositivos disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        int posTanque = spnTanques.getSelectedItemPosition();
        int posDisp = spnDispositivos.getSelectedItemPosition();

        TanqueAgua tanque = tanquesDisponibles.get(posTanque);
        Dispositivo dispositivo = dispositivosLibres.get(posDisp);

        // tanque → guarda idDispositivo
        refUser.child("tanques")
                .child(tanque.getIdTanque())
                .child("idDispositivo")
                .setValue(dispositivo.getId());

        // dispositivo → guarda idTanque
        refUser.child("dispositivos")
                .child(dispositivo.getId())
                .child("idTanque")
                .setValue(tanque.getIdTanque());

        Toast.makeText(this, "Dispositivo asociado correctamente", Toast.LENGTH_LONG).show();

        finish();
    }
}
