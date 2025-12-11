package com.example.appiot12;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class HistorialGlobal extends AppCompatActivity {

    private TextView txtUsuariosTotal, txtTanquesTotal, txtDispositivosTotal;
    private PieChart pieChartUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_global);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtUsuariosTotal = findViewById(R.id.txtUsuariosTotal);
        txtTanquesTotal = findViewById(R.id.txtTanquesTotal);
        txtDispositivosTotal = findViewById(R.id.txtDispositivosTotal);
        pieChartUsuarios = findViewById(R.id.pieChartUsuarios);

        cargarHistorialGlobal();
    }

    private void cargarHistorialGlobal() {

        DatabaseReference refUsuarios =
                FirebaseDatabase.getInstance().getReference("usuarios");

        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                int totalUsuarios = 0;
                int totalTanques = 0;
                int totalDispositivos = 0;

                // 🔍 Recorremos TODOS los usuarios
                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {

                    // ============================
                    //    🔥 FILTRO POR ROL AQUÍ
                    // ============================
                    String rol = usuarioSnap.child("rol").getValue(String.class);

                    if (rol == null || !rol.equalsIgnoreCase("usuario")) {
                        continue; // ❌ Saltamos admins y otros
                    }

                    totalUsuarios++; // Contamos usuarios válidos

                    // ---------- TANQUES ----------
                    if (usuarioSnap.child("tanques").exists()) {

                        for (DataSnapshot tanqueSnap : usuarioSnap.child("tanques").getChildren()) {

                            totalTanques++;

                            // contamos dispositivos SOLO si existe idDispositivo
                            if (tanqueSnap.child("idDispositivo").exists()) {
                                totalDispositivos++;
                            }
                        }
                    }
                }

                // ---------- MOSTRAR RESULTADOS ----------
                txtUsuariosTotal.setText("Usuarios totales: " + totalUsuarios);
                txtTanquesTotal.setText("Tanques totales: " + totalTanques);
                txtDispositivosTotal.setText("Dispositivos totales: " + totalDispositivos);

                // ---------- ACTUALIZAR GRÁFICO ----------
                actualizarGrafico(totalUsuarios, totalDispositivos);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistorialGlobal.this,
                        "Error al leer historial: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ======================================================
    //          ⭐ GRÁFICO PIECHART DINÁMICO ⭐
    // ======================================================
    private void actualizarGrafico(int usuarios, int dispositivos) {

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(usuarios, "Usuarios"));
        entries.add(new PieEntry(dispositivos, "Dispositivos"));

        PieDataSet dataSet = new PieDataSet(entries, "Distribución");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        // Colores corporativos
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50")); // verde usuarios
        colors.add(Color.parseColor("#303F9F")); // azul dispositivos
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        pieChartUsuarios.setUsePercentValues(true);
        pieChartUsuarios.setDrawHoleEnabled(true);
        pieChartUsuarios.setHoleColor(Color.TRANSPARENT);

        pieChartUsuarios.getDescription().setEnabled(false);
        pieChartUsuarios.getLegend().setEnabled(true);

        pieChartUsuarios.setData(data);
        pieChartUsuarios.invalidate(); // refrescar
    }
}
