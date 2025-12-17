package com.example.appiot12;
// 💳 Paquete del módulo de compras del proyecto Agua Segura.
// Aquí se gestionan compras de dispositivos IoT de forma simple y ordenada 🏦🤖💧

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
// 🖥️ Componentes visuales para mostrar precio, cuotas y ejecutar la compra

import androidx.appcompat.app.AppCompatActivity;
// 🎖️ Activity base moderna y estable

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// ☁️ Firebase: guardamos dispositivos y pagos en la nube

import java.util.UUID;
// 🔑 Generador de IDs únicos (sin duplicados, sin problemas)

/**
 * 🛒 ComprarDispositivo
 *
 * Esta pantalla permite:
 * 👉 Elegir en cuántas cuotas comprar un dispositivo
 * 👉 Crear el dispositivo IoT
 * 👉 Crear el pago asociado
 * 👉 Guardar todo en Firebase
 *
 * En simple:
 * Es la tienda oficial de dispositivos del sistema 🛍️🙂
 */
public class ComprarDispositivo extends AppCompatActivity {

    // 💰 Precio fijo del dispositivo (CLP)
    private static final int PRECIO_DISPOSITIVO = 100_000;

    // 🖥️ Elementos de la interfaz
    private TextView tvPrecio;
    private TextView tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    // ➗ Cuotas seleccionadas por el usuario
    private int cuotasSeleccionadas = 1;

    // ☁️ Referencia base a Firebase
    private DatabaseReference refUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_dispositivo); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos la UI con el XML
        inicializarVistas();

        // 👤 Obtenemos usuario autenticado
        String uid = obtenerUidUsuario();
        if (uid == null) {
            Toast.makeText(this, "Usuario no autenticado ❌", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ☁️ Apuntamos al nodo del usuario en Firebase
        refUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid);

        // 📈 Mostramos el precio del dispositivo
        tvPrecio.setText("Precio: $" + PRECIO_DISPOSITIVO + " CLP");

        // 🔽 Configuramos selector de cuotas
        inicializarSpinnerCuotas();

        // 🟢 Configuramos botón comprar
        btnComprar.setOnClickListener(v -> procesarCompra());
    }

    /**
     * 🔗 Conecta los componentes visuales con el XML
     */
    private void inicializarVistas() {
        tvPrecio = findViewById(R.id.tvPrecio);
        tvResumenCuota = findViewById(R.id.tvResumenCuota);
        spnCuotas = findViewById(R.id.spnCuotas);
        btnComprar = findViewById(R.id.btnComprar);
    }

    /**
     * 👤 Obtiene el UID del usuario autenticado
     */
    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // =====================================================
    // 🔽 CONFIGURAR SPINNER DE CUOTAS
    // =====================================================
    private void inicializarSpinnerCuotas() {

        // 📋 Cargamos las opciones desde resources (XML)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cuotas_array,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spnCuotas.setAdapter(adapter);

        // 🧠 Detectamos selección del usuario
        spnCuotas.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        // 🔢 Traducimos posición → número de cuotas
                        cuotasSeleccionadas = obtenerCuotasDesdePosicion(position);

                        // 💰 Calculamos valor de cada cuota
                        int valorCuota = PRECIO_DISPOSITIVO / cuotasSeleccionadas;

                        // 📊 Mostramos resumen al usuario
                        tvResumenCuota.setText(
                                "Valor por cuota: $" + valorCuota
                        );
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                        // No hacemos nada aquí 👍
                    }
                }
        );
    }

    /**
     * 🔢 Convierte la posición del Spinner en número de cuotas
     */
    private int obtenerCuotasDesdePosicion(int position) {
        switch (position) {
            case 1: return 3;
            case 2: return 6;
            case 3: return 12;
            default: return 1; // posición 0
        }
    }

    // =====================================================
    // 🔥 PROCESAR COMPRA
    // =====================================================
    private void procesarCompra() {

        // 🆔 Generamos IDs únicos
        String idDispositivo = UUID.randomUUID().toString();
        String idPago = UUID.randomUUID().toString();

        // 🤖 Creamos el dispositivo con valores iniciales
        Dispositivo dispositivo = crearDispositivo(idDispositivo);

        // 💳 Creamos el pago asociado
        Pago pago = crearPago(idPago, idDispositivo);

        // ☁️ Guardamos dispositivo en Firebase
        refUsuario.child("dispositivos")
                .child(idDispositivo)
                .setValue(dispositivo);

        // ☁️ Guardamos pago en Firebase
        refUsuario.child("pagos")
                .child(idPago)
                .setValue(pago)
                .addOnSuccessListener(a -> {
                    Toast.makeText(
                            this,
                            "Dispositivo comprado y pago registrado 🎉",
                            Toast.LENGTH_LONG
                    ).show();
                    finish(); // 🚪 Cerramos pantalla
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Error al registrar pago: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    /**
     * 🤖 Crea un dispositivo IoT con valores iniciales
     */
    private Dispositivo crearDispositivo(String idDispositivo) {

        // Valores iniciales simulados:
        // pH neutro, lecturas base seguras
        return new Dispositivo(
                idDispositivo,
                7.0,     // 🧪 pH
                500.0,   // ⚡ Conductividad
                1.0,     // 🌫️ Turbidez
                1000.0   // 📏 Nivel
        );
    }

    /**
     * 💳 Crea el objeto Pago asociado a la compra
     */
    private Pago crearPago(String idPago, String idDispositivo) {

        long timestamp = System.currentTimeMillis();

        return new Pago(
                idPago,
                PRECIO_DISPOSITIVO,
                cuotasSeleccionadas,
                timestamp,
                idDispositivo
        );
    }
}
