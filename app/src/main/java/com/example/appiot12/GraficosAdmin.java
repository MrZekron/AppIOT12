package com.example.appiot12;

// =====================================================
// 📊 PANTALLA DE GRÁFICOS PARA EL ADMINISTRADOR
// =====================================================
// ¿Qué hace este archivo?
// 1. Carga datos de Firebase (usuarios y compras)
// 2. Agrupa registros por mes para ver tendencias
// 3. Dibuja 3 gráficos:
//    - Barras verticales: usuarios registrados por mes
//    - Barras verticales: compras de dispositivos por mes (global)
//    - Barras horizontales: cantidad de dispositivos comprados por usuario
// =====================================================

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * 📊 GraficosAdmin
 *
 * Pantalla exclusiva del administrador que muestra
 * estadísticas visuales del sistema en forma de gráficos.
 *
 * Explicado simple 👶:
 * → Es como ver un "reporte visual" de cuántos usuarios
 *   se registraron y cuántos dispositivos se compraron,
 *   agrupados por mes y por persona.
 *
 * Datos que usa desde Firebase:
 * → "usuarios/{uid}/createdAt"  → fecha de registro del usuario
 * → "compras/{id}/fecha"        → fecha de la compra
 * → "compras/{id}/uidUsuario"   → quién compró
 * → "compras/{id}/estado"       → estado (se excluyen los cancelados)
 * → "usuarios/{uid}/correo"     → email para mostrar en el gráfico por usuario
 */
public class GraficosAdmin extends AppCompatActivity {

    // =====================================================
    // 📊 GRÁFICOS DE BARRAS
    // =====================================================

    /** Gráfico de barras verticales: usuarios registrados por mes */
    private BarChart barChartUsuarios;

    /** Gráfico de barras verticales: compras globales de dispositivos por mes */
    private BarChart barChartComprasGlobal;

    /** Gráfico de barras horizontales: compras de dispositivos por usuario */
    private HorizontalBarChart barChartComprasPorUsuario;

    // =====================================================
    // 🔤 TEXTOS INFORMATIVOS
    // =====================================================

    /** Título del gráfico de usuarios */
    private TextView tvTituloUsuarios;

    /** Título del gráfico de compras globales */
    private TextView tvTituloComprasGlobal;

    /** Título del gráfico de compras por usuario */
    private TextView tvTituloComprasPorUsuario;

    // =====================================================
    // ☁️ REFERENCIAS FIREBASE
    // =====================================================

    /** Nodo "usuarios" en Firebase Realtime Database */
    private DatabaseReference refUsuarios;

    /** Nodo "compras" en Firebase Realtime Database */
    private DatabaseReference refCompras;

    // =====================================================
    // 🎨 PALETA DE COLORES (igual a Agregar.java)
    // =====================================================

    /** Azul primario: botones, barras principales */
    private static final int COLOR_PRIMARY     = Color.parseColor("#1D8EC1");

    /** Azul texto: títulos y etiquetas */
    private static final int COLOR_TEXT        = Color.parseColor("#4B65A9");

    /** Celeste: barras secundarias */
    private static final int COLOR_SECONDARY   = Color.parseColor("#5FCAE0");

    /** Blanco crema: texto sobre fondos oscuros */
    private static final int COLOR_SURFACE     = Color.parseColor("#F8EFED");

    // =====================================================
    // 📅 FORMATO DE FECHA PARA AGRUPAR POR MES
    // =====================================================

    /**
     * Formatea timestamp (milisegundos) como "MMM yyyy", por ejemplo "Ene 2025".
     * Se usa para agrupar usuarios y compras por mes.
     */
    private final SimpleDateFormat formatoMes =
            new SimpleDateFormat("MMM yyyy", new Locale("es", "CL"));

