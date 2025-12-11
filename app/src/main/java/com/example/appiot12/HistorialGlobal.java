package com.example.appiot12;
// 📦 Pantalla de estadísticas globales.
// El panel donde los CEOs lloran de emoción viendo KPIs en vivo 📊😎

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

    // 📝 TextViews para mostrar KPIs globales
    private TextView txtUsuariosTotal, txtTanquesTotal, txtDispositivosTotal;

    // 📊 Gráfico circular para representar proporciones
    private PieChart pieChartUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 🖥️ Pantalla moderna sin bordes
        setContentView(R.layout.activity_historial_global);

        // Ajuste automático de padding según barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // 🔗 Vincular UI
        txtUsuariosTotal = findViewById(R.id.txtUsuariosTotal);
        txtTanquesTotal = findViewById(R.id.txtTanquesTotal);
        txtDispositivosTotal = findViewById(R.id.txtDispositivosTotal);
        pieChartUsuarios = findViewById(R.id.pieChartUsuarios);

        // 🚀 Cargar KPIs de Firebase
        cargarHistorialGlobal();
    }

    // ============================================================================
    // 📥 CARGAR INFORMACIÓN GLOBAL DESDE FIREBASE
    // ============================================================================
    private void cargarHistorialGlobal() {

        // Accedemos al nodo principal de usuarios
        DatabaseReference refUsuarios =
                FirebaseDatabase.getInstance().getReference("usuarios");

        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // 🔢 Contadores globales
                int totalUsuarios = 0;
                int totalTanques = 0;
                int totalDispositivos = 0;

                // 🔄 Iteramos sobre todos los usuarios
                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {

                    // ============================
                    // 🔥 FILTRO: Solo usuarios normales
                    // ============================
                    String rol = usuarioSnap.child("rol").getValue(String.class);

                    if (rol == null || !rol.equalsIgnoreCase("usuario")) {
                        // Saltamos administradores o cualquier otro rol
                        continue;
                    }

                    totalUsuarios++; // 👤 Usuario válido encontrado

                    // ============================
                    // 🔍 CONTAR TANQUES DEL USUARIO
                    // ============================
                    if (usuarioSnap.child("tanques").exists()) {

                        for (DataSnapshot tanqueSnap : usuarioSnap.child("tanques").getChildren()) {

                            totalTanques++; // 🛢️ Sumamos tanque

                            // ============================
                            // 🎯 CONTAR DISPOSITIVOS ASOCIADOS
                            // ============================
                            if (tanqueSnap.child("idDispositivo").exists()) {
                                totalDispositivos++; // 📡 Dispositivo asignado a ese tanque
                            }
                        }
                    }
                }

                // ================================
                // 🔢 MOSTRAR RESULTADOS EN UI
                // ================================
                txtUsuariosTotal.setText("Usuarios totales: " + totalUsuarios);
                txtTanquesTotal.setText("Tanques totales: " + totalTanques);
                txtDispositivosTotal.setText("Dispositivos totales: " + totalDispositivos);

                // ================================
                // 📊 Actualizar gráfico circular
                // ================================
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

    // ============================================================================
    // ⭐ GRÁFICO PIECHART DINÁMICO (Usuarios vs Dispositivos)
    // ============================================================================
    private void actualizarGrafico(int usuarios, int dispositivos) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        // Entradas del gráfico
        entries.add(new PieEntry(usuarios, "Usuarios"));
        entries.add(new PieEntry(dispositivos, "Dispositivos"));

        // Dataset del gráfico
        PieDataSet dataSet = new PieDataSet(entries, "Distribución del Sistema");
        dataSet.setSliceSpace(3f);     // Separación estética
        dataSet.setSelectionShift(6f); // Aumento visual cuando se selecciona

        // 🎨 Paleta corporativa verde + azul
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50")); // Verde → usuarios
        colors.add(Color.parseColor("#303F9F")); // Azul → dispositivos
        dataSet.setColors(colors);

        // Formato de texto
        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        // Configuración general del gráfico
        pieChartUsuarios.setUsePercentValues(true);
        pieChartUsuarios.setDrawHoleEnabled(true);      // 🔘 Gráfico tipo donut
        pieChartUsuarios.setHoleColor(Color.TRANSPARENT);

        pieChartUsuarios.getDescription().setEnabled(false);
        pieChartUsuarios.getLegend().setEnabled(true);

        // Asignar dataset
        pieChartUsuarios.setData(data);

        // Refrescar visualización
        pieChartUsuarios.invalidate();
    }
}
