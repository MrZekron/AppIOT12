package com.example.appiot12;
// 📦 Pantalla de información del tanque en tiempo real.
// Dashboard IoT donde los sensores hablan y la app escucha 📡💧

// ===== IMPORTS ANDROID =====
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// ===== MPAndroidChart =====
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

// ===== FIREBASE =====
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

// ===== JAVA =====
import java.util.ArrayList;
import java.util.List;

/**
 * 📊 Informacion
 *
 * ¿Qué hace esta pantalla?
 * 👉 Muestra datos del tanque (nombre, capacidad, color)
 * 👉 Escucha sensores en tiempo real desde Firebase
 * 👉 Muestra valores + estados (semáforo)
 * 👉 Grafica pH, conductividad y turbidez
 *
 * Explicado para un niño:
 * 👉 Es una pantalla que te dice si el agua está sana o enferma 💧🙂
 */
public class Informacion extends AppCompatActivity {

    // =====================================================
    // 🛢️ DATOS DEL TANQUE
    // =====================================================
    private TextView txtNombre;
    private TextView txtCapacidad;
    private TextView txtColor;

    // =====================================================
    // 📡 VALORES DE SENSORES
    // =====================================================
    private TextView txtPh;
    private TextView txtConductividad;
    private TextView txtTurbidez;
    private TextView txtUltrasonico;

    // =====================================================
    // 🚦 ESTADOS DE LOS SENSORES
    // =====================================================
    private TextView txtPhEstado;
    private TextView txtCondEstado;
    private TextView txtTurbEstado;
    private TextView txtUltraEstado;

    // =====================================================
    // 📈 GRÁFICO
    // =====================================================
    private LineChart sensorChart;
    private LineData lineData;
    private LineDataSet setPH;
    private LineDataSet setCond;
    private LineDataSet setTurb;

    private static final int MAX_POINTS = 300; // 🧠 Límite de puntos
    private int ejeX = 0; // Simula tiempo

    // =====================================================
    // ☁️ FIREBASE
    // =====================================================
    private DatabaseReference tanqueRef;
    private DatabaseReference dispositivoRef;

    private ValueEventListener tanqueListener;
    private ValueEventListener dispositivoListener;

