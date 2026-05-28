package com.example.appiot12.ui.pago;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.example.appiot12.adapter.PagoAdapter;
import com.example.appiot12.model.Pago;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistorialCompra extends AppCompatActivity {

    private ListView lvPagos;
    private final List<Pago> pagos = new ArrayList<>();
    private PagoAdapter pagoAdapter;
    private DatabaseReference refPagos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_historial_compra);

        lvPagos = findViewById(R.id.lvPagos);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        refPagos = FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("pagos");

        pagoAdapter = new PagoAdapter(this, pagos);
        lvPagos.setAdapter(pagoAdapter);

        cargarPagos();
    }

    private void cargarPagos() {
        refPagos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pagos.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(HistorialCompra.this, "No tienes pagos registrados", Toast.LENGTH_LONG).show();
                    pagoAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot pagoSnap : snapshot.getChildren()) {
                    Pago pago = pagoSnap.getValue(Pago.class);
                    if (pago != null) pagos.add(pago);
                }

                if (pagos.isEmpty()) {
                    Toast.makeText(HistorialCompra.this, "No tienes pagos registrados", Toast.LENGTH_LONG).show();
                }

                pagoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistorialCompra.this, "Error al leer pagos", Toast.LENGTH_LONG).show();
            }
        });
    }
}
