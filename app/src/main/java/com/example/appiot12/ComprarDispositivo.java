package com.example.appiot12;

// 🛒 Módulo de compra de dispositivos
// Flujo: se crea un pago pendiente y luego se envía al centro de pagos.

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.UUID;

/**
 * ComprarDispositivo
 *
 * Responsabilidades:
 * - mostrar el precio del dispositivo
 * - permitir elegir cuotas
 * - crear un pago pendiente en Firebase
 * - guardar el link de Mercado Pago
 * - enviar al usuario al centro de pagos
 *
 * Regla del flujo:
 * ✔ primero se crea el pago
 * ✔ después se paga
 * ✔ el dispositivo aún no se crea físicamente aquí
 *
 * Mejora aplicada:
 * ✔ el precio puede cambiar desde Firebase
 * ✔ si Firebase no tiene precio, usa uno por defecto
 */
public class ComprarDispositivo extends AppCompatActivity {

    // =====================================================
    // 💰 CONFIGURACIÓN DE COMPRA
    // =====================================================

    // Precio por defecto si Firebase no responde o no tiene valor
    private static final int PRECIO_DEFAULT = 125000;

    // Link real de Mercado Pago
    private static final String CHECKOUT_URL_MP = "https://mpago.li/1ApKZgY";

    // Nombre comercial del producto
    private static final String NOMBRE_PRODUCTO = "Dispositivo AguaSegura";

    // =====================================================
    // 🖥️ UI
    // =====================================================
    private TextView tvPrecio;
    private TextView tvResumenCuota;
    private Spinner spnCuotas;
    private Button btnComprar;

    // =====================================================
    // 📦 ESTADO LOCAL
    // =====================================================
    private int cuotasSeleccionadas = 1;
    private int precioActual = PRECIO_DEFAULT;

    // =====================================================
    // ☁️ FIREBASE
    // =====================================================
    private DatabaseReference refUsuario;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar_dispositivo);

        inicializarVistas();

        uid = obtenerUidUsuario();
        if (uid == null) {
            toast("Usuario no autenticado ❌");
            finish();
            return;
        }

        refUsuario = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid);

        inicializarSpinnerCuotas();
        cargarPrecioDesdeFirebase();

        btnComprar.setOnClickListener(v -> procesarCompra());
    }

    // =====================================================
    // 🔗 INICIALIZAR VISTAS
    // =====================================================
    private void inicializarVistas() {
        tvPrecio = findViewById(R.id.tvPrecio);
        tvResumenCuota = findViewById(R.id.tvResumenCuota);
        spnCuotas = findViewById(R.id.spnCuotas);
        btnComprar = findViewById(R.id.btnComprar);
    }

    // =====================================================
    // 🔐 OBTENER UID
    // =====================================================
    private String obtenerUidUsuario() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // =====================================================
    // 💰 CARGAR PRECIO DESDE FIREBASE
    // =====================================================
    /**
     * Busca el precio del dispositivo en Firebase.
     *
     * Ruta esperada:
     * configuracion/precioDispositivo
     *
     * Si no existe o falla, usa PRECIO_DEFAULT.
     */
    private void cargarPrecioDesdeFirebase() {

        DatabaseReference refConfig = FirebaseDatabase.getInstance()
                .getReference("configuracion")
                .child("precioDispositivo");

        refConfig.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Integer precio = snapshot.getValue(Integer.class);

                if (precio != null && precio > 0) {
                    precioActual = precio;
                } else {
                    precioActual = PRECIO_DEFAULT;
                }

                actualizarUIPrecio();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                precioActual = PRECIO_DEFAULT;
                actualizarUIPrecio();
            }
        });
    }

    // =====================================================
    // 💰 ACTUALIZAR UI DEL PRECIO
    // =====================================================
    /**
     * Refresca el precio total y el valor por cuota en pantalla.
     */
    private void actualizarUIPrecio() {
        tvPrecio.setText(String.format(
                Locale.getDefault(),
                "Precio: $%,d CLP",
                precioActual
        ));

        int valorCuota = (int) Math.ceil((double) precioActual / cuotasSeleccionadas);

        tvResumenCuota.setText(String.format(
                Locale.getDefault(),
                "Valor por cuota: $%,d",
                valorCuota
        ));
    }

    // =====================================================
    // 🔽 SPINNER DE CUOTAS
    // =====================================================
    private void inicializarSpinnerCuotas() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.cuotas_array,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCuotas.setAdapter(adapter);

        spnCuotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cuotasSeleccionadas = obtenerCuotasDesdePosicion(position);
                actualizarUIPrecio();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se requiere acción
            }
        });
    }

    /**
     * Convierte la posición del spinner a número real de cuotas.
     */
    private int obtenerCuotasDesdePosicion(int position) {
        switch (position) {
            case 1:
                return 3;
            case 2:
                return 6;
            case 3:
                return 12;
            default:
                return 1;
        }
    }

    // =====================================================
    // 🛒 PROCESAR COMPRA
    // =====================================================
    private void procesarCompra() {

        String idPago = UUID.randomUUID().toString();

        // Si después quieres asociar el pago a un tanque o dispositivo específico,
        // aquí puedes reemplazar estos valores por los IDs reales.
        String idTanque = "";
        String idDispositivo = "";

        // Crear el objeto pago con la estructura actual
        Pago pago = new Pago(
                idPago,
                idTanque,
                idDispositivo,
                NOMBRE_PRODUCTO,
                precioActual,
                cuotasSeleccionadas
        );

        // Configuración inicial del flujo de pago
        pago.setEstadoPago("pendiente");
        pago.setEstadoEnvio("preparando");
        pago.setCheckoutUrl(CHECKOUT_URL_MP);

        // IDs opcionales de Mercado Pago (vacíos por ahora)
        pago.setMpPreferenceId("");
        pago.setMpPaymentId("");

        // Guardar en Firebase
        refUsuario.child("pagos")
                .child(idPago)
                .setValue(pago)
                .addOnSuccessListener(unused -> {

                    toast("Pago creado correctamente. Redirigiendo al centro de pagos 💳");

                    // Registrar en historial
                    HistorialService.registrarEvento(
                            "COMPRA",
                            "Se creó un pago pendiente para " + NOMBRE_PRODUCTO
                    );

                    // Ir a la pantalla de pagos
                    Intent intent = new Intent(this, CentroPagos.class);
                    intent.putExtra("idPago", idPago);
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e ->
                        toast("Error al crear pago: " + e.getMessage())
                );
    }

    // =====================================================
    // 🍞 TOAST
    // =====================================================
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}