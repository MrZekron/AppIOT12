package com.example.appiot12.ui.pago;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CentroPagos extends AppCompatActivity {

    private static final String BANCO = "Mercado Pago";
    private static final String TIPO_CUENTA = "Cuenta Vista";
    private static final String NUMERO_CUENTA = "1085751669";
    private static final String TITULAR = "Moisés Bensaid Cid Valenzuela";
    private static final String RUT = "21.503.331-7";
    private static final String CORREO_PAGO = "cidm3965@gmail.com";
    private static final String TELEFONO_WHATSAPP = "56954181940";

    private TextView tvProducto, tvMonto, tvCodigoPedido, tvBanco, tvTipoCuenta,
            tvNumeroCuenta, tvTitular, tvRut, tvCorreoPago;
    private Button btnWhatsapp, btnVolver;

    private String idCompra, codigoPedido, producto;
    private int monto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centro_pagos);

        tvProducto     = findViewById(R.id.tvProductoPago);
        tvMonto        = findViewById(R.id.tvMontoPago);
        tvCodigoPedido = findViewById(R.id.tvCodigoPedido);
        tvBanco        = findViewById(R.id.tvBanco);
        tvTipoCuenta   = findViewById(R.id.tvTipoCuenta);
        tvNumeroCuenta = findViewById(R.id.tvNumeroCuenta);
        tvTitular      = findViewById(R.id.tvTitular);
        tvRut          = findViewById(R.id.tvRut);
        tvCorreoPago   = findViewById(R.id.tvCorreoPago);
        btnWhatsapp    = findViewById(R.id.btnEnviarWhatsapp);
        btnVolver      = findViewById(R.id.btnVolverMenu);

        leerExtras();

        if (idCompra == null || codigoPedido == null || producto == null || monto <= 0) {
            toast("Datos de compra incompletos");
            finish();
            return;
        }

        cargarDatosPantalla();

        btnWhatsapp.setOnClickListener(v -> enviarWhatsapp());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void leerExtras() {
        Intent i = getIntent();
        idCompra     = i.getStringExtra("idCompra");
        codigoPedido = i.getStringExtra("codigoPedido");
        producto     = i.getStringExtra("producto");
        monto        = i.getIntExtra("monto", 0);
    }

    private void cargarDatosPantalla() {
        tvProducto.setText("Producto: " + producto);
        tvMonto.setText(String.format(Locale.getDefault(), "Monto: $%d", monto));
        tvCodigoPedido.setText("Código de pedido: " + codigoPedido);
        tvBanco.setText("Banco: " + BANCO);
        tvTipoCuenta.setText("Tipo de cuenta: " + TIPO_CUENTA);
        tvNumeroCuenta.setText("N° de cuenta: " + NUMERO_CUENTA);
        tvTitular.setText("Titular: " + TITULAR);
        tvRut.setText("RUT: " + RUT);
        tvCorreoPago.setText("Correo: " + CORREO_PAGO);
    }

    private void enviarWhatsapp() {
        actualizarEstadoCompra();

        String msg = "Hola, acabo de realizar la transferencia del pedido "
                + codigoPedido + " del producto " + producto
                + " por un monto de $" + monto
                + ". Adjunto comprobante.";

        try {
            Uri uri = Uri.parse("https://wa.me/" + TELEFONO_WHATSAPP + "?text=" + Uri.encode(msg));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
            toast("No se pudo abrir WhatsApp");
        }
    }

    private void actualizarEstadoCompra() {
        Map<String, Object> update = new HashMap<>();
        update.put("estado", "comprobante_enviado");
        update.put("ultimaActualizacion", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("compras").child(idCompra).updateChildren(update);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
