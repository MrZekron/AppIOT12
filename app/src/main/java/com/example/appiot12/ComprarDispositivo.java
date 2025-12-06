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

public class ComprarDispositivo extends AppCompatActivity {

    private TextView tvPrecio, tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    private final int PRECIO_DISPOSITIVO = 100000; // CLP

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

        // Inicializar precio
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
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int i, long l) {
                calcularCuota(i);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) {}
        });
    }

    private void calcularCuota(int indice) {
        int cuotas = 1;

        switch (indice) {
            case 0: cuotas = 1; break;   // Pago completo
            case 1: cuotas = 3; break;
            case 2: cuotas = 6; break;
            case 3: cuotas = 12; break;
        }

        int valorCuota = PRECIO_DISPOSITIVO / cuotas;
        tvResumenCuota.setText("Valor por cuota: $" + valorCuota);
    }

    private void configurarBotonCompra() {
        btnComprar.setOnClickListener(v -> {
            Toast.makeText(this, "Compra realizada con éxito", Toast.LENGTH_LONG).show();
            // Aquí se debería registrar el pago en Firebase
        });
    }
}
