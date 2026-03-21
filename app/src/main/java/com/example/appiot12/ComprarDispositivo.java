package com.example.appiot12;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

public class ComprarDispositivo extends AppCompatActivity {

    private static final int PRECIO_DEFAULT = 125000;
    private static final String NOMBRE_PRODUCTO = "Dispositivo AguaSegura";

    // 🔥 URL DE TU CLOUD FUNCTION (IMPORTANTE CAMBIAR)
    private static final String URL_BACKEND =
            "https://TU_REGION-TU_PROYECTO.cloudfunctions.net/crearPreferenciaPago";

    private TextView tvPrecio, tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    private int cuotasSeleccionadas = 1;
    private int precioActual = PRECIO_DEFAULT;

    private DatabaseReference refUsuario;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_dispositivo);

        tvPrecio = findViewById(R.id.tvPrecio);
        tvResumenCuota = findViewById(R.id.tvResumenCuota);
        spnCuotas = findViewById(R.id.spnCuotas);
        btnComprar = findViewById(R.id.btnComprar);

        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) finish();

        refUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid);

        initSpinner();
        cargarPrecio();

        btnComprar.setOnClickListener(v -> crearPagoYCheckout());
    }

    private void cargarPrecio() {
        FirebaseDatabase.getInstance()
                .getReference("configuracion/precioDispositivo")
                .get()
                .addOnSuccessListener(snap -> {
                    Integer p = snap.getValue(Integer.class);
                    precioActual = (p != null) ? p : PRECIO_DEFAULT;
                    actualizarUI();
                });
    }

    private void actualizarUI() {
        tvPrecio.setText("Precio: $" + precioActual);

        int cuota = (int) Math.ceil((double) precioActual / cuotasSeleccionadas);
        tvResumenCuota.setText("Cuota: $" + cuota);
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cuotas_array,
                android.R.layout.simple_spinner_item
        );

        spnCuotas.setAdapter(adapter);

        spnCuotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                cuotasSeleccionadas = obtenerCuotas(pos);
                actualizarUI();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int obtenerCuotas(int pos) {
        switch (pos) {
            case 1: return 3;
            case 2: return 6;
            case 3: return 12;
            default: return 1;
        }
    }

    // =====================================================
    // 🔥 CREAR PAGO + LLAMAR BACKEND
    // =====================================================
    private void crearPagoYCheckout() {

        String idPago = UUID.randomUUID().toString();

        Pago pago = new Pago(
                idPago,
                "",
                "",
                NOMBRE_PRODUCTO,
                precioActual,
                cuotasSeleccionadas
        );

        pago.setEstadoPago("pendiente");

        refUsuario.child("pagos").child(idPago)
                .setValue(pago)
                .addOnSuccessListener(unused -> {

                    // 🔥 LLAMAR BACKEND
                    new Thread(() -> {
                        try {
                            URL url = new URL(URL_BACKEND);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setRequestProperty("Content-Type", "application/json");

                            JSONObject json = new JSONObject();
                            json.put("idPago", idPago);
                            json.put("titulo", NOMBRE_PRODUCTO);
                            json.put("precio", precioActual);
                            json.put("uid", uid);

                            OutputStream os = conn.getOutputStream();
                            os.write(json.toString().getBytes());
                            os.flush();

                            if (conn.getResponseCode() == 200) {

                                runOnUiThread(() -> {
                                    Intent intent = new Intent(this, CentroPagos.class);
                                    intent.putExtra("idPago", idPago);
                                    startActivity(intent);
                                    finish();
                                });
                            }

                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Error backend ❌", Toast.LENGTH_LONG).show()
                            );
                        }
                    }).start();

                });
    }
}