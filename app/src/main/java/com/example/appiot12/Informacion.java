package com.example.appiot12; // 📦 Este archivo vive dentro del paquete de la app

import android.content.Intent; // 🚪 Para recibir datos de la otra pantalla
import android.graphics.Color; // 🎨 Para colores en textos y gráficos
import android.os.Bundle; // 🎒 Estado al iniciar pantalla
import android.view.View; // 👆 Para detectar clics
import android.widget.TextView; // 📝 Para mostrar texto al usuario
import android.widget.Toast; // 🍞 Mensajes cortitos emergentes

import androidx.activity.EdgeToEdge; // 📱 Pantalla completa sin bordes
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // ⚠️ Cuadros de diálogo
import androidx.appcompat.app.AppCompatActivity; // 🏛️ Clasica Activity
import androidx.core.graphics.Insets; // 📐 Márgenes de pantalla
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart; // 📊 Gráfico de líneas
import com.github.mikephil.charting.components.Legend; // 🏷 Leyenda del gráfico
import com.github.mikephil.charting.components.XAxis; // 📏 Eje X
import com.github.mikephil.charting.data.Entry; // 🔹 Un puntito del gráfico
import com.github.mikephil.charting.data.LineData; // 📈 Conjunto de datos
import com.github.mikephil.charting.data.LineDataSet; // 📈 Serie de datos
import com.google.firebase.database.DataSnapshot; // 📦 Datos extraídos de Firebase
import com.google.firebase.database.DatabaseError; // 🚫 Error en Firebase
import com.google.firebase.database.DatabaseReference; // 📍 Ruta en Firebase
import com.google.firebase.database.FirebaseDatabase; // 🌐 Base de datos
import com.google.firebase.database.ValueEventListener; // 👂 Escucha datos en tiempo real

import java.util.ArrayList; // 📚 Para listas dinámicas

// ⭐⭐⭐ PANTALLA DE INFORMACIÓN DEL TANQUE ⭐⭐⭐
// Aquí mostramos los datos del tanque, sus sensores y un gráfico en tiempo real 💧📊⚡
public class Informacion extends AppCompatActivity {

    // 📝 Textos para mostrar información del tanque
    private TextView txtNombre, txtCapasidad, txtColor;
    private TextView txtPh, txtConductividad, txtTurbidez, txtUltrasonico;

    // 🛑 Estados de los sensores
    private TextView txtPhEstado, txtCondEstado, txtTurbEstado, txtUltraEstado;

    // 📊 El gráfico de los sensores
    private LineChart sensorChart;

    // 🆔 El ID del tanque a mostrar
    private String tanqueId;

    // 🔗 Referencia a Firebase
    private DatabaseReference tanqueRef;

    // 🎧 Escuchadores en tiempo real
    private ValueEventListener dispositivoListener;
    private ValueEventListener tanqueMetaListener;

    // 📊 Datos del gráfico
    private LineData lineData;
    private LineDataSet setPH, setCond, setTurb;

