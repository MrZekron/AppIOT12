package com.example.appiot12.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.service.HistorialService;
import com.example.appiot12.ui.auth.IniciarSesion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.cliente_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        HistorialService.limpiarHistorialAntiguo();
        inicializarConfiguracionFirebase();
    }

    private void inicializarConfiguracionFirebase() {
        DatabaseReference configRef = FirebaseDatabase.getInstance().getReference("configuracion");
        configRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Si no existe o solo tiene el valor "true" temporal, cargar defaults
                if (!snapshot.exists() || !snapshot.hasChild("precioDispositivo")) {
                    Map<String, Object> config = new HashMap<>();
                    config.put("precioDispositivo", 125000);

                    Map<String, Object> ph = new HashMap<>();
                    ph.put("minNormal", 6.5);
                    ph.put("maxNormal", 8.5);
                    ph.put("minAlerta", 6.0);
                    ph.put("maxAlerta", 9.0);

                    Map<String, Object> conductividad = new HashMap<>();
                    conductividad.put("maxNormal", 1500);
                    conductividad.put("maxAlerta", 2500);

                    Map<String, Object> turbidez = new HashMap<>();
                    turbidez.put("maxNormal", 5);
                    turbidez.put("maxAlerta", 10);

                    Map<String, Object> nivel = new HashMap<>();
                    nivel.put("minNormal", 60);
                    nivel.put("minAlerta", 30);

                    Map<String, Object> umbrales = new HashMap<>();
                    umbrales.put("ph", ph);
                    umbrales.put("conductividad", conductividad);
                    umbrales.put("turbidez", turbidez);
                    umbrales.put("nivel", nivel);

                    config.put("umbrales", umbrales);
                    configRef.setValue(config);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    public void iniciar(View view) {
        startActivity(new Intent(this, IniciarSesion.class));
    }
}
