package com.example.appiot12;
// 📦 Centro de Pagos del usuario.
// Aquí el usuario puede ver sus pagos y pagar cuotas o el total 💳💸

import android.os.Bundle;
import android.widget.ArrayAdapter;
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

    // =========================
    // 📺 ELEMENTOS DE LA PANTALLA
    // =========================
    private TextView tvUsuarioPago, tvResumenDeuda;
    private ListView listPagos;
    private Button btnPagarCuota, btnPagarTotal;

    // =========================
    // 💾 DATOS EN MEMORIA
    // =========================
    private final ArrayList<Pago> listaPagos = new ArrayList<>();
    private PagoAdapter adapter;
    private Pago pagoSeleccionado; // El pago que el usuario toca 👆

    // =========================
    // ☁️ FIREBASE
    // =========================
    private DatabaseReference refPagos;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_centro_pagos);

        // Ajustar márgenes para que no choque con la barra del celular 📱
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // =========================
        // 🔗 VINCULAR XML → JAVA
        // =========================
        tvUsuarioPago = findViewById(R.id.tvUsuarioPago);
        tvResumenDeuda = findViewById(R.id.tvResumenDeuda);
        listPagos = findViewById(R.id.listPagos);
        btnPagarCuota = findViewById(R.id.btnPagarCuota);
        btnPagarTotal = findViewById(R.id.btnPagarTotal);

        // =========================
        // 🔐 USUARIO ACTUAL
        // =========================
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tvUsuarioPago.setText(
                "Usuario: " + FirebaseAuth.getInstance().getCurrentUser().getEmail()
        );

        // Ruta correcta a los pagos del usuario
        refPagos = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos");

        // =========================
        // 📋 ADAPTADOR DE PAGOS
        // =========================
        adapter = new PagoAdapter(this, listaPagos);
        listPagos.setAdapter(adapter);

        // Cuando el usuario toca un pago 👇
        listPagos.setOnItemClickListener((parent, view, position, id) -> {
            pagoSeleccionado = listaPagos.get(position);
            Toast.makeText(this, "Pago seleccionado ✔️", Toast.LENGTH_SHORT).show();
        });

        cargarPagos();
        configurarBotones();
    }

    // =========================================================
    // 📥 CARGAR PAGOS DESDE FIREBASE
    // =========================================================
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

                    // Solo sumamos deuda si NO está pagado 💰
                    if (!pago.isPagado()) {
                        deudaTotal += pago.getSaldoPendiente();
                    }
                }

                tvResumenDeuda.setText("Deuda total: $" + deudaTotal);
                adapter.notifyDataSetChanged();

                if (listaPagos.isEmpty()) {
                    Toast.makeText(
                            CentroPagos.this,
                            "No tienes pagos registrados 📭",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        CentroPagos.this,
                        "Error al leer pagos ❌",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // =========================================================
    // ⚙️ CONFIGURAR BOTONES
    // =========================================================
    private void configurarBotones() {

        // =========================
        // ➗ PAGAR UNA CUOTA
        // =========================
        btnPagarCuota.setOnClickListener(v -> {

            if (!pagoValidoSeleccionado()) return;

            // Valor de UNA cuota 💡
            int valorCuota = pagoSeleccionado.getPrecioTotal()
                    / pagoSeleccionado.getCuotasTotales();

            // Sumamos una cuota pagada
            pagoSeleccionado.setCuotasPagadas(
                    pagoSeleccionado.getCuotasPagadas() + 1
            );

            // Restamos saldo pendiente
            pagoSeleccionado.setSaldoPendiente(
                    Math.max(0,
                            pagoSeleccionado.getSaldoPendiente() - valorCuota)
            );

            guardarPagoActualizado();
            Toast.makeText(this, "Cuota pagada correctamente ✔️", Toast.LENGTH_LONG).show();
        });

        // =========================
        // 💥 PAGAR TODO
        // =========================
        btnPagarTotal.setOnClickListener(v -> {

            if (!pagoValidoSeleccionado()) return;

            // Pagamos TODAS las cuotas
            pagoSeleccionado.setCuotasPagadas(
                    pagoSeleccionado.getCuotasTotales()
            );

            // Dejamos saldo en 0 → el modelo se encarga del estado 😎
            pagoSeleccionado.setSaldoPendiente(0);

            guardarPagoActualizado();
            Toast.makeText(this, "Pago completado ✔️", Toast.LENGTH_LONG).show();
        });
    }

    // =========================================================
    // ✅ VALIDAR PAGO SELECCIONADO
    // =========================================================
    private boolean pagoValidoSeleccionado() {

        if (pagoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un pago primero ☝️", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pagoSeleccionado.isPagado()) {
            Toast.makeText(this, "Este pago ya está completado ✔️", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // =========================================================
    // 💾 GUARDAR CAMBIOS EN FIREBASE
    // =========================================================
    private void guardarPagoActualizado() {

        refPagos.child(pagoSeleccionado.getIdPago())
                .setValue(pagoSeleccionado)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Pago actualizado ☁️", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error al guardar ❌",
                                Toast.LENGTH_LONG).show()
                );
    }
}
