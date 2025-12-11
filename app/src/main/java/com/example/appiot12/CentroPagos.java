package com.example.appiot12;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class CentroPagos extends AppCompatActivity {

    private TextView tvUsuarioPago, tvResumenDeuda;
    private ListView listPagos;
    private Button btnPagarCuota, btnPagarTotal;

    private ArrayList<Pago> listaPagos = new ArrayList<>();
    private PagoAdapter adapter;

    private Pago pagoSeleccionado = null;

    private DatabaseReference refPagos;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_centro_pagos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // VINCULAR XML
        tvUsuarioPago = findViewById(R.id.tvUsuarioPago);
        tvResumenDeuda = findViewById(R.id.tvResumenDeuda);
        listPagos = findViewById(R.id.listPagos);
        btnPagarCuota = findViewById(R.id.btnPagarCuota);
        btnPagarTotal = findViewById(R.id.btnPagarTotal);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvUsuarioPago.setText("Usuario: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());

        refPagos = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos");

        adapter = new PagoAdapter(this, listaPagos);
        listPagos.setAdapter(adapter);

        cargarPagos();
        configurarBotones();
    }

    // ====================================================
    // 🔄 CARGAR PAGOS DESDE FIREBASE
    // ====================================================
    private void cargarPagos() {

        refPagos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                listaPagos.clear();
                int deudaTotal = 0;

                for (DataSnapshot s : snapshot.getChildren()) {
                    Pago pago = s.getValue(Pago.class);
                    if (pago != null) {
                        listaPagos.add(pago);

                        if (!pago.isPagado()) {
                            deudaTotal += pago.getSaldoPendiente();
                        }
                    }
                }

                tvResumenDeuda.setText("Deuda total: $" + deudaTotal);
                adapter.notifyDataSetChanged();

                if (listaPagos.isEmpty()) {
                    Toast.makeText(CentroPagos.this, "No tienes pagos registrados", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CentroPagos.this, "Error al leer pagos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Cuando el usuario toca un pago lo selecciona
        listPagos.setOnItemClickListener((parent, view, pos, id) -> {
            pagoSeleccionado = listaPagos.get(pos);
            Toast.makeText(this, "Pago seleccionado", Toast.LENGTH_SHORT).show();
        });
    }

    // ====================================================
    // ⚙ CONFIGURAR BOTONES DE PAGO
    // ====================================================
    private void configurarBotones() {

        // PAGAR UNA CUOTA
        btnPagarCuota.setOnClickListener(v -> {

            if (pagoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un pago primero", Toast.LENGTH_LONG).show();
                return;
            }

            if (pagoSeleccionado.isPagado()) {
                Toast.makeText(this, "Este pago ya está completado", Toast.LENGTH_SHORT).show();
                return;
            }

            // calcular valor de cuota
            int valorCuota = pagoSeleccionado.getPrecioTotal() / pagoSeleccionado.getCuotasTotales();

            pagoSeleccionado.setCuotasPagadas(
                    pagoSeleccionado.getCuotasPagadas() + 1
            );

            pagoSeleccionado.setSaldoPendiente(
                    Math.max(0, pagoSeleccionado.getSaldoPendiente() - valorCuota)
            );

            guardarPagoActualizado();

            Toast.makeText(this, "Cuota pagada correctamente", Toast.LENGTH_LONG).show();
        });

        // PAGAR TODO
        btnPagarTotal.setOnClickListener(v -> {

            if (pagoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un pago primero", Toast.LENGTH_LONG).show();
                return;
            }

            pagoSeleccionado.setCuotasPagadas(pagoSeleccionado.getCuotasTotales());
            pagoSeleccionado.setSaldoPendiente(0);
            pagoSeleccionado.setPagado(true);

            guardarPagoActualizado();

            Toast.makeText(this, "Pago completado", Toast.LENGTH_LONG).show();
        });
    }


    // ====================================================
    // 💾 GUARDAR ACTUALIZACIÓN DE PAGO EN FIREBASE
    // ====================================================
    private void guardarPagoActualizado() {

        refPagos.child(pagoSeleccionado.getIdPago())
                .setValue(pagoSeleccionado)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
