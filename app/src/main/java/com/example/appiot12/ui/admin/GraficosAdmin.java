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
import com.github.mikephil.charting.components.Legend;
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
import java.util.Calendar;
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

    // Business metrics views
    private BarChart barChartIngresosCostos;
    private BarChart barChartUtilidad;
    private BarChart barChartMantencion;
    private TextView tvKpiClientes;
    private TextView tvKpiIngresos;
    private TextView tvKpiMantencion;
    private TextView tvKpiUtilidad;

    private DatabaseReference refUsuarios;
    private DatabaseReference refCompras;
    private DatabaseReference refPagos;

    private static final int COLOR_GREEN  = Color.parseColor("#2E7D32");
    private static final int COLOR_ORANGE = Color.parseColor("#E65100");

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

        tvKpiClientes   = findViewById(R.id.tvKpiClientes);
        tvKpiIngresos   = findViewById(R.id.tvKpiIngresos);
        tvKpiMantencion = findViewById(R.id.tvKpiMantencion);
        tvKpiUtilidad   = findViewById(R.id.tvKpiUtilidad);

        barChartIngresosCostos = findViewById(R.id.barChartIngresosCostos);
        barChartUtilidad       = findViewById(R.id.barChartUtilidad);
        barChartMantencion     = findViewById(R.id.barChartMantencion);

        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        refCompras  = FirebaseDatabase.getInstance().getReference("compras");
        refPagos    = FirebaseDatabase.getInstance().getReference("pagos");

        cargarDatosUsuarios();
        cargarDatosCompras();
        cargarMetricasNegocio();
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
                    tvTituloUsuarios.setText("Usuarios registrados por mes (ejemplo)");
                    registrosPorMes = generarMesesDemo(new int[]{1, 3, 2, 5, 4, 2});
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
                    tvTituloComprasGlobal.setText("Compras de dispositivos por mes (ejemplo)");
                    dibujarBarChart(barChartComprasGlobal, generarMesesDemo(new int[]{0, 1, 2, 1, 3, 1}), "Dispositivos comprados", colorSecondary);
                }

                if (!comprasPorUid.isEmpty()) {
                    cargarCorreosParaGrafico(comprasPorUid);
                } else {
                    tvTituloComprasPorUsuario.setText("Compras por usuario (ejemplo)");
                    Map<String, Integer> demoUsuarios = new LinkedHashMap<>();
                    demoUsuarios.put("cliente1@mail.com", 3);
                    demoUsuarios.put("cliente2@mail.com", 1);
                    demoUsuarios.put("cliente3@mail.com", 2);
                    dibujarBarChartHorizontal(demoUsuarios);
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

    private Map<String, Integer> generarMesesDemo(int[] valores) {
        Map<String, Integer> demo = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        int n = valores.length;
        for (int i = n - 1; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.MONTH, -i);
            demo.put(formatoMes.format(c.getTime()), valores[n - 1 - i]);
        }
        return demo;
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    public void volver(View view) {
        finish();
    }

    // ── Business metrics ────────────────────────────────────────────────────────

    private void cargarMetricasNegocio() {
        refPagos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Map<String, Long>    ingresosPorMes   = new TreeMap<>();
                Map<String, Integer> mantencionPorMes = new TreeMap<>();
                long totalIngresos = 0;
                int  totalMantencion = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String estado = snap.child("estado").getValue(String.class);
                    if (!"aprobado".equalsIgnoreCase(estado)) continue;

                    Long monto = snap.child("monto").getValue(Long.class);
                    Long fecha = snap.child("fechaPago").getValue(Long.class);
                    String tipo = snap.child("tipoPago").getValue(String.class);
                    if (monto == null || fecha == null) continue;

                    String mes = formatoMes.format(new Date(fecha));
                    ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, 0L) + monto);
                    totalIngresos += monto;

                    if ("mantenimiento".equalsIgnoreCase(tipo)) {
                        mantencionPorMes.put(mes, mantencionPorMes.getOrDefault(mes, 0) + 1);
                        totalMantencion++;
                    }
                }

                if (ingresosPorMes.isEmpty()) {
                    mostrarMetricasDemo();
                    return;
                }

                Map<String, Long> costosPorMes = new TreeMap<>();
                Map<String, Long> utilidadPorMes = new TreeMap<>();
                for (Map.Entry<String, Long> e : ingresosPorMes.entrySet()) {
                    long costo = (long) (e.getValue() * 0.33);
                    costosPorMes.put(e.getKey(), costo);
                    utilidadPorMes.put(e.getKey(), e.getValue() - costo);
                }

                final long fTotalIngresos   = totalIngresos;
                final int  fTotalMantencion = totalMantencion;
                final long fTotalUtilidad   = fTotalIngresos - (long) (fTotalIngresos * 0.33);

                refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot us) {
                        int clientes = 0;
                        for (DataSnapshot u : us.getChildren()) {
                            String rol = u.child("rol").getValue(String.class);
                            if ("usuario".equalsIgnoreCase(rol)) clientes++;
                        }
                        actualizarKPIs(clientes, fTotalIngresos, fTotalMantencion, fTotalUtilidad);
                    }
                    @Override public void onCancelled(DatabaseError e) {
                        actualizarKPIs(0, fTotalIngresos, fTotalMantencion, fTotalUtilidad);
                    }
                });

                dibujarGraficoIngresosCostos(ingresosPorMes, costosPorMes);
                dibujarGraficoUtilidad(utilidadPorMes);
                dibujarGraficoMantencion(mantencionPorMes);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mostrarMetricasDemo();
            }
        });
    }

    private void mostrarMetricasDemo() {
        // Simulated 6-month growth curve (values in CLP thousands)
        String[] meses = {"Feb 2026", "Mar 2026", "Abr 2026", "May 2026", "Jun 2026", "Jul 2026"};
        long[]   ingresosDemo   = {1300, 2100, 3050, 4200, 5450, 7200};
        int[]    mantencionDemo = {0, 1, 1, 2, 2, 3};

        Map<String, Long>    ingresosPorMes   = new LinkedHashMap<>();
        Map<String, Long>    costosPorMes     = new LinkedHashMap<>();
        Map<String, Long>    utilidadPorMes   = new LinkedHashMap<>();
        Map<String, Integer> mantencionPorMes = new LinkedHashMap<>();

        long totalIngresos = 0;
        int  totalMantencion = 0;

        for (int i = 0; i < meses.length; i++) {
            long ing   = ingresosDemo[i];
            long costo = (long) (ing * 0.33);
            ingresosPorMes.put(meses[i], ing);
            costosPorMes.put(meses[i], costo);
            utilidadPorMes.put(meses[i], ing - costo);
            mantencionPorMes.put(meses[i], mantencionDemo[i]);
            totalIngresos   += ing;
            totalMantencion += mantencionDemo[i];
        }

        long totalUtilidad = totalIngresos - (long) (totalIngresos * 0.33);

        actualizarKPIs(10, totalIngresos * 1000L, totalMantencion, totalUtilidad * 1000L);
        dibujarGraficoIngresosCostos(ingresosPorMes, costosPorMes);
        dibujarGraficoUtilidad(utilidadPorMes);
        dibujarGraficoMantencion(mantencionPorMes);
    }

    private void actualizarKPIs(int clientes, long ingresos, int mantencion, long utilidad) {
        tvKpiClientes.setText(String.valueOf(clientes));
        tvKpiIngresos.setText(formatCLP(ingresos));
        tvKpiMantencion.setText(String.valueOf(mantencion));
        tvKpiUtilidad.setText(formatCLP(utilidad));
    }

    private String formatCLP(long valor) {
        if (valor >= 1_000_000) return String.format(new Locale("es", "CL"), "$%.1fM", valor / 1_000_000.0);
        if (valor >= 1_000)    return String.format(new Locale("es", "CL"), "$%.0fK", valor / 1_000.0);
        return "$" + valor;
    }

    private void dibujarGraficoIngresosCostos(Map<String, Long> ingresosPorMes,
                                               Map<String, Long> costosPorMes) {
        List<String> etiquetas = new ArrayList<>(ingresosPorMes.keySet());
        List<BarEntry> ingresosEntries = new ArrayList<>();
        List<BarEntry> costosEntries   = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Long> e : ingresosPorMes.entrySet()) {
            ingresosEntries.add(new BarEntry(i, e.getValue()));
            Long costo = costosPorMes.get(e.getKey());
            costosEntries.add(new BarEntry(i, costo != null ? costo : 0));
            i++;
        }

        BarDataSet setIngresos = new BarDataSet(ingresosEntries, "Ingresos");
        setIngresos.setColor(colorPrimary);
        setIngresos.setValueTextColor(colorText);
        setIngresos.setValueTextSize(9f);
        setIngresos.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return v >= 1000 ? String.format("%.0fK", v / 1000) : String.valueOf((int) v);
            }
        });

        BarDataSet setCostos = new BarDataSet(costosEntries, "Costos");
        setCostos.setColor(COLOR_ORANGE);
        setCostos.setValueTextColor(colorText);
        setCostos.setValueTextSize(9f);
        setCostos.setValueFormatter(setIngresos.getValueFormatter());

        float groupSpace = 0.3f;
        float barSpace   = 0.05f;
        float barWidth   = 0.3f;

        BarData barData = new BarData(setIngresos, setCostos);
        barData.setBarWidth(barWidth);

        XAxis xAxis = barChartIngresosCostos.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(etiquetas.size());
        xAxis.setLabelRotationAngle(-30f);
        xAxis.setTextColor(colorText);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(true);

        YAxis yAxisLeft = barChartIngresosCostos.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setTextColor(colorText);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return v >= 1000 ? String.format("%.0fK", v / 1000) : String.valueOf((int) v);
            }
        });

        barChartIngresosCostos.getAxisRight().setEnabled(false);
        barChartIngresosCostos.setData(barData);
        barChartIngresosCostos.groupBars(0f, groupSpace, barSpace);
        barChartIngresosCostos.getDescription().setEnabled(false);
        barChartIngresosCostos.getLegend().setTextColor(colorText);
        barChartIngresosCostos.getLegend().setForm(Legend.LegendForm.SQUARE);
        barChartIngresosCostos.setBackgroundColor(Color.TRANSPARENT);
        barChartIngresosCostos.setFitBars(true);
        barChartIngresosCostos.animateY(900);
        barChartIngresosCostos.invalidate();
    }

    private void dibujarGraficoUtilidad(Map<String, Long> utilidadPorMes) {
        List<String>   etiquetas = new ArrayList<>(utilidadPorMes.keySet());
        List<BarEntry> entradas  = new ArrayList<>();
        int i = 0;
        for (Long valor : utilidadPorMes.values()) {
            entradas.add(new BarEntry(i++, valor));
        }

        BarDataSet dataSet = new BarDataSet(entradas, "Utilidad Neta");
        dataSet.setColor(COLOR_GREEN);
        dataSet.setValueTextColor(colorText);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return v >= 1000 ? String.format("%.0fK", v / 1000) : String.valueOf((int) v);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        XAxis xAxis = barChartUtilidad.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(etiquetas.size());
        xAxis.setLabelRotationAngle(-30f);
        xAxis.setTextColor(colorText);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = barChartUtilidad.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setTextColor(colorText);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) {
                return v >= 1000 ? String.format("%.0fK", v / 1000) : String.valueOf((int) v);
            }
        });

        barChartUtilidad.getAxisRight().setEnabled(false);
        barChartUtilidad.setData(barData);
        barChartUtilidad.getDescription().setEnabled(false);
        barChartUtilidad.getLegend().setTextColor(colorText);
        barChartUtilidad.setBackgroundColor(Color.TRANSPARENT);
        barChartUtilidad.setFitBars(true);
        barChartUtilidad.animateY(900);
        barChartUtilidad.invalidate();
    }

    private void dibujarGraficoMantencion(Map<String, Integer> mantencionPorMes) {
        List<String>   etiquetas = new ArrayList<>(mantencionPorMes.keySet());
        List<BarEntry> entradas  = new ArrayList<>();
        int i = 0;
        for (Integer valor : mantencionPorMes.values()) {
            entradas.add(new BarEntry(i++, valor));
        }

        BarDataSet dataSet = new BarDataSet(entradas, "Dispositivos en mantencion");
        dataSet.setColor(COLOR_ORANGE);
        dataSet.setValueTextColor(colorText);
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return String.valueOf((int) v); }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        XAxis xAxis = barChartMantencion.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetas));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(etiquetas.size());
        xAxis.setLabelRotationAngle(-30f);
        xAxis.setTextColor(colorText);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = barChartMantencion.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setTextColor(colorText);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float v) { return String.valueOf((int) v); }
        });

        barChartMantencion.getAxisRight().setEnabled(false);
        barChartMantencion.setData(barData);
        barChartMantencion.getDescription().setEnabled(false);
        barChartMantencion.getLegend().setTextColor(colorText);
        barChartMantencion.setBackgroundColor(Color.TRANSPARENT);
        barChartMantencion.setFitBars(true);
        barChartMantencion.animateY(900);
        barChartMantencion.invalidate();
    }
}
