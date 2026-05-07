package com.example.appiot12.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appiot12.R;
import com.example.appiot12.adapter.HistorialAdapter;
import com.example.appiot12.model.HistorialEvento;
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

/**
 * Activity que muestra el historial de acciones del usuario.
 *
 * Funcionalidades:
 * - Filtrar historial por rango de días
 * - Mostrar eventos en una lista
 * - Exportar historial a un archivo de texto
 *
 * Nota:
 * Se apoya en Firebase Realtime Database y en la clase HistorialEvento.
 */
public class HistorialAcciones extends AppCompatActivity {

    // =========================
    // COMPONENTES DE INTERFAZ
    // =========================
    private Spinner spnFiltro;
    private ListView lvHistorial;
    private Button btnPdf;

    // =========================
    // DATOS
    // =========================
    private final List<HistorialEvento> eventos = new ArrayList<>();
    private HistorialAdapter adapter;

    // =========================
    // FIREBASE
    // =========================
    private DatabaseReference refHistorial;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_acciones);

        // Vincula los componentes XML con el código Java
        spnFiltro = findViewById(R.id.spnFiltro);
        lvHistorial = findViewById(R.id.lvHistorial);
        btnPdf = findViewById(R.id.btnPdf);

        // Verifica que exista un usuario autenticado
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Obtiene el UID del usuario logueado
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referencia al historial del usuario en Firebase
        refHistorial = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        // Configura el adaptador para mostrar los eventos en la lista
        adapter = new HistorialAdapter(this, eventos);
        lvHistorial.setAdapter(adapter);

        // Configura el spinner del filtro
        configurarFiltro();

        // Carga por defecto los últimos 30 días
        cargarHistorial(30);

        // Acción del botón exportar
        btnPdf.setOnClickListener(v -> exportarHistorial());
    }

    // =========================
    // CONFIGURAR FILTRO
    // =========================
    /**
     * Configura el spinner que permite filtrar el historial
     * por hoy, 7 días o 30 días.
     */
    private void configurarFiltro() {

        ArrayAdapter<CharSequence> filtroAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.filtro_historial,
                android.R.layout.simple_spinner_item
        );

        filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFiltro.setAdapter(filtroAdapter);

        spnFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    cargarHistorial(1);   // Hoy
                } else if (position == 1) {
                    cargarHistorial(7);   // Últimos 7 días
                } else if (position == 2) {
                    cargarHistorial(30);  // Últimos 30 días
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se requiere acción aquí
            }
        });
    }

    // =========================
    // CARGAR HISTORIAL
    // =========================
    /**
     * Carga desde Firebase los eventos dentro de un rango de días.
     *
     * @param dias cantidad de días hacia atrás que se quieren mostrar
     */
    private void cargarHistorial(int dias) {

        long ahora = System.currentTimeMillis();
        long limite = ahora - (dias * 24L * 60L * 60L * 1000L);

        refHistorial.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                eventos.clear();

                for (DataSnapshot s : snapshot.getChildren()) {

                    HistorialEvento evento = s.getValue(HistorialEvento.class);

                    // Se usa timestamp en vez de fecha
                    if (evento != null && evento.getTimestamp() >= limite) {
                        eventos.add(evento);
                    }
                }

                // Ordena el historial del más reciente al más antiguo
                eventos.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                // Actualiza la lista visual
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        HistorialAcciones.this,
                        "Error al cargar historial",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // =========================
    // EXPORTAR HISTORIAL
    // =========================
    /**
     * Exporta el historial mostrado actualmente a un archivo .txt.
     *
     * Aunque el botón se llame btnPdf, aquí realmente estás exportando
     * un TXT. No pasa nada, pero el nombre del método queda más honesto
     * que político en campaña.
     */
    private void exportarHistorial() {

        try {
            File file = new File(getExternalFilesDir(null), "historial.txt");
            FileOutputStream fos = new FileOutputStream(file);

            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
            );

            for (HistorialEvento evento : eventos) {

                String linea = "[" + safe(evento.getTipo()) + "] "
                        + safe(evento.getDescripcion()) + " - "
                        + sdf.format(new Date(evento.getTimestamp()))
                        + "\n";

                fos.write(linea.getBytes());
            }

            fos.flush();
            fos.close();

            Toast.makeText(
                    this,
                    "Historial exportado correctamente:\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Error al exportar historial",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================
    // UTILIDAD
    // =========================
    /**
     * Evita null al mostrar texto.
     *
     * @param valor texto posiblemente nulo
     * @return texto seguro
     */
    private String safe(String valor) {
        return valor == null ? "" : valor;
    }
}
