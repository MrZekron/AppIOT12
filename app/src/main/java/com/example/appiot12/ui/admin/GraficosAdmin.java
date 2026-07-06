package com.example.appiot12.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appiot12.ui.BaseActivity;
import androidx.core.content.ContextCompat;

import com.example.appiot12.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class GraficosAdmin extends BaseActivity {

    private BarChart barChartUsuarios;
    private BarChart barChartComprasGlobal;
    private HorizontalBarChart barChartComprasPorUsuario;

    private TextView tvTituloUsuarios;
    private TextView tvTituloComprasGlobal;
    private TextView tvTituloComprasPorUsuario;

    private DatabaseReference refUsuarios;
    private DatabaseReference refCompras;

    private int colorPrimary;
    private int colorText;
    private int colorSecondary;
    private int colorSurface;

    private final SimpleDateFormat formatoMes = new SimpleDateFormat("MMM yyyy", new Locale("es", "CL"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_graficos);

        colorPrimary = ContextCompat.getColor(this, R.color.color_primary);
        colorText = ContextCompat.getColor(this, R.color.color_text_primary);
        colorSecondary = ContextCompat.getColor(this, R.color.color_secondary);
        colorSurface = ContextCompat.getColor(this, R.color.color_surface);

        tvTituloUsuarios          = findViewById(R.id.tvTituloUsuarios);
        tvTituloComprasGlobal     = findViewById(R.id.tvTituloComprasGlobal);
        tvTituloComprasPorUsuario = findViewById(R.id.tvTituloComprasPorUsuario);

        barChartUsuarios          = findViewById(R.id.barChartUsuarios);
        barChartComprasGlobal     = findViewById(R.id.barChartComprasGlobal);
        barChartComprasPorUsuario = findViewById(R.id.barChartComprasPorUsuario);

        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        refCompras  = FirebaseDatabase.getInstance().getReference("compras");

        cargarDatosUsuarios();
        cargarDatosCompras();
    }

    private void cargarDatosUsuarios() {
        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Integer> registrosPorMes = new TreeMap<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String rol = snap.child("rol").getValue(String.class);
                    if (!"usuario".equalsIgnoreCase(rol)) continue;

                    Long createdAt = snap.child("createdAt").getValue(Long.class);
                    if (createdAt == null) continue;

                    String mes = formatoMes.format(new Date(createdAt));
                    registrosPorMes.put(mes, registrosPorMes.getOrDefault(mes, 0) + 1);
                }

                if (registrosPorMes.isEmpty()) {
                    tvTituloUsuarios.setText("Usuarios registrados por mes (sin datos aún)");
                    return;
                }

                dibujarBarChart(barChartUsuarios, registrosPorMes, "Usuarios registrados", colorPrimary);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mostrarError("Error al cargar usuarios: " + error.getMessage());
            }
        });
    }

    private void cargarDatosCompras() {
        refCompras.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshotCompras) {
                Map<String, Integer> comprasPorMes = new TreeMap<>();
                Map<String, Integer> comprasPorUid = new LinkedHashMap<>();

                for (DataSnapshot snap : snapshotCompras.getChildren()) {
                    String estado = snap.child("estado").getValue(String.class);
                    if ("cancelado".equalsIgnoreCase(estado)) continue;

                    String uid = snap.child("uidUsuario").getValue(String.class);
                    if (uid == null) continue;

                    Long fecha = snap.child("fecha").getValue(Long.class);
                    if (fecha != null) {
                        String mes = formatoMes.format(new Date(fecha));
                        comprasPorMes.put(mes, comprasPorMes.getOrDefault(mes, 0) + 1);
                    }

                    comprasPorUid.put(uid, comprasPorUid.getOrDefault(uid, 0) + 1);
                }

                if (!comprasPorMes.isEmpty()) {
                    dibujarBarChart(barChartComprasGlobal, comprasPorMes, "Dispositivos comprados", colorSecondary);
                } else {
                    tvTituloComprasGlobal.setText("Compras de dispositivos por mes (sin datos aún)");
                }

                if (!comprasPorUid.isEmpty()) {
                    cargarCorreosParaGrafico(comprasPorUid);
                } else {
                    tvTituloComprasPorUsuario.setText("Compras por usuario (sin datos aún)");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mostrarError("Error al cargar compras: " + error.getMessage());
            }
        });
    }

    private void cargarCorreosParaGrafico(Map<String, Integer> comprasPorUid) {
        Map<String, Integer> comprasPorCorreo = new LinkedHashMap<>();

        int[] total = {comprasPorUid.size()};
        int[] completados = {0};

        for (Map.Entry<String, Integer> entry : comprasPorUid.entrySet()) {
            String uid = entry.getKey();
            int cantCompras = entry.getValue();

            refUsuarios.child(uid).child("correo")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snap) {
                            String correo = snap.getValue(String.class);
                            String etiqueta = (correo != null && !correo.isEmpty())
                                    ? correo
                                    : uid.substring(0, 8) + "...";

                            comprasPorCorreo.put(etiqueta, cantCompras);

                            completados[0]++;
                            if (completados[0] >= total[0]) dibujarBarChartHorizontal(comprasPorCorreo);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            comprasPorCorreo.put(uid.substring(0, 8) + "...", cantCompras);

                            completados[0]++;
                            if (completados[0] >= total[0]) dibujarBarChartHorizontal(comprasPorCorreo);
                        }
                    });
        }
    }

    private void dibujarBarChart(BarChart chart, Map<String, Integer> datos, String etiqueta, int colorBarra) {
        List<String> etiquetasX = new ArrayList<>(datos.keySet());

        List<BarEntry> entradas = new ArrayList<>();
        int index = 0;
        for (Integer valor : datos.values()) {
            entradas.add(new BarEntry(index++, valor));
        }

        BarDataSet dataSet = new BarDataSet(entradas, etiqueta);
        dataSet.setColor(colorBarra);
        dataSet.setValueTextColor(colorText);
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { return String.valueOf((int) value); }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetasX));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(etiquetasX.size());
        xAxis.setLabelRotationAngle(-30f);
        xAxis.setTextColor(colorText);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setTextColor(colorText);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { return String.valueOf((int) value); }
        });

        chart.getAxisRight().setEnabled(false);
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(colorText);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setFitBars(true);
        chart.animateY(800);
        chart.invalidate();
    }

    private void dibujarBarChartHorizontal(Map<String, Integer> datos) {
        List<String> correos = new ArrayList<>(datos.keySet());

        List<BarEntry> entradas = new ArrayList<>();
        int index = 0;
        for (Integer valor : datos.values()) {
            entradas.add(new BarEntry(index++, valor));
        }

        BarDataSet dataSet = new BarDataSet(entradas, "Dispositivos por usuario");
        dataSet.setColor(colorText);
        dataSet.setValueTextColor(colorSurface);
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { return String.valueOf((int) value); }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        XAxis xAxis = barChartComprasPorUsuario.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(correos));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(correos.size());
        xAxis.setTextColor(colorText);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);

        YAxis yAxisLeft = barChartComprasPorUsuario.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setTextColor(colorText);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { return String.valueOf((int) value); }
        });

        barChartComprasPorUsuario.getAxisRight().setEnabled(false);
        barChartComprasPorUsuario.setData(barData);
        barChartComprasPorUsuario.getDescription().setEnabled(false);
        barChartComprasPorUsuario.getLegend().setTextColor(colorText);
        barChartComprasPorUsuario.setBackgroundColor(Color.TRANSPARENT);
        barChartComprasPorUsuario.setFitBars(true);
        barChartComprasPorUsuario.animateX(800);
        barChartComprasPorUsuario.invalidate();
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    public void volver(View view) {
        finish();
    }
}
