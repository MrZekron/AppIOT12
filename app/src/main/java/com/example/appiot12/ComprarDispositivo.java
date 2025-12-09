package com.example.appiot12;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class ComprarDispositivo extends AppCompatActivity {

    private TextView tvPrecio, tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    private final int PRECIO_DISPOSITIVO = 100000; // CLP
    private int cuotasSeleccionadas = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comprar_dispositivo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // Vincular XML con Java
        tvPrecio = findViewById(R.id.tvPrecio);
        tvResumenCuota = findViewById(R.id.tvResumenCuota);
        spnCuotas = findViewById(R.id.spnCuotas);
        btnComprar = findViewById(R.id.btnComprar);

        tvPrecio.setText("Precio: $" + PRECIO_DISPOSITIVO + " CLP");

        inicializarSpinner();
        configurarBotonCompra();
    }

    private void inicializarSpinner() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cuotas_array,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCuotas.setAdapter(adapter);

        spnCuotas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int pos, long id) {

                switch (pos) {
                    case 0: cuotasSeleccionadas = 1; break;
                    case 1: cuotasSeleccionadas = 3; break;
                    case 2: cuotasSeleccionadas = 6; break;
                    case 3: cuotasSeleccionadas = 12; break;
                }

                int valorCuota = PRECIO_DISPOSITIVO / cuotasSeleccionadas;
                tvResumenCuota.setText("Valor por cuota: $" + valorCuota);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) { }
        });
    }


    // =============================================================
    // 🟢 BOTÓN COMPRAR — CREA DISPOSITIVO + CREA PAGO EN FIREBASE
    // =============================================================
    private void configurarBotonCompra() {

        btnComprar.setOnClickListener(v -> procesarCompra());
    }


    private void procesarCompra() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_LONG).show();
            return;
        }

        // 🆔 IDs únicos
        String idDispositivo = UUID.randomUUID().toString();
        String idPago = UUID.randomUUID().toString();

        // =============================================================
        // 1) Crear DISPOSITIVO (sin tanque asignado)
        // =============================================================
        Dispositivo dispositivo = new Dispositivo(
                idDispositivo,
                7.0,
                500.0,
                1.0,
                100.0
        );

        // =============================================================
        // 2) Crear PAGO asociado EXTERNAMENTE
        // =============================================================
        Pago pago = new Pago(PRECIO_DISPOSITIVO, cuotasSeleccionadas, idDispositivo);

        // Le agrego manualmente el idDispositivo:
        //  🔥 MODIFICACIÓN NECESARIA EN Pago.java:
        //     agregar: private String idDispositivo;
        //     + getter/setter
        pago.setIdDispositivo(idDispositivo);

        // Guardar DISPOSITIVO
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(idDispositivo)
                .setValue(dispositivo);

        // Guardar PAGO
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos")
                .child(idPago)
                .setValue(pago)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Dispositivo comprado y pago registrado", Toast.LENGTH_LONG).show();
                    finish(); // cerrar pantalla
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al registrar pago: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