    private String tanqueId;
    private String idDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion);

        // 🔗 Vincular UI
        inicializarVistas();

        // 📥 Leer datos desde Intent
        leerIntent();

        // 📊 Configurar gráfico
        configurarGrafico();

        // 🔐 Usuario actual
        String uid = obtenerUidUsuario();
        if (uid == null || tanqueId == null) {
            Toast.makeText(this, "Error al cargar información ❌", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🛢️ Referencia al tanque
        tanqueRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .child(tanqueId);

        escucharTanque();

        // 📡 Referencia al dispositivo (si existe)
        if (idDispositivo != null && !idDispositivo.isEmpty()) {
            dispositivoRef = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(uid)
                    .child("dispositivos")
                    .child(idDispositivo);

            escucharDispositivo();
        } else {
            Toast.makeText(this,
                    "Este tanque no tiene dispositivo asociado 📡❌",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // =====================================================
    // 🔗 INICIALIZAR VISTAS
    // =====================================================
    private void inicializarVistas() {
        txtNombre = findViewById(R.id.txtNombre);
        txtCapacidad = findViewById(R.id.txtCapasidad);
        txtColor = findViewById(R.id.txtColor);

        txtPh = findViewById(R.id.txtPh);
        txtConductividad = findViewById(R.id.txtConductividad);
        txtTurbidez = findViewById(R.id.txtTurbidez);
        txtUltrasonico = findViewById(R.id.txtSonico);

        txtPhEstado = findViewById(R.id.txtPhEstado);
        txtCondEstado = findViewById(R.id.txtCondEstado);
        txtTurbEstado = findViewById(R.id.txtTurbEstado);
        txtUltraEstado = findViewById(R.id.txtUltraEstado);

        sensorChart = findViewById(R.id.sensorChart);
    }

    // =====================================================
    // 📥 LEER DATOS DESDE INTENT
    // =====================================================
    private void leerIntent() {

        Intent intent = getIntent();

        tanqueId = firstNonNull(
                intent.getStringExtra("TANQUE_ID"),
                intent.getStringExtra("tanqueId"),
                intent.getStringExtra("tanque_id")
        );

        idDispositivo = intent.getStringExtra("idDispositivo");

        setTextIfNotNull(txtNombre, intent.getStringExtra("tanqueNombre"));
        setTextIfNotNull(txtCapacidad, intent.getStringExtra("tanqueCapacidad"));
        setTextIfNotNull(txtColor, intent.getStringExtra("tanqueColor"));
    }

    // =====================================================
    // 📡 ESCUCHAR DATOS DEL TANQUE
    // =====================================================
    private void escucharTanque() {

        tanqueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                setTextIfNotNull(txtNombre, snap.child("nombre").getValue(String.class));
                setTextIfNotNull(txtCapacidad, snap.child("capacidad").getValue(String.class));
                setTextIfNotNull(txtColor, snap.child("color").getValue(String.class));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        tanqueRef.addValueEventListener(tanqueListener);
    }

    // =====================================================
    // ⚡ ESCUCHAR DISPOSITIVO EN TIEMPO REAL
    // =====================================================
    private void escucharDispositivo() {

        dispositivoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                double ph = leerDouble(snap, "ph");
                double cond = leerDouble(snap, "conductividad");
                double turb = leerDouble(snap, "turbidez");
                double ultra = leerDouble(snap, "ultrasonico");

                // Mostrar valores
                txtPh.setText("pH: " + ph);
                txtConductividad.setText("Conductividad: " + cond);
                txtTurbidez.setText("Turbidez: " + turb);
                txtUltrasonico.setText("Nivel: " + ultra);

                // Semáforo
                actualizarColores(ph, cond, turb, ultra);

                // 📈 Gráfico
                agregarDato(ph, cond, turb);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        dispositivoRef.addValueEventListener(dispositivoListener);
    }

    // =====================================================
    // 📊 CONFIGURAR GRÁFICO
    // =====================================================
    private void configurarGrafico() {

        sensorChart.getDescription().setEnabled(false);
        sensorChart.setNoDataText("Aún no hay lecturas 📭");

        XAxis xAxis = sensorChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        sensorChart.getAxisLeft().setEnabled(false);
        sensorChart.getAxisRight().setEnabled(false);

        Legend legend = sensorChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        setPH = crearDataSet("pH", Color.GREEN);
        setCond = crearDataSet("Conductividad", Color.BLUE);
        setTurb = crearDataSet("Turbidez", Color.MAGENTA);

        lineData = new LineData(setPH, setCond, setTurb);
        sensorChart.setData(lineData);
    }

    private LineDataSet crearDataSet(String label, int color) {
        LineDataSet set = new LineDataSet(new ArrayList<>(), label);
        set.setColor(color);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        return set;
    }

    // =====================================================
    // 📈 AGREGAR DATOS AL GRÁFICO
    // =====================================================
    private void agregarDato(double ph, double cond, double turb) {

        ejeX++;

        lineData.addEntry(new Entry(ejeX, escalar(ph, 14)), 0);
        lineData.addEntry(new Entry(ejeX, escalar(cond, 2000)), 1);
        lineData.addEntry(new Entry(ejeX, escalar(turb, 100)), 2);

        limitar(setPH);
        limitar(setCond);
        limitar(setTurb);

        lineData.notifyDataChanged();
        sensorChart.notifyDataSetChanged();
        sensorChart.invalidate();
    }

    private void limitar(LineDataSet set) {
        while (set.getEntryCount() > MAX_POINTS) {
            set.removeFirst();
        }
    }

    // =====================================================
    // 🚦 SEMÁFORO DE CALIDAD
    // =====================================================
    private void actualizarColores(double ph, double cond, double turb, double ultra) {

        aplicarEstado(txtPh, txtPhEstado, ph, 6.5, 8.5);
        aplicarEstado(txtConductividad, txtCondEstado, cond, 0, 700);
        aplicarEstado(txtTurbidez, txtTurbEstado, turb, 0, 5);
        aplicarEstado(txtUltrasonico, txtUltraEstado, ultra, 60, 100);
    }

    private void aplicarEstado(TextView valor, TextView estado,
                               double v, double min, double max) {

        int color;
        String texto;

        if (v >= min && v <= max) {
            color = Color.GREEN;
            texto = "Normal 👍";
        } else {
            color = Color.RED;
            texto = "Peligro 🔥";
        }

        valor.setTextColor(color);
        estado.setTextColor(color);
        estado.setText(texto);
    }

    // =====================================================
    // 🔧 HELPERS
    // =====================================================
    private String obtenerUidUsuario() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    private void setTextIfNotNull(TextView tv, String text) {
        if (text != null) tv.setText(text);
    }

    private double leerDouble(DataSnapshot snap, String key) {
        try {
            return snap.child(key).getValue(Double.class);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private float escalar(double valor, double max) {
        if (Double.isNaN(valor)) return 0f;
        return (float) ((valor / max) * 100);
    }

    private String firstNonNull(String... valores) {
        for (String v : valores) {
            if (v != null && !v.isEmpty()) return v;
        }
        return null;
    }

    // =====================================================
    // 🧹 LIMPIAR LISTENERS
    // =====================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tanqueRef != null && tanqueListener != null)
            tanqueRef.removeEventListener(tanqueListener);

        if (dispositivoRef != null && dispositivoListener != null)
            dispositivoRef.removeEventListener(dispositivoListener);
    }
}
