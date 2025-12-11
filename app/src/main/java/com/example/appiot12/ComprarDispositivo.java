package com.example.appiot12;
// Zona premium donde vive el módulo de compras. La fintech oficial de tus dispositivos IoT 💳🤖

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
// Widgets para UI financiera: textos, spinners, botones y toasts 💸📱

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Ajuste visual incómodo para usuarios, pero elegante para CEOs 🧑‍💼✨

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
// Firebase: la nube que respalda nuestras transacciones IoT ☁️📡

import java.util.UUID;
// Generador de IDs únicos estilo “no-duplicable-a-nivel-galáctico” 🌌

public class ComprarDispositivo extends AppCompatActivity {
    // Activity encargada de ejecutar el flujo de compra:
    // seleccionar cuotas → registrar dispositivo → generar pago.
    // Básicamente, un e-commerce minimalista pero funcional 🛒⚙️

    private TextView tvPrecio, tvResumenCuota; // Etiquetas informativas del precio y cuota 💰📊
    private Spinner spnCuotas;                 // Selector de cuotas flexible 🔽
    private Button btnComprar;                 // Botón para disparar operación financiera 🟩💳

    private final int PRECIO_DISPOSITIVO = 100000; // Precio fijo (CLP). CFO-approved 🇨🇱💵
    private int cuotasSeleccionadas = 1;            // Default: 1 cuota ✔️

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);                        // Pantalla de extremo a extremo 📱✨
        setContentView(R.layout.activity_comprar_dispositivo); // Renderiza el layout principal 🎨

        // Ajustes automáticos según barras del sistema (modern UI)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom); // Padding dinámico 📐
            return insets;
        });

        // ================================
        // VINCULACIÓN DEL XML CON JAVA
        // ================================
        tvPrecio = findViewById(R.id.tvPrecio);
        tvResumenCuota = findViewById(R.id.tvResumenCuota);
        spnCuotas = findViewById(R.id.spnCuotas);
        btnComprar = findViewById(R.id.btnComprar);

        // Mostramos el precio oficialmente 📈
        tvPrecio.setText("Precio: $" + PRECIO_DISPOSITIVO + " CLP");

        inicializarSpinner();     // Carga el selector de cuotas 🔽
        configurarBotonCompra();  // Prepara el botón de compra 💳
    }

    // ============================================
    // 🔽 INICIALIZAR SPINNER DE CUOTAS
    // ============================================
    private void inicializarSpinner() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,                       // Contexto actual
                R.array.cuotas_array,       // Arreglo de cuotas definido en resources XML
                android.R.layout.simple_spinner_item // Layout minimalista oficial 🎨
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCuotas.setAdapter(adapter); // Se carga el spinner

        // Listener para saber cuántas cuotas eligió el usuario
        spnCuotas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> adapterView, View view, int pos, long id) {

                // Asignamos número de cuotas según posición del Spinner
                switch (pos) {
                    case 0: cuotasSeleccionadas = 1; break;
                    case 1: cuotasSeleccionadas = 3; break;
                    case 2: cuotasSeleccionadas = 6; break;
                    case 3: cuotasSeleccionadas = 12; break;
                }

                // Cálculo contable de la cuota
                int valorCuota = PRECIO_DISPOSITIVO / cuotasSeleccionadas;
                tvResumenCuota.setText("Valor por cuota: $" + valorCuota);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> adapterView) { }
        });
    }


    // =============================================================
    // 🟢 CONFIGURAR BOTÓN COMPRAR (ORQUESTADOR FINANCIERO)
    // =============================================================
    private void configurarBotonCompra() {

        btnComprar.setOnClickListener(v -> procesarCompra());
        // Cuando el usuario presiona, se inicia el flujo de compra ☕💳
    }


    // =============================================================
    // 🔥 PROCESAR COMPRA — CREA DISPOSITIVO + CREA PAGO
    // =============================================================
    private void procesarCompra() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uid == null) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_LONG).show();
            return; // Abortamos misión 🚨
        }

        // ==========================
        // Generamos IDs únicos 🔐
        // ==========================
        String idDispositivo = UUID.randomUUID().toString();
        String idPago = UUID.randomUUID().toString();

        // =============================================================
        // 1) Crear DISPOSITIVO (No asociado a ningún tanque aún)
        // =============================================================
        Dispositivo dispositivo = new Dispositivo(
                idDispositivo,  // ID único
                7.0,            // pH inicial simulado 🤖
                500.0,          // Conductividad inicial
                1.0,            // Turbidez base
                1000.0          // Nivel base
        );

        // =============================================================
        // 2) Crear PAGO (CONSTRUCTOR FORMAL)
        // =============================================================
        long timestamp = System.currentTimeMillis();

        Pago pago = new Pago(
                idPago,               // idPago
                PRECIO_DISPOSITIVO,   // precio total de la compra
                cuotasSeleccionadas,  // cuotas seleccionadas por el usuario
                timestamp,            // fecha de creación del pago
                idDispositivo         // ID del dispositivo comprado
        );

        // ============================
        // Guardar DISPOSITIVO en Firebase
        // ============================
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(idDispositivo)
                .setValue(dispositivo);

        // ============================
        // Guardar PAGO en Firebase
        // ============================
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("pagos")
                .child(idPago)
                .setValue(pago)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Dispositivo comprado y pago registrado 🎉", Toast.LENGTH_LONG).show();
                    finish(); // Cerramos pantalla porque la compra finalizó ✔️
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al registrar pago: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
