package com.example.appiot12;

// Pantalla para solicitar la compra manual del dispositivo por transferencia.

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ComprarDispositivo extends AppCompatActivity {

    // Configuración base del producto.
    private static final int PRECIO_DEFAULT = 125000;
    private static final String NOMBRE_PRODUCTO = "Dispositivo AguaSegura";

    // Vistas.
    private TextView tvPrecio, tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    // Estado de compra.
    private int precioActual = PRECIO_DEFAULT;
    private int cuotasSeleccionadas = 1;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_dispositivo);

        // Vincula vistas del XML.
        tvPrecio = findViewById(R.id.tvPrecio);
        tvResumenCuota = findViewById(R.id.tvResumenCuota);
        spnCuotas = findViewById(R.id.spnCuotas);
        btnComprar = findViewById(R.id.btnComprar);

        // Obtiene usuario actual.
        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            toast("Debes iniciar sesión");
            finish();
            return;
        }

        // Inicializa cuotas y carga precio.
        initSpinner();
        cargarPrecio();

        // Crea la compra manual.
        btnComprar.setOnClickListener(v -> crearCompra());
    }

    // Carga el precio desde Firebase; si no existe, usa el valor por defecto.
    private void cargarPrecio() {
        FirebaseDatabase.getInstance()
                .getReference("configuracion/precioDispositivo")
                .get()
                .addOnSuccessListener(snap -> {
                    Integer precio = snap.getValue(Integer.class);
                    precioActual = precio != null ? precio : PRECIO_DEFAULT;
                    actualizarUI();
                })
                .addOnFailureListener(e -> actualizarUI());
    }

    // Configura selector de cuotas.
    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cuotas_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    // Convierte la posición del spinner en cantidad de cuotas.
    private int obtenerCuotas(int pos) {
        return pos == 1 ? 3 : pos == 2 ? 6 : pos == 3 ? 12 : 1;
    }

    // Actualiza precio total y valor estimado por cuota.
    private void actualizarUI() {
        int cuota = (int) Math.ceil((double) precioActual / cuotasSeleccionadas);
        tvPrecio.setText(String.format(Locale.getDefault(), "Precio: $%d", precioActual));
        tvResumenCuota.setText(String.format(Locale.getDefault(), "Cuota referencial: $%d", cuota));
    }

    // Crea una compra en Firebase para pago por transferencia.
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

        FirebaseDatabase.getInstance()
                .getReference("compras")
                .child(idCompra)
                .setValue(compra)
                .addOnSuccessListener(unused -> abrirCentroPagos(idCompra, codigoPedido))
                .addOnFailureListener(e -> {
                    btnComprar.setEnabled(true);
                    toast("No se pudo registrar la compra");
                });
    }

    // Abre la pantalla con los datos para transferencia.
    private void abrirCentroPagos(String idCompra, String codigoPedido) {
        Intent i = new Intent(this, CentroPagos.class);
        i.putExtra("idCompra", idCompra);
        i.putExtra("codigoPedido", codigoPedido);
        i.putExtra("producto", NOMBRE_PRODUCTO);
        i.putExtra("monto", precioActual);
        startActivity(i);
        finish();
    }

    // Muestra mensajes rápidos al usuario.
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}