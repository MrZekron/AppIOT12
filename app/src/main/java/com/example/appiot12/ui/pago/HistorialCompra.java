package com.example.appiot12.ui.pago;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.appiot12.ui.BaseActivity;

import com.example.appiot12.R;
import com.example.appiot12.adapter.PagoAdapter;
import com.example.appiot12.model.Pago;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistorialCompra extends BaseActivity {

    private ListView lvPagos;
    private final List<Pago> pagos = new ArrayList<>();
    private PagoAdapter pagoAdapter;
    private DatabaseReference refPagos;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_historial_compra);

        lvPagos = findViewById(R.id.lvPagos);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        uid = user.getUid();

        refPagos = FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("pagos");

        pagoAdapter = new PagoAdapter(this, pagos);
        pagoAdapter.setOnPagarCuotaListener(this::iniciarPagoCuota);
        lvPagos.setAdapter(pagoAdapter);

        cargarPagos();
    }

    private void cargarPagos() {
        refPagos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pagos.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(HistorialCompra.this, "No tenés pagos registrados", Toast.LENGTH_LONG).show();
                    pagoAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Pago pago = snap.getValue(Pago.class);
                    if (pago != null) {
                        if (pago.getIdPago() == null) pago.setIdPago(snap.getKey());
                        pagos.add(pago);
                    }
                }

                pagoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistorialCompra.this, "Error al leer pagos", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarPagoCuota(Pago pago) {
        if (pago.getCuotasTotales() <= 0) return;

        int montoCuota = (int) Math.ceil((double) pago.getPrecioTotal() / pago.getCuotasTotales());
        int numeroCuota = pago.getCuotasPagadas() + 1;
        String codigoPedido = "AGS-CTA-" + System.currentTimeMillis();
        String productoLabel = "Cuota " + numeroCuota + " — " + pago.getNombreProducto();

        DatabaseReference refCompras = FirebaseDatabase.getInstance().getReference("compras");
        String nuevaCompraId = refCompras.push().getKey();
        if (nuevaCompraId == null) {
            Toast.makeText(this, "Error al generar el comprobante", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> cuotaCompra = new HashMap<>();
        cuotaCompra.put("idCompra", nuevaCompraId);
        cuotaCompra.put("tipo", "cuota");
        cuotaCompra.put("uidUsuario", uid);
        cuotaCompra.put("idPagoRef", pago.getIdPago());
        cuotaCompra.put("numeroCuota", numeroCuota);
        cuotaCompra.put("monto", montoCuota);
        cuotaCompra.put("producto", productoLabel);
        cuotaCompra.put("codigoPedido", codigoPedido);
        cuotaCompra.put("estado", "pendiente_pago");
        cuotaCompra.put("fecha", System.currentTimeMillis());

        refCompras.child(nuevaCompraId).setValue(cuotaCompra)
                .addOnSuccessListener(unused -> {
                    Intent i = new Intent(this, CentroPagos.class);
                    i.putExtra("idCompra", nuevaCompraId);
                    i.putExtra("codigoPedido", codigoPedido);
                    i.putExtra("producto", productoLabel);
                    i.putExtra("monto", montoCuota);
                    i.putExtra("cuotas", 1);
                    startActivity(i);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al crear el comprobante", Toast.LENGTH_SHORT).show());
    }

    public void volverAlMenu(View view) {
        finish();
    }
}
