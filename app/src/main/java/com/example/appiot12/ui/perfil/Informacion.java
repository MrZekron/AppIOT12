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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class Informacion extends BaseActivity {

    private int colorOk;
    private int colorAlerta;
    private int colorPeligro;

    private TextView txtNombre;
    private TextView txtCapasidad;
    private TextView txtColor;
    private TextView txtDireccion;
    private TextView txtMantTanque;
    private TextView txtMantDispositivo;

    private TextView txtPh;
    private TextView txtConductividad;
    private TextView txtTurbidez;
    private TextView txtUltrasonico;

    private TextView txtPhEstado;
    private TextView txtCondEstado;
    private TextView txtTurbEstado;
    private TextView txtUltraEstado;

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
        txtMantTanque = findViewById(R.id.txtMantTanque);
        txtMantDispositivo = findViewById(R.id.txtMantDispositivo);

        txtPh = findViewById(R.id.txtPh);
        txtConductividad = findViewById(R.id.txtConductividad);
        txtTurbidez = findViewById(R.id.txtTurbidez);
        txtUltrasonico = findViewById(R.id.txtSonico);

        txtPhEstado = findViewById(R.id.txtPhEstado);
        txtCondEstado = findViewById(R.id.txtCondEstado);
        txtTurbEstado = findViewById(R.id.txtTurbEstado);
        txtUltraEstado = findViewById(R.id.txtUltraEstado);
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
        int capLitros = intent.getIntExtra("tanqueCapacidad", 0);
        tanqueCapacidad = capLitros > 0 ? capLitros + " L" : "";
        tanqueColor = valorSeguro(intent.getStringExtra("tanqueColor"));
        tanqueDireccion = valorSeguro(intent.getStringExtra("tanqueDireccion"));

        txtNombre.setText(tanqueNombre);
        txtCapasidad.setText(tanqueCapacidad);
        txtDireccion.setText(tanqueDireccion.isEmpty() ? "Sin dirección registrada" : tanqueDireccion);
        actualizarTextoMantencion(tanqueColor);
    }

    private void actualizarTextoMantencion(String color) {
        txtColor.setText(color.isEmpty() ? "—" : color);
        txtColor.setTextColor(colorOk);

        txtMantTanque.setText(mantencionTanque ? "Pendiente" : "Al día");
        txtMantTanque.setTextColor(mantencionTanque ? colorPeligro : colorOk);

        txtMantDispositivo.setText(mantencionDispositivo ? "Pendiente" : "Al día");
        txtMantDispositivo.setTextColor(mantencionDispositivo ? colorPeligro : colorOk);
    }

    private void suscribirseMetaTanque() {
        tanqueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String nombre = snap.child("nombre").getValue(String.class);
                Long capacidadLong = snap.child("capacidadLitros").getValue(Long.class);
                String color = snap.child("color").getValue(String.class);
                String direccion = snap.child("direccion").getValue(String.class);

                Boolean mantTanqueDb = snap.child("mantencionTanque").getValue(Boolean.class);
                Boolean mantDispDb = snap.child("mantencionDispositivo").getValue(Boolean.class);

                if (nombre != null) txtNombre.setText(nombre);
                if (capacidadLong != null) {
                    tanqueCapacidad = capacidadLong + " L";
                    txtCapasidad.setText(tanqueCapacidad);
                }
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
                txtConductividad.setText(String.format(Locale.getDefault(), "Conductividad: %.2f µS/cm", conductividad));
                txtTurbidez.setText(String.format(Locale.getDefault(), "Turbidez: %.2f NTU", turbidez));
                txtUltrasonico.setText(String.format(Locale.getDefault(), "Nivel: %.2f cm", ultrasonico));

                actualizarEstadosVisuales(ph, conductividad, turbidez, ultrasonico);

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

        estadoView.setText(String.format(Locale.getDefault(), "Estado: %s", estado));
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

    private void mostrarSinDispositivo() {
        txtPh.setText("pH: N/A");
        txtConductividad.setText("Conductividad: N/A");
        txtTurbidez.setText("Turbidez: N/A");
        txtUltrasonico.setText("Nivel: N/A");

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
        intent.putExtra("tanqueCapacidad", tanqueCapacidad.replace(" L", ""));
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
