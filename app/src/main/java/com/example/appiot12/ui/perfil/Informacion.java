package com.example.appiot12.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import com.example.appiot12.ui.BaseActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
import com.example.appiot12.service.AlertaService;
import com.example.appiot12.ui.tanque.Editor;
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
import java.util.Locale;

public class Informacion extends BaseActivity {

    private int colorOk;
    private int colorAlerta;
    private int colorPeligro;

    private TextView txtNombre;
    private TextView txtCapasidad;
    private TextView txtColor;
    private TextView txtDireccion;

    private TextView txtPh;
    private TextView txtConductividad;
    private TextView txtTurbidez;
    private TextView txtUltrasonico;

    private TextView txtPhEstado;
    private TextView txtCondEstado;
    private TextView txtTurbEstado;
    private TextView txtUltraEstado;

    private LineChart sensorChart;
    private LineData lineData;
    private LineDataSet setPH;
    private LineDataSet setCond;
    private LineDataSet setTurb;

    private static final int MAX_POINTS_PER_SET = 300;
    private int sampleIndex = 0;

    private DatabaseReference tanqueRef;
    private DatabaseReference dispositivoRef;

    private ValueEventListener tanqueListener;
    private ValueEventListener dispositivoListener;

    private String tanqueId;
    private String idDispositivo;

    private String tanqueNombre;
    private String tanqueCapacidad;
    private String tanqueColor;
    private String tanqueDireccion;

    private boolean mantencionTanque;
    private boolean mantencionDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.cliente_informacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        inicializarVistas();

        colorOk = ContextCompat.getColor(this, R.color.color_success);
        colorAlerta = ContextCompat.getColor(this, R.color.color_warning);
        colorPeligro = ContextCompat.getColor(this, R.color.color_error);

        leerIntent();
        configurarGrafico();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (tanqueId == null || tanqueId.trim().isEmpty()) {
            Toast.makeText(this, "No se encontró el ID del tanque.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        tanqueRef = FirebaseDatabase.getInstance()
                .getReference("usuarios").child(uid).child("tanques").child(tanqueId);

        if (idDispositivo != null && !idDispositivo.trim().isEmpty()) {
            dispositivoRef = FirebaseDatabase.getInstance()
                    .getReference("lecturas_actuales").child(idDispositivo);
            suscribirseDispositivoTiempoReal();
        } else {
            mostrarSinDispositivo();
        }

        suscribirseMetaTanque();
    }

    private void inicializarVistas() {
        txtNombre = findViewById(R.id.txtNombre);
        txtCapasidad = findViewById(R.id.txtCapasidad);
        txtColor = findViewById(R.id.txtColor);
        txtDireccion = findViewById(R.id.txtDireccion);

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

    private void leerIntent() {
        Intent intent = getIntent();

        tanqueId = primerTextoValido(
                intent.getStringExtra("tanqueId"),
                intent.getStringExtra("TANQUE_ID"),
                intent.getStringExtra("tanque_id")
        );

        idDispositivo = intent.getStringExtra("idDispositivo");

        mantencionTanque = intent.getBooleanExtra("mantencionTanque", false);
        mantencionDispositivo = intent.getBooleanExtra("mantencionDispositivo", false);

        tanqueNombre = valorSeguro(intent.getStringExtra("tanqueNombre"));
        tanqueCapacidad = valorSeguro(intent.getStringExtra("tanqueCapacidad"));
        tanqueColor = valorSeguro(intent.getStringExtra("tanqueColor"));
        tanqueDireccion = valorSeguro(intent.getStringExtra("tanqueDireccion"));

        txtNombre.setText(tanqueNombre);
        txtCapasidad.setText(tanqueCapacidad);
        txtDireccion.setText(tanqueDireccion.isEmpty() ? "Sin dirección registrada" : tanqueDireccion);
        actualizarTextoMantencion(tanqueColor);
    }

    private void actualizarTextoMantencion(String color) {
        String textoColor = String.format(
                Locale.getDefault(),
                "%s | Mant. tanque: %s | Mant. dispositivo: %s",
                color,
                mantencionTanque ? "Pendiente" : "Al día",
                mantencionDispositivo ? "Pendiente" : "Al día"
        );
        txtColor.setText(textoColor);
        txtColor.setTextColor((mantencionTanque || mantencionDispositivo) ? colorPeligro : colorOk);
    }

    private void suscribirseMetaTanque() {
        tanqueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String nombre = snap.child("nombre").getValue(String.class);
                String capacidad = snap.child("capacidad").getValue(String.class);
                String color = snap.child("color").getValue(String.class);
                String direccion = snap.child("direccion").getValue(String.class);

                Boolean mantTanqueDb = snap.child("mantencionTanque").getValue(Boolean.class);
                Boolean mantDispDb = snap.child("mantencionDispositivo").getValue(Boolean.class);

                if (nombre != null) txtNombre.setText(nombre);
                if (capacidad != null) txtCapasidad.setText(capacidad);
                if (mantTanqueDb != null) mantencionTanque = mantTanqueDb;
                if (mantDispDb != null) mantencionDispositivo = mantDispDb;
                if (direccion != null && !direccion.isEmpty()) {
                    txtDireccion.setText(direccion);
                }

                actualizarTextoMantencion(valorSeguro(color));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Informacion.this, "Error al leer datos del tanque.", Toast.LENGTH_SHORT).show();
            }
        };

