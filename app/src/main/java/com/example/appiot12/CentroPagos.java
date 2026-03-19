package com.example.appiot12;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Centro de pagos del usuario.
 *
 * Responsabilidades:
 * - listar pagos registrados
 * - seleccionar un pago
 * - pagar una cuota
 * - pagar todo
 * - abrir checkout externo si existe link
 *
 * Esta versión deja listo el terreno para integrar Mercado Pago.
 */
public class CentroPagos extends AppCompatActivity {

    // =========================
    // UI
    // =========================
    private TextView tvUsuarioPago;
    private TextView tvResumenDeuda;
    private ListView listPagos;
    private Button btnPagarCuota;
    private Button btnPagarTotal;

    // =========================
    // DATOS
    // =========================
    private final ArrayList<Pago> listaPagos = new ArrayList<>();
    private PagoAdapter adapter;
    private Pago pagoSeleccionado;

    // =========================
    // FIREBASE
    // =========================
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

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        inicializarVistas();
        configurarFirebase();
        configurarLista();
        configurarBotones();
        cargarPagos();
    }

    // =========================
    // INICIALIZAR UI
    // =========================
    private void inicializarVistas() {
        tvUsuarioPago = findViewById(R.id.tvUsuarioPago);
        tvResumenDeuda = findViewById(R.id.tvResumenDeuda);
        listPagos = findViewById(R.id.listPagos);
        btnPagarCuota = findViewById(R.id.btnPagarCuota);
        btnPagarTotal = findViewById(R.id.btnPagarTotal);

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        tvUsuarioPago.setText("Usuario: " + (email == null ? "" : email));

        adapter = new PagoAdapter(this, listaPagos);
        listPagos.setAdapter(adapter);
    }

    // =========================
    // FIREBASE
    // =========================
    private void configurarFirebase() {
        refPagos = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos");
    }

    // =========================
    // CONFIGURAR LISTA
    // =========================
    private void configurarLista() {
        listPagos.setOnItemClickListener((parent, view, position, id) -> {
            pagoSeleccionado = listaPagos.get(position);

            String nombre = pagoSeleccionado.getNombreProducto();
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = "Dispositivo";
            }

            Toast.makeText(
                    this,
                    "Seleccionado: " + nombre,
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // =========================
    // CARGAR PAGOS
    // =========================
    private void cargarPagos() {
        refPagos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaPagos.clear();
                int deudaTotal = 0;

                for (DataSnapshot s : snapshot.getChildren()) {
                    Pago pago = mapearPagoSeguro(s);

                    if (pago == null) {
                        continue;
                    }

                    if (pago.getIdPago() == null || pago.getIdPago().trim().isEmpty()) {
                        pago.setIdPago(s.getKey());
                    }

                    listaPagos.add(pago);

                    if (!pago.isPagado()) {
                        deudaTotal += pago.getSaldoPendiente();
                    }
                }

                tvResumenDeuda.setText(String.format(
                        Locale.getDefault(),
                        "Deuda total: $%d",
                        deudaTotal
                ));

                adapter.notifyDataSetChanged();

                if (listaPagos.isEmpty()) {
                    Toast.makeText(
                            CentroPagos.this,
                            "No tienes pagos registrados",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        CentroPagos.this,
                        "Error al leer pagos: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // =========================
    // BOTONES
    // =========================
    private void configurarBotones() {

        btnPagarCuota.setOnClickListener(v -> {
            if (!validarPagoSeleccionado()) {
                return;
            }

            if (pagoSeleccionado.isPagado()) {
                Toast.makeText(this, "Este pago ya está completado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Si existe link real de checkout, lo abrimos.
            // Si no existe, por ahora dejamos una actualización local demo.
            if (tieneCheckoutExterno(pagoSeleccionado)) {
                abrirCheckoutExterno(pagoSeleccionado.getCheckoutUrl());
                Toast.makeText(this, "Abriendo pago de cuota...", Toast.LENGTH_SHORT).show();
            } else {
                pagoSeleccionado.pagarUnaCuota();
                guardarPagoActualizado("Cuota pagada correctamente");
            }
        });

        btnPagarTotal.setOnClickListener(v -> {
            if (!validarPagoSeleccionado()) {
                return;
            }

            if (pagoSeleccionado.isPagado()) {
                Toast.makeText(this, "Este pago ya está completado", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tieneCheckoutExterno(pagoSeleccionado)) {
                abrirCheckoutExterno(pagoSeleccionado.getCheckoutUrl());
                Toast.makeText(this, "Abriendo pago total...", Toast.LENGTH_SHORT).show();
            } else {
                pagoSeleccionado.pagarTodo();
                guardarPagoActualizado("Pago completado");
            }
        });
    }

    // =========================
    // VALIDACIÓN
    // =========================
    private boolean validarPagoSeleccionado() {
        if (pagoSeleccionado == null) {
            Toast.makeText(this, "Seleccione un pago primero", Toast.LENGTH_LONG).show();
            return false;
        }

        if (pagoSeleccionado.getIdPago() == null || pagoSeleccionado.getIdPago().trim().isEmpty()) {
            Toast.makeText(this, "El pago seleccionado no tiene ID válido", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // =========================
    // GUARDAR PAGO
    // =========================
    private void guardarPagoActualizado(String mensajeExito) {
        pagoSeleccionado.setUltimaActualizacion(System.currentTimeMillis());

        refPagos.child(pagoSeleccionado.getIdPago())
                .setValue(pagoSeleccionado)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, mensajeExito, Toast.LENGTH_LONG).show();

                    // Registrar en historial
                    HistorialService.registrarPago(
                            pagoSeleccionado.getPrecioTotal() - pagoSeleccionado.getSaldoPendiente(),
                            pagoSeleccionado.getCuotasPagadas()
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Error al actualizar pago: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================
    // CHECKOUT EXTERNO
    // =========================
    private boolean tieneCheckoutExterno(Pago pago) {
        return pago.getCheckoutUrl() != null && !pago.getCheckoutUrl().trim().isEmpty();
    }

    private void abrirCheckoutExterno(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el link de pago", Toast.LENGTH_LONG).show();
        }
    }

    // =========================
    // MAPEO SEGURO DESDE FIREBASE
    // =========================
    private Pago mapearPagoSeguro(DataSnapshot snapshot) {
        try {
            Pago pago = snapshot.getValue(Pago.class);
            if (pago != null) {
                return pago;
            }
        } catch (Exception ignored) {
        }

        // fallback por si los datos vienen desordenados o antiguos
        try {
            GenericTypeIndicator<HashMap<String, Object>> t =
                    new GenericTypeIndicator<HashMap<String, Object>>() {};
            Map<String, Object> raw = snapshot.getValue(t);

            if (raw == null) {
                return null;
            }

            Pago pago = new Pago();
            pago.setIdPago(snapshot.getKey());
            pago.setIdTanque(asString(raw.get("idTanque")));
            pago.setIdDispositivo(asString(raw.get("idDispositivo")));
            pago.setNombreProducto(asString(raw.get("nombreProducto")));
            pago.setPrecioTotal(asInt(raw.get("precioTotal")));
            pago.setCuotasTotales(Math.max(1, asInt(raw.get("cuotasTotales"))));
            pago.setCuotasPagadas(asInt(raw.get("cuotasPagadas")));
            pago.setSaldoPendiente(asInt(raw.get("saldoPendiente")));
            pago.setPagado(asBoolean(raw.get("pagado")));
            pago.setEstadoPago(asString(raw.get("estadoPago")));
            pago.setEstadoEnvio(asString(raw.get("estadoEnvio")));
            pago.setCheckoutUrl(asString(raw.get("checkoutUrl")));
            pago.setMpPreferenceId(asString(raw.get("mpPreferenceId")));
            pago.setMpPaymentId(asString(raw.get("mpPaymentId")));
            pago.setFechaCreacion(asLong(raw.get("fechaCreacion")));
            pago.setUltimaActualizacion(asLong(raw.get("ultimaActualizacion")));
            return pago;

        } catch (Exception e) {
            return null;
        }
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int asInt(Object value) {
        try {
            if (value == null) return 0;
            if (value instanceof Long) return ((Long) value).intValue();
            if (value instanceof Double) return ((Double) value).intValue();
            if (value instanceof Integer) return (Integer) value;
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private long asLong(Object value) {
        try {
            if (value == null) return 0L;
            if (value instanceof Long) return (Long) value;
            if (value instanceof Double) return ((Double) value).longValue();
            if (value instanceof Integer) return ((Integer) value).longValue();
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }

    private boolean asBoolean(Object value) {
        try {
            if (value == null) return false;
            if (value instanceof Boolean) return (Boolean) value;
            return Boolean.parseBoolean(String.valueOf(value));
        } catch (Exception e) {
            return false;
        }
    }
}