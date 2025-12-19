package com.example.appiot12;
// 🛒 Módulo de compra de dispositivos
// Opción A: Pago primero → Dispositivo después 💳➡️🤖

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * 🛒 ComprarDispositivo
 *
 * Flujo REAL:
 * ✔ El usuario elige cuotas
 * ✔ Se crea un PAGO pendiente
 * ✔ Se envía a Centro de Pagos (Mercado Pago simulado)
 * ❌ NO se crea el dispositivo aún
 */
public class ComprarDispositivo extends AppCompatActivity {

    // 💰 Precio fijo del dispositivo (CLP)
    private static final int PRECIO_DISPOSITIVO = 100_000;

    // UI
    private TextView tvPrecio;
    private TextView tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    private int cuotasSeleccionadas = 1;

    // Firebase
    private DatabaseReference refUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_dispositivo);

        inicializarVistas();

        String uid = obtenerUidUsuario();
        if (uid == null) {
            toast("Usuario no autenticado ❌");
            finish();
            return;
        }

        refUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid);

        tvPrecio.setText("Precio: $" + PRECIO_DISPOSITIVO + " CLP");

        inicializarSpinnerCuotas();

        btnComprar.setOnClickListener(v -> procesarCompra());
    }

    // =====================================================
    // 🔗 VISTAS
    // =====================================================
    private void inicializarVistas() {
        tvPrecio = findViewById(R.id.tvPrecio);
        tvResumenCuota = findViewById(R.id.tvResumenCuota);
        spnCuotas = findViewById(R.id.spnCuotas);
        btnComprar = findViewById(R.id.btnComprar);
    }

    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // =====================================================
    // 🔽 SPINNER CUOTAS
    // =====================================================
    private void inicializarSpinnerCuotas() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cuotas_array,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCuotas.setAdapter(adapter);

        spnCuotas.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        cuotasSeleccionadas = obtenerCuotasDesdePosicion(position);
                        int valorCuota = PRECIO_DISPOSITIVO / cuotasSeleccionadas;
                        tvResumenCuota.setText("Valor por cuota: $" + valorCuota);
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                }
        );
    }

    private int obtenerCuotasDesdePosicion(int position) {
        switch (position) {
            case 1: return 3;
            case 2: return 6;
            case 3: return 12;
            default: return 1;
        }
    }

    // =====================================================
    // 🔥 COMPRA REAL (PAGO PENDIENTE)
    // =====================================================
    private void procesarCompra() {

        String idPago = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // 💳 Creamos SOLO el pago (pendiente)
        Pago pago = new Pago(
                idPago,
                PRECIO_DISPOSITIVO,
                cuotasSeleccionadas,
                timestamp
        );

        // ⛔ AÚN NO hay dispositivo
        pago.setEstado("pendiente");

        refUsuario.child("pagos")
                .child(idPago)
                .setValue(pago)
                .addOnSuccessListener(a -> {

                    toast("Pago creado. Redirigiendo a Mercado Pago 💳");

                    // 👉 Ir al Centro de Pagos (simulación Mercado Pago)
                    Intent intent = new Intent(this, CentroPagos.class);
                    intent.putExtra("idPago", idPago);
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e ->
                        toast("Error al crear pago: " + e.getMessage())
                );
    }

    // 🍞 Toast
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
