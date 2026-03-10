package com.example.appiot12;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;

public class Informacion extends AppCompatActivity {

    private static final int COLOR_OK = Color.parseColor("#2E7D32");
    private static final int COLOR_DANGER = Color.parseColor("#C62828");

    private TextView txtNombre, txtCapacidad, txtColor;
    private TextView txtPh, txtConductividad, txtTurbidez, txtUltrasonico;
    private TextView txtPhEstado, txtCondEstado, txtTurbEstado, txtUltraEstado;

    private LineChart chart;
    private LineData data;
    private LineDataSet setPH, setCond, setTurb;

    private DatabaseReference tanqueRef, dispositivoRef;
    private ValueEventListener dispositivoListener;

    private String tanqueId, idDispositivo;
    private boolean snapshotHoyGuardado = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_informacion);

        inicializarVistas();
        leerIntent();
        configurarGrafico();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) finish();

        tanqueRef = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .child(tanqueId);

        if (idDispositivo != null) {
            dispositivoRef = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(uid)
                    .child("dispositivos")
                    .child(idDispositivo);
            escucharDispositivo();
        }
    }

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
        chart = findViewById(R.id.sensorChart);
    }

    private void leerIntent() {
        Intent i = getIntent();
        tanqueId = i.getStringExtra("TANQUE_ID");
        idDispositivo = i.getStringExtra("idDispositivo");
        txtNombre.setText(i.getStringExtra("tanqueNombre"));
        txtCapacidad.setText(i.getStringExtra("tanqueCapacidad"));
        txtColor.setText(i.getStringExtra("tanqueColor"));
    }

    private void configurarGrafico() {
        chart.getDescription().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        setPH = crearSet("pH", COLOR_OK);
        setCond = crearSet("Conductividad", Color.BLUE);
        setTurb = crearSet("Turbidez", Color.MAGENTA);

        data = new LineData(setPH, setCond, setTurb);
        chart.setData(data);
    }

    private LineDataSet crearSet(String label, int color) {
        LineDataSet s = new LineDataSet(new ArrayList<>(), label);
        s.setColor(color);
        s.setDrawCircles(false);
        s.setDrawValues(false);
        return s;
    }

    private void escucharDispositivo() {
        dispositivoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {

                double ph = leer(s, "ph");
                double cond = leer(s, "conductividad");
                double turb = leer(s, "turbidez");
                double ultra = leer(s, "ultrasonico");

                txtPh.setText("pH: " + ph);
                txtConductividad.setText("Conductividad: " + cond);
                txtTurbidez.setText("Turbidez: " + turb);
                txtUltrasonico.setText("Nivel: " + ultra);

                estado(txtPh, txtPhEstado, ph, 6.5, 8.5);
                estado(txtConductividad, txtCondEstado, cond, 0, 700);
                estado(txtTurbidez, txtTurbEstado, turb, 0, 5);
                estado(txtUltrasonico, txtUltraEstado, ultra, 60, 100);

                if (!snapshotHoyGuardado) {
                    snapshotHoyGuardado = true;
                    HistorialService.registrarSensorDiario(
                            tanqueId, ph, cond, turb, ultra
                    );
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };

        dispositivoRef.addValueEventListener(dispositivoListener);
    }

    private void estado(TextView v, TextView e, double val, double min, double max) {
        if (val >= min && val <= max) {
            v.setTextColor(COLOR_OK);
            e.setTextColor(COLOR_OK);
            e.setText("Normal");
        } else {
            v.setTextColor(COLOR_DANGER);
            e.setTextColor(COLOR_DANGER);
            e.setText("Peligro");
        }
    }

    private double leer(DataSnapshot s, String k) {
        Double v = s.child(k).getValue(Double.class);
        return v != null ? v : 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dispositivoRef != null && dispositivoListener != null)
            dispositivoRef.removeEventListener(dispositivoListener);
    }
}
