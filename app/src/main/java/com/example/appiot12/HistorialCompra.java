package com.example.appiot12;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistorialCompra extends AppCompatActivity {

    private ListView lvPagos;
    private ArrayList<Pago> pagosList = new ArrayList<>();
    private PagoAdapter pagoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_compra);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lvPagos = findViewById(R.id.lvPagos);
        pagoAdapter = new PagoAdapter(this, pagosList);
        lvPagos.setAdapter(pagoAdapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cargarPagosCorrectamente(uid);
    }

    private void cargarPagosCorrectamente(String uid) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        pagosList.clear();

                        if (!snapshot.exists()) {
                            Toast.makeText(HistorialCompra.this,
                                    "No tienes tanques registrados.",
                                    Toast.LENGTH_LONG).show();
                            pagoAdapter.notifyDataSetChanged();
                            return;
                        }

                        for (DataSnapshot tanqueSnap : snapshot.getChildren()) {

                            DataSnapshot dispSnap = tanqueSnap.child("dispositivo");

                            if (dispSnap.exists() && dispSnap.child("pago").exists()) {

                                Pago pago = dispSnap.child("pago").getValue(Pago.class);

                                if (pago != null) {
                                    pagosList.add(pago);
                                }
                            }
                        }

                        if (pagosList.isEmpty()) {
                            Toast.makeText(HistorialCompra.this,
                                    "No tienes pagos registrados.",
                                    Toast.LENGTH_LONG).show();
                        }

                        pagoAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(HistorialCompra.this,
                                "Error al leer pagos: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
