package com.example.appiot12.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.ui.BaseActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfirmarPagosAdmin extends BaseActivity {

    private ListView lvComprobantes;
    private final List<Map<String, Object>> comprobantes = new ArrayList<>();
    private ArrayAdapter<Map<String, Object>> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_confirmar_pagos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        lvComprobantes = findViewById(R.id.lvComprobantes);
        findViewById(R.id.btnVolverConfirmar).setOnClickListener(v -> finish());

        adapter = new ArrayAdapter<Map<String, Object>>(this, R.layout.item_comprobante, comprobantes) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_comprobante, parent, false);
                }
                Map<String, Object> item = comprobantes.get(position);

                TextView tvProducto = convertView.findViewById(R.id.tvComprobanteProducto);
                TextView tvUsuario  = convertView.findViewById(R.id.tvComprobanteUsuario);
                TextView tvMonto    = convertView.findViewById(R.id.tvComprobanteMonto);
                Button   btnConf    = convertView.findViewById(R.id.btnConfirmar);
                Button   btnRech    = convertView.findViewById(R.id.btnRechazar);

                tvProducto.setText(strOf(item, "producto"));
                tvUsuario.setText("Usuario: " + strOf(item, "uidUsuario"));
                int monto = intOf(item, "monto");
                tvMonto.setText(String.format(Locale.getDefault(), "Monto: $%,d", monto));

                btnConf.setOnClickListener(v -> confirmar(item));
                btnRech.setOnClickListener(v -> rechazar(item));

                return convertView;
            }
        };

        lvComprobantes.setAdapter(adapter);
        cargarComprobantes();
    }

    private void cargarComprobantes() {
        FirebaseDatabase.getInstance().getReference("compras")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        comprobantes.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String tipo   = snap.child("tipo").getValue(String.class);
                            String estado = snap.child("estado").getValue(String.class);
                            if ("cuota".equals(tipo) && "comprobante_enviado".equals(estado)) {
                                Map<String, Object> item = new HashMap<>();
                                item.put("idCompra",   snap.child("idCompra").getValue(String.class));
                                item.put("uidUsuario", snap.child("uidUsuario").getValue(String.class));
                                item.put("idPagoRef",  snap.child("idPagoRef").getValue(String.class));
                                item.put("numeroCuota",snap.child("numeroCuota").getValue(Long.class));
                                item.put("monto",      snap.child("monto").getValue(Long.class));
                                item.put("producto",   snap.child("producto").getValue(String.class));
                                comprobantes.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (comprobantes.isEmpty()) {
                            Toast.makeText(ConfirmarPagosAdmin.this,
                                    "No hay comprobantes pendientes", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ConfirmarPagosAdmin.this,
                                "Error al cargar comprobantes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmar(Map<String, Object> item) {
        String idCompra  = strOf(item, "idCompra");
        String uid       = strOf(item, "uidUsuario");
        String idPagoRef = strOf(item, "idPagoRef");
        int monto        = intOf(item, "monto");

        if (idPagoRef.isEmpty() || uid.isEmpty()) {
            Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("usuarios")
                .child(uid).child("pagos").child(idPagoRef)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Long cuotasPagadasL = snapshot.child("cuotasPagadas").getValue(Long.class);
                        Long cuotasTotalesL = snapshot.child("cuotasTotales").getValue(Long.class);
                        Long saldoL         = snapshot.child("saldoPendiente").getValue(Long.class);

                        int cuotasPagadas = cuotasPagadasL != null ? cuotasPagadasL.intValue() : 0;
                        int cuotasTotales = cuotasTotalesL != null ? cuotasTotalesL.intValue() : 1;
                        int saldo         = saldoL         != null ? saldoL.intValue()         : 0;

                        int nuevasCuotas = cuotasPagadas + 1;
                        int nuevoSaldo   = Math.max(0, saldo - monto);
                        boolean pagadoTotal = nuevasCuotas >= cuotasTotales;

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("compras/" + idCompra + "/estado", "pagado");
                        updates.put("usuarios/" + uid + "/pagos/" + idPagoRef + "/cuotasPagadas", nuevasCuotas);
                        updates.put("usuarios/" + uid + "/pagos/" + idPagoRef + "/saldoPendiente", nuevoSaldo);
                        updates.put("usuarios/" + uid + "/pagos/" + idPagoRef + "/ultimaActualizacion",
                                System.currentTimeMillis());
                        if (pagadoTotal) {
                            updates.put("usuarios/" + uid + "/pagos/" + idPagoRef + "/pagado", true);
                            updates.put("usuarios/" + uid + "/pagos/" + idPagoRef + "/estadoPago", "pagado");
                        }

                        FirebaseDatabase.getInstance().getReference().updateChildren(updates)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(ConfirmarPagosAdmin.this,
                                                "Cuota confirmada", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(ConfirmarPagosAdmin.this,
                                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ConfirmarPagosAdmin.this,
                                "Error al leer el pago", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rechazar(Map<String, Object> item) {
        String idCompra = strOf(item, "idCompra");
        FirebaseDatabase.getInstance().getReference("compras").child(idCompra)
                .child("estado").setValue("rechazado")
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Comprobante rechazado", Toast.LENGTH_SHORT).show());
    }

    private String strOf(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private int intOf(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Long) return ((Long) v).intValue();
        if (v instanceof Integer) return (Integer) v;
        return 0;
    }
}
