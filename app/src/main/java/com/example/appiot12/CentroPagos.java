package com.example.appiot12;
// 📦 Centro de Pagos del usuario.
// Aquí se pagan cuotas y, si corresponde, se CREA el dispositivo 🤖💳

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
import java.util.UUID;

public class CentroPagos extends AppCompatActivity {

    // UI
    private TextView tvUsuarioPago, tvResumenDeuda;
    private ListView listPagos;
    private Button btnPagarCuota, btnPagarTotal;

    // Datos
    private final ArrayList<Pago> listaPagos = new ArrayList<>();
    private PagoAdapter adapter;
    private Pago pagoSeleccionado;

    // Firebase
    private DatabaseReference refUsuario;
    private DatabaseReference refPagos;
    private DatabaseReference refDispositivos;
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

        // Vistas
        tvUsuarioPago = findViewById(R.id.tvUsuarioPago);
        tvResumenDeuda = findViewById(R.id.tvResumenDeuda);
        listPagos = findViewById(R.id.listPagos);
        btnPagarCuota = findViewById(R.id.btnPagarCuota);
        btnPagarTotal = findViewById(R.id.btnPagarTotal);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvUsuarioPago.setText(
                "Usuario: " + FirebaseAuth.getInstance().getCurrentUser().getEmail()
        );

        refUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid);

        refPagos = refUsuario.child("pagos");
        refDispositivos = refUsuario.child("dispositivos");

        adapter = new PagoAdapter(this, listaPagos);
        listPagos.setAdapter(adapter);

        listPagos.setOnItemClickListener((parent, view, position, id) -> {
            pagoSeleccionado = listaPagos.get(position);
            Toast.makeText(this, "Pago seleccionado ✔️", Toast.LENGTH_SHORT).show();
        });

        cargarPagos();
        configurarBotones();
    }

    // =====================================================
    // 📥 CARGAR PAGOS
    // =====================================================
    private void cargarPagos() {

        refPagos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                listaPagos.clear();
                int deudaTotal = 0;

                for (DataSnapshot s : snapshot.getChildren()) {

                    Pago pago = s.getValue(Pago.class);
                    if (pago == null) continue;

                    listaPagos.add(pago);

                    if (!pago.isPagado()) {
                        deudaTotal += pago.getSaldoPendiente();
                    }
                }

                tvResumenDeuda.setText("Deuda total: $" + deudaTotal);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CentroPagos.this,
                        "Error al leer pagos ❌",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // =====================================================
    // ⚙️ BOTONES
    // =====================================================
    private void configurarBotones() {

        // ➗ PAGAR UNA CUOTA
        btnPagarCuota.setOnClickListener(v -> {

            if (!pagoValidoSeleccionado()) return;

            int valorCuota =
                    pagoSeleccionado.getPrecioTotal()
                            / pagoSeleccionado.getCuotasTotales();

            pagoSeleccionado.setCuotasPagadas(
                    pagoSeleccionado.getCuotasPagadas() + 1
            );

            pagoSeleccionado.setSaldoPendiente(
                    pagoSeleccionado.getSaldoPendiente() - valorCuota
            );

            finalizarPagoSiCorresponde();
        });

        // 💥 PAGAR TODO
        btnPagarTotal.setOnClickListener(v -> {

            if (!pagoValidoSeleccionado()) return;

            pagoSeleccionado.setCuotasPagadas(
                    pagoSeleccionado.getCuotasTotales()
            );

            pagoSeleccionado.setSaldoPendiente(0);

            finalizarPagoSiCorresponde();
        });
    }

    // =====================================================
    // ✅ VALIDACIONES
    // =====================================================
    private boolean pagoValidoSeleccionado() {

        if (pagoSeleccionado == null) {
            Toast.makeText(this,
                    "Seleccione un pago primero ☝️",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pagoSeleccionado.isPagado()) {
            Toast.makeText(this,
                    "Este pago ya está completado ✔️",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // =====================================================
    // 🧠 LÓGICA CLAVE: CREAR DISPOSITIVO SI SE PAGÓ
    // =====================================================
    private void finalizarPagoSiCorresponde() {

        // Si el pago quedó COMPLETADO y aún no tiene dispositivo
        if (pagoSeleccionado.isPagado()
                && pagoSeleccionado.getIdDispositivo() == null) {

            String idDispositivo = UUID.randomUUID().toString();

            Dispositivo dispositivo = new Dispositivo(
                    idDispositivo,
                    7.0,
                    500.0,
                    1.0,
                    1000.0
            );

            // Guardamos el dispositivo
            refDispositivos.child(idDispositivo).setValue(dispositivo);

            // Asociamos el dispositivo al pago
            pagoSeleccionado.setIdDispositivo(idDispositivo);

            Toast.makeText(this,
                    "Pago completado. Dispositivo creado 🤖✅",
                    Toast.LENGTH_LONG).show();
        }

        guardarPagoActualizado();
    }

    // =====================================================
    // 💾 GUARDAR PAGO
    // =====================================================
    private void guardarPagoActualizado() {

        refPagos.child(pagoSeleccionado.getIdPago())
                .setValue(pagoSeleccionado)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                "Pago actualizado ☁️",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error al guardar ❌",
                                Toast.LENGTH_LONG).show()
                );
    }
}
