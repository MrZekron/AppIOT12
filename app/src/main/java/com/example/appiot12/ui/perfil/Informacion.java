package com.example.appiot12.ui.perfil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appiot12.R;
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

/**
 * Pantalla de información detallada del tanque y su dispositivo asociado.
 *
 * Responsabilidades:
 * - Mostrar metadata del tanque
 * - Escuchar cambios en tiempo real del dispositivo
 * - Colorear estados según rangos
 * - Graficar lecturas de sensores
 *
 * Nota:
 * Esta versión no depende de TextViews extra de mantención en el XML.
 * Para no romper compilación, la mantención se refleja junto al color del tanque.
 */
public class Informacion extends AppCompatActivity {

    // =====================================================
    // 🎨 COLORES DE ESTADO
    // =====================================================
    private static final int COLOR_OK = Color.parseColor("#2E7D32");
    private static final int COLOR_ALERTA = Color.parseColor("#F9A825");
    private static final int COLOR_PELIGRO = Color.parseColor("#C62828");

    // =====================================================
    // 🖥️ VISTAS DEL TANQUE
    // =====================================================
    private TextView txtNombre;
    private TextView txtCapasidad;
    private TextView txtColor;

    // =====================================================
    // 🖥️ VISTAS DE SENSORES
    // =====================================================
    private TextView txtPh;
    private TextView txtConductividad;
    private TextView txtTurbidez;
    private TextView txtUltrasonico;

    // =====================================================
    // 🖥️ VISTAS DE ESTADO
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

    private static final int MAX_POINTS_PER_SET = 300;
    private int sampleIndex = 0;

    // =====================================================
    // ☁️ FIREBASE
    // =====================================================
    private DatabaseReference tanqueRef;
    private DatabaseReference dispositivoRef;

    private ValueEventListener tanqueListener;
    private ValueEventListener dispositivoListener;

    // =====================================================
    // 📦 DATOS DEL INTENT
    // =====================================================
    private String tanqueId;
    private String idDispositivo;

    private String tanqueNombre;
    private String tanqueCapacidad;
    private String tanqueColor;

    private boolean mantencionTanque;
    private boolean mantencionDispositivo;

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

        inicializarVistas();
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
                .getReference("usuarios")
                .child(uid)
                .child("tanques")
                .child(tanqueId);

        if (idDispositivo != null && !idDispositivo.trim().isEmpty()) {
            dispositivoRef = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(uid)
                    .child("dispositivos")
                    .child(idDispositivo);

            suscribirseDispositivoTiempoReal();
        } else {
            mostrarSinDispositivo();
        }

