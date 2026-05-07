package com.example.appiot12.ui.admin;
// 📦 Pantalla de estadísticas globales del proyecto Agua Segura.
// Aquí vemos los números grandes del sistema 📊💧

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

/**
 * 📊 HistorialGlobal
 *
 * ¿Qué hace esta pantalla?
 * 👉 Cuenta usuarios, tanques y dispositivos del sistema
 * 👉 Muestra los resultados en texto
 * 👉 Dibuja un gráfico circular simple
 *
 * Explicado para un niño:
 * 👉 Es como un resumen con dibujos que dice
 *    cuántas personas, tanques y robots hay 🤖🛢️👤
 */
public class HistorialGlobal extends AppCompatActivity {

    // 📝 Textos donde mostramos los números
    private TextView txtUsuariosTotal;
    private TextView txtTanquesTotal;
    private TextView txtDispositivosTotal;

    // 📊 Gráfico circular
    private PieChart pieChartUsuarios;

    // ☁️ Referencia a Firebase
    private DatabaseReference refUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_global); // 🎨 Mostramos la pantalla

        // 🔗 Conectamos UI con XML
        inicializarVistas();

        // ☁️ Nodo principal de usuarios
        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        // 🚀 Cargamos estadísticas globales
        cargarEstadisticasGlobales();
    }

    /**
     * 🔗 Vincula los elementos visuales con el XML
     */
    private void inicializarVistas() {
        txtUsuariosTotal = findViewById(R.id.txtUsuariosTotal);
        txtTanquesTotal = findViewById(R.id.txtTanquesTotal);
        txtDispositivosTotal = findViewById(R.id.txtDispositivosTotal);
        pieChartUsuarios = findViewById(R.id.pieChartUsuarios);
    }

    // =====================================================
    // 📥 CARGAR ESTADÍSTICAS DESDE FIREBASE
    // =====================================================
    private void cargarEstadisticasGlobales() {

        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                int totalUsuarios = 0;
                int totalTanques = 0;
                int totalDispositivos = 0;

                // 🔄 Recorremos todos los usuarios
                for (DataSnapshot usuarioSnap : snapshot.getChildren()) {

                    String rol = usuarioSnap.child("rol").getValue(String.class);

                    // ⭐ Solo contamos usuarios normales
                    if (!"usuario".equalsIgnoreCase(rol)) {
                        continue;
                    }

                    totalUsuarios++; // 👤 Usuario válido

                    // 🛢️ Contar tanques
                    DataSnapshot tanquesSnap = usuarioSnap.child("tanques");
                    if (tanquesSnap.exists()) {

                        for (DataSnapshot tanqueSnap : tanquesSnap.getChildren()) {

                            totalTanques++; // 🛢️ Sumamos tanque

                            // 📡 Si tiene dispositivo asociado
                            if (tanqueSnap.child("idDispositivo").exists()) {
                                totalDispositivos++;
                            }
                        }
                    }
                }

                // 📝 Mostrar resultados
                mostrarResultados(totalUsuarios, totalTanques, totalDispositivos);

                // 📊 Actualizar gráfico
                actualizarGrafico(totalUsuarios, totalDispositivos);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(
                        HistorialGlobal.this,
                        "Error al leer datos ⚠️",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    /**
     * 📝 Muestra los resultados en los TextView
     */
    private void mostrarResultados(int usuarios, int tanques, int dispositivos) {
        txtUsuariosTotal.setText("Usuarios totales: " + usuarios);
        txtTanquesTotal.setText("Tanques totales: " + tanques);
        txtDispositivosTotal.setText("Dispositivos totales: " + dispositivos);
    }

    // =====================================================
    // 📊 ACTUALIZAR GRÁFICO CIRCULAR
    // =====================================================
    private void actualizarGrafico(int usuarios, int dispositivos) {

        List<PieEntry> entradas = new ArrayList<>();

        entradas.add(new PieEntry(usuarios, "Usuarios 👤"));
        entradas.add(new PieEntry(dispositivos, "Dispositivos 🤖"));

        PieDataSet dataSet = new PieDataSet(entradas, "Distribución del sistema");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        // 🎨 Colores corporativos
        List<Integer> colores = new ArrayList<>();
        colores.add(Color.parseColor("#4CAF50")); // Verde
        colores.add(Color.parseColor("#303F9F")); // Azul
        dataSet.setColors(colores);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        // ⚙️ Configuración del gráfico
        pieChartUsuarios.setUsePercentValues(true);
        pieChartUsuarios.setDrawHoleEnabled(true);
        pieChartUsuarios.setHoleColor(Color.TRANSPARENT);
        pieChartUsuarios.getDescription().setEnabled(false);
        pieChartUsuarios.getLegend().setEnabled(true);

        pieChartUsuarios.setData(data);
        pieChartUsuarios.invalidate(); // 🔄 Redibujar
    }
}
