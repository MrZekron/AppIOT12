package com.example.appiot12.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.appiot12.ui.BaseActivity;

import com.example.appiot12.R;
import com.example.appiot12.adapter.HistorialAdapter;
import com.example.appiot12.model.HistorialEvento;
import com.example.appiot12.model.LecturaParametro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialAcciones extends BaseActivity {

    private Spinner spnFiltro;
    private ListView lvHistorial;
    private Button btnPdf;

    private final List<HistorialEvento> eventos = new ArrayList<>();
    private HistorialAdapter adapter;

    private DatabaseReference refLecturas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_historial_acciones);

        spnFiltro   = findViewById(R.id.spnFiltro);
        lvHistorial = findViewById(R.id.lvHistorial);
        btnPdf      = findViewById(R.id.btnPdf);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refLecturas = FirebaseDatabase.getInstance().getReference("lecturas_historial");

        adapter = new HistorialAdapter(this, eventos);
        lvHistorial.setAdapter(adapter);

        configurarFiltro();
        cargarHistorial(30);

        btnPdf.setOnClickListener(v -> exportarHistorial());
    }

    private void configurarFiltro() {
        ArrayAdapter<CharSequence> filtroAdapter = ArrayAdapter.createFromResource(
                this, R.array.filtro_historial, android.R.layout.simple_spinner_item);
        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFiltro.setAdapter(filtroAdapter);

        spnFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)      cargarHistorial(1);
                else if (position == 1) cargarHistorial(7);
                else                    cargarHistorial(30);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarHistorial(int dias) {
        long limite = System.currentTimeMillis() - (dias * 24L * 60L * 60L * 1000L);

        refLecturas.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                eventos.clear();

                for (DataSnapshot deviceSnap : snapshot.getChildren()) {
                    String idDispositivo = deviceSnap.getKey();

                    for (DataSnapshot lecturaSnap : deviceSnap.getChildren()) {
                        LecturaParametro lectura = lecturaSnap.getValue(LecturaParametro.class);
                        if (lectura == null || lectura.getTimestamp() < limite) continue;

                        HistorialEvento evento = new HistorialEvento();
                        evento.setTipo(idDispositivo);
                        evento.setDescripcion(formatearLectura(lectura));
                        evento.setTimestamp(lectura.getTimestamp());
                        eventos.add(evento);
                    }
                }

                eventos.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistorialAcciones.this, "Error al cargar lecturas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatearLectura(LecturaParametro l) {
        return String.format(Locale.getDefault(),
                "pH %.1f  |  Turb %.1f NTU  |  Nivel %.0f%%  |  Cond %.0f µS",
                l.getPh(), l.getTurbidez(), l.getNivelPorcentaje(), l.getConductividad());
    }

    private void exportarHistorial() {
        try {
            File file = new File(getExternalFilesDir(null), "lecturas_historial.txt");
            FileOutputStream fos = new FileOutputStream(file);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            fos.write("Historial de lecturas IoT\n".getBytes());
            fos.write("==========================\n".getBytes());

            for (HistorialEvento e : eventos) {
                String linea = sdf.format(new Date(e.getTimestamp()))
                        + "  [" + safe(e.getTipo()) + "]  "
                        + safe(e.getDescripcion()) + "\n";
                fos.write(linea.getBytes());
            }

            fos.flush();
            fos.close();
            Toast.makeText(this, "Exportado: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error al exportar", Toast.LENGTH_LONG).show();
        }
    }

    private String safe(String valor) {
        return valor == null ? "" : valor;
    }
}
