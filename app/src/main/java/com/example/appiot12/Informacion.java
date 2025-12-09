package com.example.appiot12;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Informacion extends AppCompatActivity {

    // Información del tanque
    private TextView txtNombre, txtCapasidad, txtColor;

    // Sensores
    private TextView txtPh, txtConductividad, txtTurbidez, txtUltrasonico;

    // Estados
    private TextView txtPhEstado, txtCondEstado, txtTurbEstado, txtUltraEstado;

    // Gráfico
    private LineChart sensorChart;
    private LineData lineData;
    private LineDataSet setPH, setCond, setTurb;
    private static final int MAX_POINTS_PER_SET = 300;
    private int sampleIndex = 0;

    // Firebase
    private String tanqueId;
    private String idDispositivo;

    private DatabaseReference tanqueRef;
    private DatabaseReference dispositivoRef;

    private ValueEventListener tanqueListener;
    private ValueEventListener dispositivoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_informacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // Enlazar vistas
        txtNombre = findViewById(R.id.txtNombre);
        txtCapasidad = findViewById(R.id.txtCapasidad);
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

        // Recibir datos del Intent
        Intent intent = getIntent();

        tanqueId = firstNonNull(
                intent.getStringExtra("TANQUE_ID"),
                intent.getStringExtra("tanqueId"),
                intent.getStringExtra("tanque_id")
        );

        idDispositivo = intent.getStringExtra("idDispositivo");

        String nombreExtra = firstNonNull(intent.getStringExtra("tanqueNombre"));
        String capacidadExtra = firstNonNull(intent.getStringExtra("tanqueCapacidad"));
        String colorExtra = firstNonNull(intent.getStringExtra("tanqueColor"));

        if (nombreExtra != null) txtNombre.setText(nombreExtra);
        if (capacidadExtra != null) txtCapasidad.setText(capacidadExtra);
        if (colorExtra != null) txtColor.setText(colorExtra);

        // Configurar gráfica
        setupChart();

        // Validar tanqueId
        if (tanqueId == null || tanqueId.isEmpty()) {
            Toast.makeText(this, "No se encontró ID del tanque.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Rutas reales de Firebase (nuevo modelo)
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tanqueRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .child(tanqueId);

        if (idDispositivo != null && !idDispositivo.isEmpty()) {
            dispositivoRef = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(uid)
                    .child("dispositivos")
                    .child(idDispositivo);

            subscribeDispositivoRealtime();
        } else {
            Toast.makeText(this, "Este tanque no tiene dispositivo asociado.", Toast.LENGTH_SHORT).show();
        }

        subscribeTanqueMetaRealtime();
    }

    // ===================== META DEL TANQUE =======================
    private void subscribeTanqueMetaRealtime() {

        tanqueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                if (snap.child("nombre").exists())
                    txtNombre.setText(snap.child("nombre").getValue(String.class));

                if (snap.child("capacidad").exists())
                    txtCapasidad.setText(snap.child("capacidad").getValue(String.class));

                if (snap.child("color").exists())
                    txtColor.setText(snap.child("color").getValue(String.class));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        tanqueRef.addValueEventListener(tanqueListener);
    }


    // ===================== SENSORES EN TIEMPO REAL =======================
    private void subscribeDispositivoRealtime() {

        dispositivoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                double ph = readDouble(snap, "ph");
                double cond = readDouble(snap, "conductividad");
                double turb = readDouble(snap, "turbidez");
                double ultra = readDouble(snap, "ultrasonico");

                txtPh.setText("pH: " + ph);
                txtConductividad.setText("Conductividad: " + cond);
                txtTurbidez.setText("Turbidez: " + turb);
                txtUltrasonico.setText("Ultrasonico: " + ultra);

                updateSensorColors(ph, cond, turb, ultra);

                // Agregar al gráfico
                float y = sampleIndex++;

                safeAddEntry(lineData, 0, new Entry(scalePh(ph), y));
                safeAddEntry(lineData, 1, new Entry(scaleCond(cond), y));
                safeAddEntry(lineData, 2, new Entry(scaleTurb(turb), y));

                prune(setPH);
                prune(setCond);
                prune(setTurb);

                lineData.notifyDataChanged();
                sensorChart.notifyDataSetChanged();
                sensorChart.invalidate();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        dispositivoRef.addValueEventListener(dispositivoListener);
    }


    // ===================== GRÁFICO =======================
    private void setupChart() {

        sensorChart.setNoDataText("Aún no hay lecturas");
        sensorChart.getDescription().setEnabled(false);

        XAxis x = sensorChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(5f);
        x.setLabelCount(20, true);

        sensorChart.getAxisLeft().setEnabled(false);
        sensorChart.getAxisRight().setEnabled(false);

        Legend legend = sensorChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        setPH = new LineDataSet(new ArrayList<>(), "pH");
        setCond = new LineDataSet(new ArrayList<>(), "Conductividad");
        setTurb = new LineDataSet(new ArrayList<>(), "Turbidez");

        configureDataSet(setPH);
        configureDataSet(setCond);
        configureDataSet(setTurb);

        lineData = new LineData();
        lineData.addDataSet(setPH);
        lineData.addDataSet(setCond);
        lineData.addDataSet(setTurb);

        sensorChart.setData(lineData);
    }

    private void configureDataSet(LineDataSet set) {
        set.setLineWidth(2f);
        set.setDrawCircles(true);
        set.setCircleRadius(3f);
        set.setDrawValues(false);
        set.setColor(Color.GRAY);
        set.setCircleColor(Color.GRAY);
    }

    private void prune(LineDataSet set) {
        while (set.getEntryCount() > MAX_POINTS_PER_SET)
            set.removeFirst();
    }


    // ===================== ESTADOS DE COLOR =======================
    private void updateSensorColors(double ph, double cond, double turb, double ultra) {

        int colorPh = getColorStatus(ph, 6.5, 8.5, 5.0, 9.0);
        int colorCond = getColorStatus(cond, 0, 700, 701, 1500);
        int colorTurb = getColorStatus(turb, 0, 5, 6, 50);
        int colorUltra = getColorStatus(ultra, 60, 100, 30, 59);

        txtPh.setTextColor(colorPh);
        txtConductividad.setTextColor(colorCond);
        txtTurbidez.setTextColor(colorTurb);
        txtUltrasonico.setTextColor(colorUltra);

        txtPhEstado.setText(getEstadoTexto("pH", colorPh));
        txtPhEstado.setTextColor(colorPh);

        txtCondEstado.setText(getEstadoTexto("Conductividad", colorCond));
        txtCondEstado.setTextColor(colorCond);

        txtTurbEstado.setText(getEstadoTexto("Turbidez", colorTurb));
        txtTurbEstado.setTextColor(colorTurb);

        txtUltraEstado.setText(getEstadoTexto("Nivel del tanque", colorUltra));
        txtUltraEstado.setTextColor(colorUltra);
    }

    private int getColorStatus(double v, double goodMin, double goodMax, double warnMin, double warnMax) {
        if (v >= goodMin && v <= goodMax) return Color.GREEN;
        if (v >= warnMin && v <= warnMax) return Color.YELLOW;
        return Color.RED;
    }

    private String getEstadoTexto(String sensor, int color) {
        if (color == Color.GREEN) return sensor + " en nivel óptimo";
        if (color == Color.YELLOW) return sensor + " en advertencia";
        return sensor + " en nivel crítico";
    }


    // ===================== HELPERS =======================
    private double readDouble(DataSnapshot snap, String key) {
        if (!snap.hasChild(key)) return Double.NaN;
        try {
            return Double.parseDouble(String.valueOf(snap.child(key).getValue()));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private void safeAddEntry(LineData data, int index, Entry e) {
        if (data.getDataSetCount() <= index) return;
        data.addEntry(e, index);
    }

    private String firstNonNull(String... arr) {
        for (String s : arr)
            if (s != null && !s.isEmpty())
                return s;
        return null;
    }

    private float scalePh(double v) { return Float.isNaN((float)v) ? Float.NaN : (float)((v / 14.0) * 100); }
    private float scaleCond(double v) { return Float.isNaN((float)v) ? Float.NaN : (float)((v / 2000.0) * 100); }
    private float scaleTurb(double v) { return Float.isNaN((float)v) ? Float.NaN : (float)((v / 100.0) * 100); }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tanqueRef != null && tanqueListener != null)
            tanqueRef.removeEventListener(tanqueListener);

        if (dispositivoRef != null && dispositivoListener != null)
            dispositivoRef.removeEventListener(dispositivoListener);
    }
}
