package com.example.appiot12;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistorialGlobal extends AppCompatActivity {

    // 🔢 Etiquetas donde mostraremos los números importantes del sistema
    private TextView txtUsuariosTotal, txtTanquesTotal, txtDispositivosTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_global);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 📌 Conectamos los TextView del XML
        txtUsuariosTotal = findViewById(R.id.txtUsuariosTotal);
        txtTanquesTotal = findViewById(R.id.txtTanquesTotal);
        txtDispositivosTotal = findViewById(R.id.txtDispositivosTotal);

        // 🚀 Llamamos al método que lee Firebase
        cargarHistorialGlobal();
    }

    private void cargarHistorialGlobal() {

        DatabaseReference refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                int totalUsuarios = 0;
                int totalTanques = 0;
                int totalDispositivos = 0;

                // 👑 Recorremos todos los usuarios del sistema
                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {

                    totalUsuarios++; // ✨ Contamos al usuario

                    // Verificamos si tiene tanques
                    if (usuarioSnap.child("tanques").exists()) {

                        // Recorremos sus tanques
                        for (DataSnapshot tanqueSnap : usuarioSnap.child("tanques").getChildren()) {

                            totalTanques++; // 🏺 Sumamos el tanque

                            // Cada tanque tiene EXACTAMENTE un dispositivo
                            if (tanqueSnap.child("dispositivo").exists()) {
                                totalDispositivos++; // 🔌 Sumamos el dispositivo
                            }
                        }
                    }
                }

                // 📊 Mostramos los resultados
                txtUsuariosTotal.setText("Usuarios totales: " + totalUsuarios);
                txtTanquesTotal.setText("Tanques totales: " + totalTanques);
                txtDispositivosTotal.setText("Dispositivos totales: " + totalDispositivos);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistorialGlobal.this,
                        "Error al leer historial: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
