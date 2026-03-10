package com.example.appiot12;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 📜 HistorialAcciones
 * Muestra todas las acciones del sistema con filtro y exportación
 */
public class HistorialAcciones extends AppCompatActivity {

    // UI
    private Spinner spnFiltro;
    private ListView lvHistorial;
    private Button btnPdf;

    // Datos
    private final List<HistorialEvento> eventos = new ArrayList<>();
    private HistorialAdapter adapter;

    // Firebase
    private DatabaseReference refHistorial;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_acciones);

        // Vincular UI
        spnFiltro = findViewById(R.id.spnFiltro);
        lvHistorial = findViewById(R.id.lvHistorial);
        btnPdf = findViewById(R.id.btnPdf);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        refHistorial = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        adapter = new HistorialAdapter(this, eventos);
        lvHistorial.setAdapter(adapter);

        configurarFiltro();
        cargarHistorial(30); // por defecto 30 días

        btnPdf.setOnClickListener(v -> exportarPdf());
    }

    // =========================
    // 🔽 FILTRO
    // =========================
    private void configurarFiltro() {

        ArrayAdapter<CharSequence> filtroAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.filtro_historial,
                        android.R.layout.simple_spinner_item
                );

        filtroAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spnFiltro.setAdapter(filtroAdapter);

        spnFiltro.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        if (position == 0) cargarHistorial(1);   // Hoy
                        if (position == 1) cargarHistorial(7);   // 7 días
                        if (position == 2) cargarHistorial(30);  // 30 días
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                }
        );
    }

    // =========================
    // 📥 CARGAR HISTORIAL
    // =========================
    private void cargarHistorial(int dias) {

        long ahora = System.currentTimeMillis();
        long limite = ahora - (dias * 24L * 60L * 60L * 1000L);

        refHistorial.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        eventos.clear();

                        for (DataSnapshot s : snapshot.getChildren()) {

                            HistorialEvento e =
                                    s.getValue(HistorialEvento.class);

                            if (e != null && e.fecha >= limite) {
                                eventos.add(e);
                            }
                        }

                        // Más reciente primero
                        eventos.sort(
                                (a, b) -> Long.compare(b.fecha, a.fecha)
                        );

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                }
        );
    }

    // =========================
    // 📄 EXPORTAR (TXT / BASE PDF)
    // =========================
    private void exportarPdf() {

        try {
            File file =
                    new File(getExternalFilesDir(null), "historial.txt");

            FileOutputStream fos = new FileOutputStream(file);

            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm",
                            Locale.getDefault()
                    );

            for (HistorialEvento e : eventos) {

                String linea =
                        "[" + e.tipo + "] " +
                                e.descripcion + " - " +
                                sdf.format(new Date(e.fecha)) + "\n";

                fos.write(linea.getBytes());
            }

            fos.close();

            Toast.makeText(
                    this,
                    "Historial exportado ✔️\n" +
                            file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception ex) {
            Toast.makeText(
                    this,
                    "Error al exportar historial ❌",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