        tanqueRef.addValueEventListener(tanqueListener);
    }

    private void suscribirseDispositivoTiempoReal() {
        dispositivoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                double ph = leerDouble(snap, "ph");
                double conductividad = leerDouble(snap, "conductividad");
                double turbidez = leerDouble(snap, "turbidez");
                double ultrasonico = leerDouble(snap, "nivelCm");
                double nivelPorcentaje = leerDouble(snap, "nivelPorcentaje");

                txtPh.setText(String.format(Locale.getDefault(), "pH: %.2f", ph));
                txtConductividad.setText(String.format(Locale.getDefault(), "Conductividad: %.2f", conductividad));
                txtTurbidez.setText(String.format(Locale.getDefault(), "Turbidez: %.2f", turbidez));
                txtUltrasonico.setText(String.format(Locale.getDefault(), "Ultrasonico: %.2f", ultrasonico));

                actualizarEstadosVisuales(ph, conductividad, turbidez, ultrasonico);
                agregarPuntosGrafico(ph, conductividad, turbidez);

                String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                AlertaService.evaluarLectura(
                        Informacion.this, uid, idDispositivo,
                        ph, conductividad, turbidez, nivelPorcentaje);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Informacion.this, "Error al leer datos del dispositivo.", Toast.LENGTH_SHORT).show();
            }
        };

        dispositivoRef.addValueEventListener(dispositivoListener);
    }

    private void configurarGrafico() {
        sensorChart.setNoDataText("Aún no hay lecturas");
        sensorChart.getDescription().setEnabled(false);

        XAxis ejeX = sensorChart.getXAxis();
        ejeX.setPosition(XAxis.XAxisPosition.BOTTOM);
        ejeX.setGranularity(1f);

        sensorChart.getAxisLeft().setEnabled(false);
        sensorChart.getAxisRight().setEnabled(false);

        Legend legend = sensorChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        setPH = new LineDataSet(new ArrayList<>(), "pH");
        setCond = new LineDataSet(new ArrayList<>(), "Conductividad");
        setTurb = new LineDataSet(new ArrayList<>(), "Turbidez");

        configurarDataSet(setPH, colorOk);
        configurarDataSet(setCond, ContextCompat.getColor(this, R.color.color_primary));
        configurarDataSet(setTurb, ContextCompat.getColor(this, R.color.color_secondary));

        lineData = new LineData();
        lineData.addDataSet(setPH);
        lineData.addDataSet(setCond);
        lineData.addDataSet(setTurb);

        sensorChart.setData(lineData);
        sensorChart.invalidate();
    }

    private void configurarDataSet(LineDataSet set, int color) {
        set.setLineWidth(2f);
        set.setDrawCircles(true);
        set.setCircleRadius(3f);
        set.setDrawValues(false);
        set.setColor(color);
        set.setCircleColor(color);
    }

    private void agregarPuntosGrafico(double ph, double conductividad, double turbidez) {
        if (lineData == null) return;

        lineData.addEntry(new Entry(sampleIndex, escalarPh(ph)), 0);
        lineData.addEntry(new Entry(sampleIndex, escalarConductividad(conductividad)), 1);
        lineData.addEntry(new Entry(sampleIndex, escalarTurbidez(turbidez)), 2);

        sampleIndex++;

        podarSet(setPH);
        podarSet(setCond);
        podarSet(setTurb);

        lineData.notifyDataChanged();
        sensorChart.notifyDataSetChanged();
        sensorChart.invalidate();
    }

    private void podarSet(LineDataSet set) {
        while (set.getEntryCount() > MAX_POINTS_PER_SET) {
            set.removeFirst();
        }
    }

    private void actualizarEstadosVisuales(double ph, double conductividad, double turbidez, double ultrasonico) {
        int colorPh = obtenerColorEstado(ph, 6.5, 8.5, 6.0, 9.0);
        int colorCond = obtenerColorEstado(conductividad, 0, 700, 701, 1500);
        int colorTurb = obtenerColorEstado(turbidez, 0, 5, 6, 50);
        int colorUltra = obtenerColorEstado(ultrasonico, 60, 100, 30, 59);

        aplicarEstado(txtPh, txtPhEstado, colorPh, "pH");
        aplicarEstado(txtConductividad, txtCondEstado, colorCond, "Conductividad");
        aplicarEstado(txtTurbidez, txtTurbEstado, colorTurb, "Turbidez");
        aplicarEstado(txtUltrasonico, txtUltraEstado, colorUltra, "Nivel del tanque");
    }

    private void aplicarEstado(TextView valorView, TextView estadoView, int color, String nombreSensor) {
        valorView.setTextColor(color);
        estadoView.setTextColor(color);

        String estado;
        if (color == colorOk) estado = "Normal";
        else if (color == colorAlerta) estado = "Advertencia";
        else estado = "Peligro";

        estadoView.setText(String.format(Locale.getDefault(), "%s: %s", nombreSensor, estado));
    }

    private int obtenerColorEstado(double valor, double okMin, double okMax, double alertaMin, double alertaMax) {
        if (Double.isNaN(valor)) return colorPeligro;
        if (valor >= okMin && valor <= okMax) return colorOk;
        if (valor >= alertaMin && valor <= alertaMax) return colorAlerta;
        return colorPeligro;
    }

    private double leerDouble(DataSnapshot snap, String key) {
        if (!snap.hasChild(key)) return Double.NaN;
        try {
            return Double.parseDouble(String.valueOf(snap.child(key).getValue()));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private String primerTextoValido(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.trim().isEmpty()) return valor;
        }
        return null;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    private float escalarPh(double valor) {
        return Double.isNaN(valor) ? 0f : (float) ((valor / 14.0) * 100.0);
    }

    private float escalarConductividad(double valor) {
        return Double.isNaN(valor) ? 0f : (float) ((valor / 2000.0) * 100.0);
    }

    private float escalarTurbidez(double valor) {
        return Double.isNaN(valor) ? 0f : (float) (valor);
    }

    private void mostrarSinDispositivo() {
        txtPh.setText("pH: N/A");
        txtConductividad.setText("Conductividad: N/A");
        txtTurbidez.setText("Turbidez: N/A");
        txtUltrasonico.setText("Ultrasonico: N/A");

        txtPhEstado.setText("Sin dispositivo");
        txtCondEstado.setText("Sin dispositivo");
        txtTurbEstado.setText("Sin dispositivo");
        txtUltraEstado.setText("Sin dispositivo");

        txtPhEstado.setTextColor(colorAlerta);
        txtCondEstado.setTextColor(colorAlerta);
        txtTurbEstado.setTextColor(colorAlerta);
        txtUltraEstado.setTextColor(colorAlerta);
    }

    public void Volver2(View view) {
        finish();
    }

    /** Called via android:onClick="editarTanque" in the layout. */
    public void editarTanque(View view) {
        Intent intent = new Intent(this, Editor.class);
        intent.putExtra("tanqueId", tanqueId);
        intent.putExtra("tanqueNombre", tanqueNombre);
        intent.putExtra("tanqueCapacidad", tanqueCapacidad);
        intent.putExtra("tanqueColor", tanqueColor);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tanqueRef != null && tanqueListener != null) tanqueRef.removeEventListener(tanqueListener);
        if (dispositivoRef != null && dispositivoListener != null) dispositivoRef.removeEventListener(dispositivoListener);
    }
}
