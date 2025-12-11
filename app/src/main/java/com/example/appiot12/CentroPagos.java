package com.example.appiot12;
// Zona corporativa donde viven los módulos de pagos. Fintech vibes 💸🏢

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
// Importamos los widgets clave: textos, listas y botones. La UI del “departamento de cobranzas” 📑🔥

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Ajustes modernos para UI edge-to-edge. Layout corporativo de alto impacto 📱✨

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
// Firebase: nuestro “SAP en la nube”, manejando datos financieros en tiempo real ☁️💼

import java.util.ArrayList;
// Colección para almacenar pagos disponibles 📦💰

public class CentroPagos extends AppCompatActivity {
    // Pantalla dedicada al centro financiero del usuario.
    // Aquí se analizan cuotas, deudas y pagos pendientes. Tesorería on fire 💹🔥

    private TextView tvUsuarioPago, tvResumenDeuda; // Información visible del usuario y su deuda total ☑️
    private ListView listPagos;                     // Lista corporativa de pagos 🗃️
    private Button btnPagarCuota, btnPagarTotal;    // Botones estratégicos de cobranza 🧾💳

    private ArrayList<Pago> listaPagos = new ArrayList<>(); // Base de datos local en memoria 🗄️
    private PagoAdapter adapter;                              // Adaptador visual para los pagos 🎨

    private Pago pagoSeleccionado = null; // Pago actualmente seleccionado por el usuario 🏷️

    private DatabaseReference refPagos; // Referencia a nodo Firebase donde viven los pagos del usuario 🔗
    private String uid;                 // ID único del usuario autenticado 🔑

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    // Arranque formal de la Activity ⚙️
        EdgeToEdge.enable(this);              // Activamos modo pantalla completa 📲
        setContentView(R.layout.activity_centro_pagos); // Layout premium cargado 🎨✨

        // Ajuste visual automático de márgenes por barras del sistema 🧩
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // === Vinculación con el XML ===
        tvUsuarioPago = findViewById(R.id.tvUsuarioPago);
        tvResumenDeuda = findViewById(R.id.tvResumenDeuda);
        listPagos = findViewById(R.id.listPagos);
        btnPagarCuota = findViewById(R.id.btnPagarCuota);
        btnPagarTotal = findViewById(R.id.btnPagarTotal);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Obtenemos UID del usuario. Certificado oficial de identidad financiera 😎🔑

        tvUsuarioPago.setText("Usuario: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
        // Mostramos el correo del usuario. Transparencia ante auditorías ✉️✔️

        // Preparamos la referencia Firebase hacia pagos del usuario 💳
        refPagos = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos");

        adapter = new PagoAdapter(this, listaPagos); // Adaptador para listado 📋
        listPagos.setAdapter(adapter);

        cargarPagos();           // Descarga los pagos desde Firebase 🔽
        configurarBotones();     // Conecta funcionalidades a los botones ⚙️
    }

    // ====================================================================
    // 🔄 CARGAR PAGOS DESDE FIREBASE (BACKOFFICE AUTOMATIZADO)
    // ====================================================================
    private void cargarPagos() {

        refPagos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                listaPagos.clear(); // Reseteamos data local antes de recargar ♻️
                int deudaTotal = 0; // Acumulador de deuda para reporte ejecutivo 💰📊

                for (DataSnapshot s : snapshot.getChildren()) {
                    Pago pago = s.getValue(Pago.class); // Convertimos snapshot → objeto Pago 🏦

                    if (pago != null) {
                        listaPagos.add(pago); // Sumamos a la lista visual

                        // Si NO está pagado, sumamos saldo pendiente a la deuda total 💵
                        if (!pago.isPagado()) {
                            deudaTotal += pago.getSaldoPendiente();
                        }
                    }
                }

                // Mostramos deuda acumulada
                tvResumenDeuda.setText("Deuda total: $" + deudaTotal); // KPI financiero actual 📉📈
                adapter.notifyDataSetChanged(); // Refrescamos UI

                if (listaPagos.isEmpty()) {
                    Toast.makeText(CentroPagos.this, "No tienes pagos registrados", Toast.LENGTH_LONG).show();
                    // Comunicación elegante al usuario ✉️
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error al leer datos — mensaje profesional para control de daños 🔥🚨
                Toast.makeText(CentroPagos.this, "Error al leer pagos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // === SELECCIÓN DE UN PAGO ===
        listPagos.setOnItemClickListener((parent, view, pos, id) -> {
            pagoSeleccionado = listaPagos.get(pos); // Marcamos pago seleccionado ✔️
            Toast.makeText(this, "Pago seleccionado", Toast.LENGTH_SHORT).show();
        });
    }

    // ====================================================================
    // ⚙ CONFIGURAR BOTONES DE PAGO (INTERFAZ FINANCIERA)
    // ====================================================================
    private void configurarBotones() {

        // === PAGAR UNA CUOTA ➗ ===
        btnPagarCuota.setOnClickListener(v -> {

            if (pagoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un pago primero", Toast.LENGTH_LONG).show();
                return;
            }

            if (pagoSeleccionado.isPagado()) {
                Toast.makeText(this, "Este pago ya está completado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determinamos valor de cuota (total / cuotas)
            int valorCuota = pagoSeleccionado.getPrecioTotal() / pagoSeleccionado.getCuotasTotales();

            pagoSeleccionado.setCuotasPagadas(
                    pagoSeleccionado.getCuotasPagadas() + 1
            );

            // Recalculamos saldo restante 📉
            pagoSeleccionado.setSaldoPendiente(
                    Math.max(0, pagoSeleccionado.getSaldoPendiente() - valorCuota)
            );

            guardarPagoActualizado(); // Subimos cambios a Firebase ☁️

            Toast.makeText(this, "Cuota pagada correctamente", Toast.LENGTH_LONG).show();
        });

        // === PAGAR TODO 💥 ===
        btnPagarTotal.setOnClickListener(v -> {

            if (pagoSeleccionado == null) {
                Toast.makeText(this, "Seleccione un pago primero", Toast.LENGTH_LONG).show();
                return;
            }

            pagoSeleccionado.setCuotasPagadas(pagoSeleccionado.getCuotasTotales()); // Todo pagado ✔️
            pagoSeleccionado.setSaldoPendiente(0); // Sin deuda 💸😎
            pagoSeleccionado.setPagado(true); // Flag cerrado 🔒

            guardarPagoActualizado(); // Persistimos en la base de datos

            Toast.makeText(this, "Pago completado", Toast.LENGTH_LONG).show();
        });
    }

    // ====================================================================
    // 💾 GUARDAR ACTUALIZACIÓN DEL PAGO EN FIREBASE
    // ====================================================================
    private void guardarPagoActualizado() {

        refPagos.child(pagoSeleccionado.getIdPago())
                .setValue(pagoSeleccionado)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show() // Validación ejecutiva 🟢
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show() // Control de daños 🔥
                );
    }
}