        suscribirseMetaTanque();
    }

    // =====================================================
    // 🔗 INICIALIZAR VISTAS
    // =====================================================
    private void inicializarVistas() {
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
    }

    // =====================================================
    // 📥 LEER DATOS DEL INTENT
    // =====================================================
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

        String nombre = tanqueNombre;
        String capacidad = tanqueCapacidad;
        String color = tanqueColor;

        txtNombre.setText(nombre);
        txtCapasidad.setText(capacidad);

        // Como todavía no queremos depender de más TextViews en el XML,
        // mostramos el estado de mantención incrustado en txtColor.
        String textoMantencionTanque = mantencionTanque ? "Pendiente" : "Al día";
        String textoMantencionDispositivo = mantencionDispositivo ? "Pendiente" : "Al día";

        String textoColor = String.format(
                Locale.getDefault(),
                "%s | Mant. tanque: %s | Mant. dispositivo: %s",
                color,
                textoMantencionTanque,
                textoMantencionDispositivo
        );

        txtColor.setText(textoColor);

        if (mantencionTanque || mantencionDispositivo) {
            txtColor.setTextColor(COLOR_PELIGRO);
        } else {
            txtColor.setTextColor(COLOR_OK);
        }
    }

    // =====================================================
    // ☁️ SUSCRIBIRSE A METADATA DEL TANQUE
    // =====================================================
    private void suscribirseMetaTanque() {

        tanqueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                String nombre = snap.child("nombre").getValue(String.class);
                String capacidad = snap.child("capacidad").getValue(String.class);
                String color = snap.child("color").getValue(String.class);

                Boolean mantTanqueDb = snap.child("mantencionTanque").getValue(Boolean.class);
                Boolean mantDispDb = snap.child("mantencionDispositivo").getValue(Boolean.class);

                if (nombre != null) {
                    txtNombre.setText(nombre);
                }

                if (capacidad != null) {
                    txtCapasidad.setText(capacidad);
                }

                if (mantTanqueDb != null) {
                    mantencionTanque = mantTanqueDb;
                }

                if (mantDispDb != null) {
                    mantencionDispositivo = mantDispDb;
                }

                String textoMantencionTanque = mantencionTanque ? "Pendiente" : "Al día";
                String textoMantencionDispositivo = mantencionDispositivo ? "Pendiente" : "Al día";

                String colorSeguro = valorSeguro(color);

                String textoColor = String.format(
                        Locale.getDefault(),
                        "%s | Mant. tanque: %s | Mant. dispositivo: %s",
                        colorSeguro,
                        textoMantencionTanque,
                        textoMantencionDispositivo
                );

                txtColor.setText(textoColor);
                txtColor.setTextColor((mantencionTanque || mantencionDispositivo) ? COLOR_PELIGRO : COLOR_OK);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Informacion.this, "Error al leer datos del tanque.", Toast.LENGTH_SHORT).show();
            }
        };

        tanqueRef.addValueEventListener(tanqueListener);
    }

    // =====================================================
    // ☁️ SUSCRIBIRSE A SENSORES EN TIEMPO REAL
    // =====================================================
    private void suscribirseDispositivoTiempoReal() {

        dispositivoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                double ph = leerDouble(snap, "ph");
                double conductividad = leerDouble(snap, "conductividad");
                double turbidez = leerDouble(snap, "turbidez");
                double ultrasonico = leerDouble(snap, "ultrasonico");

                txtPh.setText(String.format(Locale.getDefault(), "pH: %.2f", ph));
                txtConductividad.setText(String.format(Locale.getDefault(), "Conductividad: %.2f", conductividad));
                txtTurbidez.setText(String.format(Locale.getDefault(), "Turbidez: %.2f", turbidez));
                txtUltrasonico.setText(String.format(Locale.getDefault(), "Ultrasonico: %.2f", ultrasonico));

                actualizarEstadosVisuales(ph, conductividad, turbidez, ultrasonico);
                agregarPuntosGrafico(ph, conductividad, turbidez);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Informacion.this, "Error al leer datos del dispositivo.", Toast.LENGTH_SHORT).show();
            }
        };

        dispositivoRef.addValueEventListener(dispositivoListener);
    }

    // =====================================================
    // 📈 CONFIGURAR GRÁFICO
    // =====================================================
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

        configurarDataSet(setPH, Color.parseColor("#2E7D32"));
        configurarDataSet(setCond, Color.BLUE);
        configurarDataSet(setTurb, Color.MAGENTA);

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

    // =====================================================
    // 📈 AGREGAR PUNTOS
    // =====================================================
    private void agregarPuntosGrafico(double ph, double conductividad, double turbidez) {
        if (lineData == null) {
            return;
        }

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

    // =====================================================
    // 🎨 ACTUALIZAR ESTADOS
    // =====================================================
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

        if (color == COLOR_OK) {
            estadoView.setText(String.format(Locale.getDefault(), "%s: Normal", nombreSensor));
        } else if (color == COLOR_ALERTA) {
            estadoView.setText(String.format(Locale.getDefault(), "%s: Advertencia", nombreSensor));
        } else {
            estadoView.setText(String.format(Locale.getDefault(), "%s: Peligro", nombreSensor));
        }
    }

    private int obtenerColorEstado(double valor, double okMin, double okMax, double alertaMin, double alertaMax) {
        if (Double.isNaN(valor)) {
            return COLOR_PELIGRO;
        }

        if (valor >= okMin && valor <= okMax) {
            return COLOR_OK;
        }

        if (valor >= alertaMin && valor <= alertaMax) {
            return COLOR_ALERTA;
        }

        return COLOR_PELIGRO;
    }

    // =====================================================
    // 🔎 HELPERS
    // =====================================================
    private double leerDouble(DataSnapshot snap, String key) {
        if (!snap.hasChild(key)) {
            return Double.NaN;
        }

        try {
            return Double.parseDouble(String.valueOf(snap.child(key).getValue()));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private String primerTextoValido(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.trim().isEmpty()) {
                return valor;
            }
        }
        return null;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    private float escalarPh(double valor) {
        if (Double.isNaN(valor)) {
            return 0f;
        }
        return (float) ((valor / 14.0) * 100.0);
    }

    private float escalarConductividad(double valor) {
        if (Double.isNaN(valor)) {
            return 0f;
        }
        return (float) ((valor / 2000.0) * 100.0);
    }

    private float escalarTurbidez(double valor) {
        if (Double.isNaN(valor)) {
            return 0f;
        }
        return (float) ((valor / 100.0) * 100.0);
    }

    // =====================================================
    // 🚫 CASO SIN DISPOSITIVO
    // =====================================================
    private void mostrarSinDispositivo() {
        txtPh.setText("pH: N/A");
        txtConductividad.setText("Conductividad: N/A");
        txtTurbidez.setText("Turbidez: N/A");
        txtUltrasonico.setText("Ultrasonico: N/A");

        txtPhEstado.setText("Sin dispositivo");
        txtCondEstado.setText("Sin dispositivo");
        txtTurbEstado.setText("Sin dispositivo");
        txtUltraEstado.setText("Sin dispositivo");

        txtPhEstado.setTextColor(COLOR_ALERTA);
        txtCondEstado.setTextColor(COLOR_ALERTA);
        txtTurbEstado.setTextColor(COLOR_ALERTA);
        txtUltraEstado.setTextColor(COLOR_ALERTA);
    }

    // =====================================================
    // ✏️ EDITAR TANQUE
    // =====================================================
    /**
     * Abre la pantalla Editor con los datos actuales del tanque.
     * Llamado por android:onClick="editarTanque" en el XML.
     */
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

        if (tanqueRef != null && tanqueListener != null) {
            tanqueRef.removeEventListener(tanqueListener);
        }

        if (dispositivoRef != null && dispositivoListener != null) {
            dispositivoRef.removeEventListener(dispositivoListener);
        }
    }
}