    // =====================================================
    // 🚀 CICLO DE VIDA
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graficos_admin);

        // 🔗 Vincula vistas del XML con variables Java
        inicializarVistas();

        // ☁️ Obtiene referencias a los nodos de Firebase
        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");
        refCompras  = FirebaseDatabase.getInstance().getReference("compras");

        // 📥 Carga los datos y dibuja los gráficos
        cargarDatosUsuarios();
        cargarDatosCompras();
    }

    // =====================================================
    // 🔗 INICIALIZAR VISTAS
    // =====================================================
    /**
     * Conecta cada variable Java con su elemento en el XML.
     * Si algún id no existe, la app lanzará NullPointerException al usar la vista.
     */
    private void inicializarVistas() {
        // Títulos descriptivos de cada sección
        tvTituloUsuarios          = findViewById(R.id.tvTituloUsuarios);
        tvTituloComprasGlobal     = findViewById(R.id.tvTituloComprasGlobal);
        tvTituloComprasPorUsuario = findViewById(R.id.tvTituloComprasPorUsuario);

        // Los tres gráficos
        barChartUsuarios          = findViewById(R.id.barChartUsuarios);
        barChartComprasGlobal     = findViewById(R.id.barChartComprasGlobal);
        barChartComprasPorUsuario = findViewById(R.id.barChartComprasPorUsuario);
    }

    // =====================================================
    // 👤 CARGAR USUARIOS Y GRAFICAR POR MES
    // =====================================================
    /**
     * Lee el nodo "usuarios" de Firebase.
     * Filtra solo los de rol "usuario" (excluye admins).
     * Agrupa por mes usando el campo "createdAt" (timestamp en ms).
     * Dibuja un BarChart vertical con los resultados.
     */
    private void cargarDatosUsuarios() {

        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // Mapa ordenado: clave = "Mes Año" (ej: "Ene 2025"), valor = cantidad de registros
                // TreeMap ordena alfabéticamente; usamos un LinkedHashMap al final para mantener
                // el orden cronológico tras procesar.
                Map<String, Integer> registrosPorMes = new TreeMap<>();

                // Recorre cada usuario en Firebase
                for (DataSnapshot snap : snapshot.getChildren()) {

                    // Obtiene el rol para filtrar solo usuarios normales
                    String rol = snap.child("rol").getValue(String.class);
                    if (!"usuario".equalsIgnoreCase(rol)) continue;

                    // Obtiene el timestamp de creación del usuario
                    Long createdAt = snap.child("createdAt").getValue(Long.class);
                    if (createdAt == null) continue; // Sin fecha → se ignora

                    // Convierte el timestamp a texto "Mes Año"
                    String mes = formatoMes.format(new Date(createdAt));

                    // Suma 1 al contador del mes correspondiente
                    registrosPorMes.put(mes, registrosPorMes.getOrDefault(mes, 0) + 1);
                }

                // Si no hay datos, muestra mensaje en lugar del gráfico
                if (registrosPorMes.isEmpty()) {
                    tvTituloUsuarios.setText("Usuarios registrados por mes (sin datos aún)");
                    return;
                }

                // Dibuja el gráfico de barras con los datos agrupados
                dibujarBarChart(
                        barChartUsuarios,
                        registrosPorMes,
                        "Usuarios registrados",
                        COLOR_PRIMARY
                );
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Error de Firebase: avisa al usuario sin crashear la app
                mostrarError("Error al cargar usuarios: " + error.getMessage());
            }
        });
    }

    // =====================================================
    // 🛒 CARGAR COMPRAS Y GRAFICAR
    // =====================================================
    /**
     * Lee el nodo "compras" de Firebase.
     * Excluye compras canceladas.
     * Realiza dos agrupaciones en paralelo:
     *   1. Por mes (gráfico global)
     *   2. Por usuario (gráfico individual, usando correo)
     *
     * Luego pide los correos de los usuarios para mostrar
     * nombres legibles en el gráfico por usuario.
     */
    private void cargarDatosCompras() {

        refCompras.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshotCompras) {

                // --- Agrupación 1: Compras por mes ---
                // Mapa: "Mes Año" → cantidad de compras ese mes
                Map<String, Integer> comprasPorMes = new TreeMap<>();

                // --- Agrupación 2: Compras por usuario ---
                // Mapa: uid → cantidad de dispositivos comprados
                Map<String, Integer> comprasPorUid = new LinkedHashMap<>();

                // Recorre cada registro de compra en Firebase
                for (DataSnapshot snap : snapshotCompras.getChildren()) {

                    // Estado de la compra: "pendiente_pago", "pagado", "cancelado", etc.
                    String estado = snap.child("estado").getValue(String.class);

                    // Las compras canceladas NO se contabilizan
                    if ("cancelado".equalsIgnoreCase(estado)) continue;

                    // UID del usuario que hizo la compra
                    String uid = snap.child("uidUsuario").getValue(String.class);
                    if (uid == null) continue;

                    // Timestamp de la compra en milisegundos
                    Long fecha = snap.child("fecha").getValue(Long.class);

                    // --- Agrupación por mes ---
                    if (fecha != null) {
                        String mes = formatoMes.format(new Date(fecha));
                        comprasPorMes.put(mes, comprasPorMes.getOrDefault(mes, 0) + 1);
                    }

                    // --- Agrupación por usuario ---
                    comprasPorUid.put(uid, comprasPorUid.getOrDefault(uid, 0) + 1);
                }

                // Dibuja el gráfico de compras globales por mes
                if (!comprasPorMes.isEmpty()) {
                    dibujarBarChart(
                            barChartComprasGlobal,
                            comprasPorMes,
                            "Dispositivos comprados",
                            COLOR_SECONDARY
                    );
                } else {
                    tvTituloComprasGlobal.setText("Compras de dispositivos por mes (sin datos aún)");
                }

                // Para el gráfico por usuario necesitamos los correos → consulta Firebase
                if (!comprasPorUid.isEmpty()) {
                    cargarCorreosParaGrafico(comprasPorUid);
                } else {
                    tvTituloComprasPorUsuario.setText("Compras por usuario (sin datos aún)");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                mostrarError("Error al cargar compras: " + error.getMessage());
            }
        });
    }

    // =====================================================
    // 📧 CARGAR CORREOS PARA EL GRÁFICO POR USUARIO
    // =====================================================
    /**
     * Recibe un mapa de {uid → cantidad de compras}.
     * Consulta Firebase para obtener el correo de cada usuario.
     * Sustituye el uid por el correo en el mapa resultante.
     * Luego dibuja el gráfico horizontal con los correos como etiquetas.
     *
     * Se espera a que todos los correos sean cargados antes de dibujar,
     * usando un contador para sincronizar las llamadas asíncronas.
     */
    private void cargarCorreosParaGrafico(Map<String, Integer> comprasPorUid) {

        // Mapa final: correo → cantidad de compras
        // LinkedHashMap para mantener orden de inserción
        Map<String, Integer> comprasPorCorreo = new LinkedHashMap<>();

        // Cuántos usuarios debemos consultar
        int[] total = {comprasPorUid.size()};

        // Cuántos ya terminamos de consultar (callback counter)
        int[] completados = {0};

        for (Map.Entry<String, Integer> entry : comprasPorUid.entrySet()) {

            String uid      = entry.getKey();
            int cantCompras = entry.getValue();

            // Lee el correo del usuario desde Firebase
            refUsuarios.child(uid).child("correo")
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot snap) {

                            // Si no tiene correo, usa el uid como etiqueta alternativa
                            String correo = snap.getValue(String.class);
                            String etiqueta = (correo != null && !correo.isEmpty())
                                    ? correo
                                    : uid.substring(0, 8) + "..."; // Muestra solo los primeros 8 caracteres del uid

                            // Agrega al mapa con la etiqueta legible
                            comprasPorCorreo.put(etiqueta, cantCompras);

                            // Cuando se cargaron todos, dibuja el gráfico
                            completados[0]++;
                            if (completados[0] >= total[0]) {
                                dibujarBarChartHorizontal(comprasPorCorreo);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Si falla la consulta del correo, usa uid recortado
                            comprasPorCorreo.put(uid.substring(0, 8) + "...", cantCompras);

                            completados[0]++;
                            if (completados[0] >= total[0]) {
                                dibujarBarChartHorizontal(comprasPorCorreo);
                            }
                        }
                    });
        }
    }

    // =====================================================
    // 📊 DIBUJAR BARCHART VERTICAL (GENERAL)
    // =====================================================
    /**
     * Método reutilizable que dibuja un BarChart vertical.
     *
     * @param chart       El BarChart del XML donde se dibujará
     * @param datos       Mapa de etiqueta → valor numérico
     * @param etiqueta    Nombre del dataset (aparece en la leyenda)
     * @param colorBarra  Color de las barras (usa constantes de color definidas arriba)
     */
    private void dibujarBarChart(BarChart chart,
                                  Map<String, Integer> datos,
                                  String etiqueta,
                                  int colorBarra) {

        // Lista de etiquetas del eje X (meses)
        List<String> etiquetasX = new ArrayList<>(datos.keySet());

        // Lista de entradas para el gráfico (índice, valor)
        List<BarEntry> entradas = new ArrayList<>();
        int index = 0;
        for (Integer valor : datos.values()) {
            entradas.add(new BarEntry(index, valor));
            index++;
        }

        // Crea el dataset con las barras y su color
        BarDataSet dataSet = new BarDataSet(entradas, etiqueta);
        dataSet.setColor(colorBarra);           // Color de las barras
        dataSet.setValueTextColor(COLOR_TEXT);  // Color del número sobre cada barra
        dataSet.setValueTextSize(11f);           // Tamaño del número sobre cada barra

        // El ValueFormatter hace que los valores se muestren como enteros (sin decimales)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Muestra "3" en lugar de "3.0"
            }
        });

        // Envuelve el dataset en un objeto BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f); // Ancho de cada barra (0 a 1)

        // --- Configuración del eje X ---
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(etiquetasX)); // Etiquetas mes
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Etiquetas abajo del gráfico
        xAxis.setGranularity(1f);     // Un tick por cada barra
        xAxis.setLabelCount(etiquetasX.size()); // Número de etiquetas igual a número de barras
        xAxis.setLabelRotationAngle(-30f); // Rotar para que quepan mejor
        xAxis.setTextColor(COLOR_TEXT);    // Color de texto del eje X
        xAxis.setDrawGridLines(false);     // Sin líneas de cuadrícula verticales

        // --- Configuración del eje Y izquierdo ---
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);         // El eje Y empieza en 0
        yAxisLeft.setTextColor(COLOR_TEXT);   // Color de texto del eje Y
        yAxisLeft.setGranularity(1f);         // Solo valores enteros en el eje Y
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Sin decimales en el eje Y
            }
        });

        // Oculta el eje Y derecho (redundante)
        chart.getAxisRight().setEnabled(false);

        // --- Apariencia general del gráfico ---
        chart.setData(barData);
        chart.getDescription().setEnabled(false);  // Sin descripción debajo del gráfico
        chart.getLegend().setTextColor(COLOR_TEXT); // Color de la leyenda
        chart.setBackgroundColor(Color.TRANSPARENT); // Fondo transparente para ver el fondo de la app
        chart.setFitBars(true);  // Ajusta el ancho para que las barras no se corten
        chart.animateY(800);     // Animación de aparición (800ms hacia arriba)
        chart.invalidate();      // Fuerza el redibujado del gráfico
    }

    // =====================================================
    // 📊 DIBUJAR BARCHART HORIZONTAL (POR USUARIO)
    // =====================================================
    /**
     * Dibuja un HorizontalBarChart (barras horizontales) donde:
     * - El eje Y izquierdo muestra el nombre/correo de cada usuario
     * - El eje X muestra la cantidad de dispositivos comprados
     *
     * Se usa horizontal porque los correos son textos largos
     * y se leen mejor en el eje vertical.
     *
     * @param datos Mapa de correo → cantidad de compras
     */
    private void dibujarBarChartHorizontal(Map<String, Integer> datos) {

        // Listas para etiquetas y valores
        List<String> correos   = new ArrayList<>(datos.keySet());
        List<BarEntry> entradas = new ArrayList<>();

        int index = 0;
        for (Integer valor : datos.values()) {
            entradas.add(new BarEntry(index, valor));
            index++;
        }

        // Dataset de barras horizontales
        BarDataSet dataSet = new BarDataSet(entradas, "Dispositivos por usuario");
        dataSet.setColor(COLOR_TEXT);           // Azul oscuro para diferenciar del gráfico global
        dataSet.setValueTextColor(COLOR_SURFACE); // Texto blanco sobre las barras
        dataSet.setValueTextSize(11f);

        // Valores enteros sin decimales
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        // --- Eje X del HorizontalBarChart = eje vertical (muestra correos) ---
        XAxis xAxis = barChartComprasPorUsuario.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(correos)); // Correos como etiquetas
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE); // Etiquetas dentro del gráfico
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(correos.size());
        xAxis.setTextColor(COLOR_TEXT);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f); // Tamaño pequeño para que quepan los correos

        // --- Eje Y del HorizontalBarChart = eje horizontal (muestra cantidades) ---
        YAxis yAxisLeft = barChartComprasPorUsuario.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setTextColor(COLOR_TEXT);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Oculta el eje Y derecho
        barChartComprasPorUsuario.getAxisRight().setEnabled(false);

        // Apariencia general
        barChartComprasPorUsuario.setData(barData);
        barChartComprasPorUsuario.getDescription().setEnabled(false);
        barChartComprasPorUsuario.getLegend().setTextColor(COLOR_TEXT);
        barChartComprasPorUsuario.setBackgroundColor(Color.TRANSPARENT);
        barChartComprasPorUsuario.setFitBars(true);
        barChartComprasPorUsuario.animateX(800); // Animación horizontal (desde la izquierda)
        barChartComprasPorUsuario.invalidate();  // Fuerza el redibujado
    }

    // =====================================================
    // ❌ MOSTRAR ERROR
    // =====================================================
    /**
     * Muestra un Toast de error sin crashear la app.
     * Se usa cuando Firebase devuelve un error de lectura.
     *
     * @param mensaje Texto del error a mostrar
     */
    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    // =====================================================
    // ⬅️ VOLVER AL MENÚ ADMIN
    // =====================================================
    /**
     * Cierra esta Activity y regresa al MenuAdmin.
     * Se llama desde el botón "Volver" del XML.
     *
     * @param view Vista del botón que disparó el evento
     */
    public void volver(View view) {
        finish(); // Cierra la pantalla actual y vuelve a la anterior del stack
    }
}