    // 📏 Máximo de puntos antes de ir borrando
    private static final int MAX_POINTS_PER_SET = 300;
    private int sampleIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // 🎬 Se ejecuta al abrir esta pantalla
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_informacion); // 🎨 Dibujamos la interfaz

        // 📱 Ajustar pantalla para que no choquen las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom); // 🧱 Ponemos márgenes
            return insets;
        });

        // 🔍 Conectamos variables con los campos XML
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

        // 📦 Recibimos los datos del Intent (la otra pantalla nos los envió)
        Intent intent = getIntent();

        // 🆔 El ID puede venir con distintos nombres, revisamos cuál está presente
        tanqueId = firstNonNull(
                intent.getStringExtra("TANQUE_ID"),
                intent.getStringExtra("tanqueId"),
                intent.getStringExtra("tanque_id")
        );

        // Datos del tanque (nombre, capacidad, color)
        String nombreExtra = firstNonNull(
                intent.getStringExtra("tanqueNombre"),
                intent.getStringExtra("nombres"),
                intent.getStringExtra("nombre"));

        String capacidadExtra = firstNonNull(
                intent.getStringExtra("tanqueCapacidad"),
                intent.getStringExtra("capasidad"),
                intent.getStringExtra("capacidad"));

        String colorExtra = firstNonNull(
                intent.getStringExtra("tanqueColor"),
                intent.getStringExtra("color"));

        // ✍️ Mostramos los datos en pantalla
        if (nombreExtra != null) txtNombre.setText(nombreExtra);
        if (capacidadExtra != null) txtCapasidad.setText(capacidadExtra);
        if (colorExtra != null) txtColor.setText(colorExtra);

        // 📊 Configuramos el gráfico
        setupChart();

        // 🚀 Si tenemos un ID, podemos leer datos en tiempo real
        if (tanqueId != null && !tanqueId.isEmpty()) {

            tanqueRef = FirebaseDatabase.getInstance()
                    .getReference("TanquesDeAgua") // ⚠️ OJO: esta es la ruta antigua
                    .child(tanqueId);

            subscribeTanqueMetaRealtime();   // 📡 Escuchar nombre/capacidad/color
            subscribeDispositivoRealtime();  // 📡 Escuchar sensores

        } else {
            Toast.makeText(this, "No se encontró ID de tanque", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------- BOTÓN EDITAR 🔧----------------------
    public void editarTanque(View view) {

        if (tanqueId == null || tanqueId.isEmpty()) {
            Toast.makeText(this, "No se puede editar: falta ID del tanque", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(Informacion.this, Editor.class);

        // ✉️ Mandamos los datos actuales para prellenar el editor
        i.putExtra("tanqueId", tanqueId);
        i.putExtra("tanqueNombre", txtNombre.getText().toString());
        i.putExtra("tanqueCapacidad", txtCapasidad.getText().toString());
        i.putExtra("tanqueColor", txtColor.getText().toString());

        startActivity(i);
    }

    // ---------------------- CONFIGURAR GRÁFICO 📊 ----------------------
    private void setupChart() {

        sensorChart.setNoDataText("Aún no hay lecturas 💤");
        sensorChart.getDescription().setEnabled(false);
        sensorChart.setTouchEnabled(true);
        sensorChart.setDragEnabled(true);
        sensorChart.setScaleEnabled(true);
        sensorChart.setPinchZoom(true);

        // 📏 EJE X
        XAxis x = sensorChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setAxisMinimum(0f);
        x.setAxisMaximum(100f);
        x.setGranularity(5f);
        x.setLabelCount(21, true);

        // 🚫 Ocultamos ejes Y porque no los necesitamos
        sensorChart.getAxisLeft().setEnabled(false);
        sensorChart.getAxisRight().setEnabled(false);

        // 🏷 Leyendas
        Legend legend = sensorChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);

        // 🎨 Creamos datasets vacíos
        setPH = new LineDataSet(new ArrayList<>(), "pH");
        setCond = new LineDataSet(new ArrayList<>(), "Conductividad");
        setTurb = new LineDataSet(new ArrayList<>(), "Turbidez");

        // 🎨 Configuramos cómo se verán esas líneas
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
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Curvitas suaves 😎
        set.setColor(Color.GRAY);
        set.setCircleColor(Color.GRAY);
    }

    // ---------------------- COLORES Y ESTADOS 🔵🟡🔴 ----------------------
    private int getColorStatus(double value, double goodMin, double goodMax, double warnMin, double warnMax) {
        if (value >= goodMin && value <= goodMax) return Color.GREEN;  // 🟢 Perfecto
        if (value >= warnMin && value <= warnMax) return Color.YELLOW; // 🟡 Advertencia
        return Color.RED; // 🔴 Problema
    }

    private void applyColorStatus(TextView txt, int color) {
        txt.setTextColor(color);
    }

    private String getEstadoTexto(String sensor, int color) {
        if (color == Color.GREEN)
            return sensor + ": en nivel óptimo. Parámetros estables 🌱";
        if (color == Color.YELLOW)
            return sensor + ": en advertencia. Revisar pronto ⚠️";
        return sensor + ": en estado crítico. Riesgo alto detectado 🚨";
    }

    // 🔄 Actualizar los colores según los valores
    private void updateSensorColors(double ph, double cond, double turb, double ultra) {

        int colorPh = getColorStatus(ph, 6.5, 8.5, 5.0, 9.0);
        int colorCond = getColorStatus(cond, 0, 700, 701, 1500);
        int colorTurb = getColorStatus(turb, 0, 5, 6, 50);
        int colorUltra = getColorStatus(ultra, 60, 100, 30, 59);

        // 🌈 Aplicamos color a los números
        applyColorStatus(txtPh, colorPh);
        applyColorStatus(txtConductividad, colorCond);
        applyColorStatus(txtTurbidez, colorTurb);
        applyColorStatus(txtUltrasonico, colorUltra);

        // 📄 Y colocamos textos explicativos
        txtPhEstado.setText(getEstadoTexto("pH", colorPh));
        txtPhEstado.setTextColor(colorPh);

        txtCondEstado.setText(getEstadoTexto("Conductividad", colorCond));
        txtCondEstado.setTextColor(colorCond);

        txtTurbEstado.setText(getEstadoTexto("Turbidez", colorTurb));
        txtTurbEstado.setTextColor(colorTurb);

        txtUltraEstado.setText(getEstadoTexto("Nivel del tanque", colorUltra));
        txtUltraEstado.setTextColor(colorUltra);
    }

    // ---------------------- LEER METADATOS DEL TANQUE 🔍 ----------------------
    private void subscribeTanqueMetaRealtime() {
        if (tanqueRef == null) return;

        tanqueMetaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String nombre = snapshot.child("nombre").getValue(String.class);
                String capacidad = snapshot.child("capacidad").getValue(String.class);
                String color = snapshot.child("color").getValue(String.class);

                if (nombre != null) txtNombre.setText(nombre);
                if (capacidad != null) txtCapasidad.setText(capacidad);
                if (color != null) txtColor.setText(color);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        // 🎧 Escuchamos cambios en tiempo real
        tanqueRef.addValueEventListener(tanqueMetaListener);
    }

    // ---------------------- LEER SENSORES EN TIEMPO REAL 📡 ----------------------
    private void subscribeDispositivoRealtime() {
        if (tanqueRef == null) return;

        DatabaseReference dispositivoRef = tanqueRef.child("dispositivo");

        dispositivoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double ph = readDouble(snapshot, "ph");
                double cond = readDouble(snapshot, "conductividad");
                double turb = readDouble(snapshot, "turbidez");
                double ultra = readDouble(snapshot, "ultrasonico");

                // 📄 Mostramos valores
                txtPh.setText("pH: " + ph);
                txtConductividad.setText("Conductividad: " + cond);
                txtTurbidez.setText("Turbidez: " + turb);
                txtUltrasonico.setText("Ultrasonico: " + ultra);

                updateSensorColors(ph, cond, turb, ultra);

                // 📊 Añadir al gráfico
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        dispositivoRef.addValueEventListener(dispositivoListener);
    }

    // ---------------------- HELPERS 🧰 ----------------------
    private String firstNonNull(String... arr) {
        for (String s : arr) if (s != null && !s.isEmpty()) return s;
        return null;
    }

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

    private void prune(LineDataSet set) {
        while (set.getEntryCount() > MAX_POINTS_PER_SET) {
            set.removeFirst();
        }
    }

    private float scalePh(double v) {
        return Float.isNaN((float) v) ? Float.NaN : (float) Math.min(100, (v / 14.0) * 100);
    }

    private float scaleCond(double v) {
        return Float.isNaN((float) v) ? Float.NaN : (float) Math.min(100, (v / 2000.0) * 100);
    }

    private float scaleTurb(double v) {
        return Float.isNaN((float) v) ? Float.NaN : (float) Math.min(100, (v / 100.0) * 100);
    }

    @Override
    protected void onDestroy() { // 🧹 Cuando se cierra la pantalla…
        super.onDestroy();

        // 🧽 Eliminamos escuchadores para no dejar procesos colgando
        if (tanqueRef != null) {
            if (dispositivoListener != null) {
                tanqueRef.child("dispositivo").removeEventListener(dispositivoListener);
            }
            if (tanqueMetaListener != null) {
                tanqueRef.removeEventListener(tanqueMetaListener);
            }
        }
    }
}
