package com.example.appiot12.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appiot12.ui.BaseActivity;

import com.example.appiot12.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistorialGlobal extends BaseActivity {

    private TextView txtUsuariosTotal;
    private TextView txtTanquesTotal;
    private TextView txtDispositivosTotal;

    private PieChart pieChartUsuarios;

    private DatabaseReference refUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_historial_global);

        txtUsuariosTotal = findViewById(R.id.txtUsuariosTotal);
        txtTanquesTotal = findViewById(R.id.txtTanquesTotal);
        txtDispositivosTotal = findViewById(R.id.txtDispositivosTotal);
        pieChartUsuarios = findViewById(R.id.pieChartUsuarios);

        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        cargarEstadisticasGlobales();
    }

    private void cargarEstadisticasGlobales() {
        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalUsuarios = 0;
                int totalTanques = 0;
                int totalDispositivos = 0;

                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {
                    String rol = usuarioSnap.child("rol").getValue(String.class);
                    if (!"usuario".equalsIgnoreCase(rol)) continue;

                    totalUsuarios++;

                    DataSnapshot tanquesSnap = usuarioSnap.child("tanques");
                    if (tanquesSnap.exists()) {
                        for (DataSnapshot tanqueSnap : tanquesSnap.getChildren()) {
                            totalTanques++;
                            if (tanqueSnap.child("idDispositivo").exists()) {
                                totalDispositivos++;
                            }
                        }
                    }
                }

                mostrarResultados(totalUsuarios, totalTanques, totalDispositivos);
                actualizarGrafico(totalUsuarios, totalDispositivos);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistorialGlobal.this, "Error al leer datos", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarResultados(int usuarios, int tanques, int dispositivos) {
        txtUsuariosTotal.setText(String.valueOf(usuarios));
        txtTanquesTotal.setText(String.valueOf(tanques));
        txtDispositivosTotal.setText(String.valueOf(dispositivos));
    }

    private void actualizarGrafico(int usuarios, int dispositivos) {
        List<PieEntry> entradas = new ArrayList<>();
        entradas.add(new PieEntry(usuarios, "Usuarios"));
        entradas.add(new PieEntry(dispositivos, "Dispositivos"));

        PieDataSet dataSet = new PieDataSet(entradas, "Distribución del sistema");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        List<Integer> colores = new ArrayList<>();
        colores.add(Color.parseColor("#4CAF50"));
        colores.add(Color.parseColor("#303F9F"));
        dataSet.setColors(colores);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        pieChartUsuarios.setUsePercentValues(true);
        pieChartUsuarios.setDrawHoleEnabled(true);
        pieChartUsuarios.setHoleColor(Color.TRANSPARENT);
        pieChartUsuarios.getDescription().setEnabled(false);
        pieChartUsuarios.getLegend().setEnabled(true);
        pieChartUsuarios.setData(data);
        pieChartUsuarios.invalidate();
    }
}
