package com.example.appiot12.ui.pago;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appiot12.ui.BaseActivity;

import com.example.appiot12.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ComprarDispositivo extends BaseActivity {

    private static final int PRECIO_DEFAULT = 130000;
    private static final String NOMBRE_PRODUCTO = "Dispositivo AguaSegura";

    private TextView tvPrecio, tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    private int precioActual = PRECIO_DEFAULT;
    private int cuotasSeleccionadas = 1;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_comprar_dispositivo);

        tvPrecio        = findViewById(R.id.tvPrecio);
        tvResumenCuota  = findViewById(R.id.tvResumenCuota);
        spnCuotas       = findViewById(R.id.spnCuotas);
        btnComprar      = findViewById(R.id.btnComprar);

        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            toast("Debes iniciar sesión");
            finish();
            return;
        }

        initSpinner();
        cargarPrecio();

        btnComprar.setOnClickListener(v -> crearCompra());
    }

    private void cargarPrecio() {
        FirebaseDatabase.getInstance()
                .getReference("config/precioDispositivo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Integer precio = snapshot.getValue(Integer.class);
                        precioActual = (precio != null && precio > 0) ? precio : PRECIO_DEFAULT;
                        actualizarUI();
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        precioActual = PRECIO_DEFAULT;
                        actualizarUI();
                    }
                });
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.cuotas_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spnCuotas.setAdapter(adapter);

        spnCuotas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int pos, long id) {
                cuotasSeleccionadas = obtenerCuotas(pos);
                actualizarUI();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private int obtenerCuotas(int pos) {
        return pos == 1 ? 3 : pos == 2 ? 6 : pos == 3 ? 12 : 1;
    }

    private void actualizarUI() {
        int cuota = (int) Math.ceil((double) precioActual / cuotasSeleccionadas);
        tvPrecio.setText(String.format(Locale.getDefault(), "Precio: $%d", precioActual));
        tvResumenCuota.setText(String.format(Locale.getDefault(), "Cuota referencial: $%d", cuota));
    }

    private void crearCompra() {
        btnComprar.setEnabled(false);

        String idCompra = FirebaseDatabase.getInstance().getReference("compras").push().getKey();
        if (idCompra == null) {
            btnComprar.setEnabled(true);
            toast("No se pudo generar la compra");
            return;
        }

        String codigoPedido = "AGS-" + System.currentTimeMillis();

        Map<String, Object> compra = new HashMap<>();
        compra.put("idCompra", idCompra);
        compra.put("uidUsuario", uid);
        compra.put("producto", NOMBRE_PRODUCTO);
        compra.put("monto", precioActual);
        compra.put("cuotas", cuotasSeleccionadas);
        compra.put("codigoPedido", codigoPedido);
        compra.put("estado", "pendiente_pago");
        compra.put("fecha", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("compras").child(idCompra)
                .setValue(compra)
                .addOnSuccessListener(unused -> abrirCentroPagos(idCompra, codigoPedido))
                .addOnFailureListener(e -> {
                    btnComprar.setEnabled(true);
                    toast("No se pudo registrar la compra");
                });
    }

    private void abrirCentroPagos(String idCompra, String codigoPedido) {
        Intent i = new Intent(this, CentroPagos.class);
        i.putExtra("idCompra", idCompra);
        i.putExtra("codigoPedido", codigoPedido);
        i.putExtra("producto", NOMBRE_PRODUCTO);
        i.putExtra("monto", precioActual);
        i.putExtra("cuotas", cuotasSeleccionadas);
        startActivity(i);
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
