package com.example.appiot12; // 📦 Este archivo vive dentro del paquete principal de la app

import android.content.Context; // 🌍 Contexto: información del entorno de la app
import android.graphics.Color; // 🎨 Para poner colores a los textos
import android.view.LayoutInflater; // 🏗 Para inflar (crear) layouts XML en objetos View
import android.view.View; // 👀 Representa una vista en pantalla
import android.view.ViewGroup; // 📐 Contenedor de vistas (padre de las filas)
import android.widget.TextView; // 📝 Para mostrar texto
import android.widget.ArrayAdapter; // 📋 Adaptador base sencillo

import androidx.annotation.NonNull;

import java.util.List; // 🗂 Lista para manejar muchos tanques

// 🧾 ADAPTADOR DE TANQUES 🏺
// Esta clase se encarga de “pintar” cada tanque en la lista visual.
// Traduce objetos TanqueAgua → filas visibles en un ListView.
public class TanqueAdapter extends ArrayAdapter<TanqueAgua> {

    private final Context context;      // 📍 Dónde estamos (Activity, app, etc.)
    private final List<TanqueAgua> tanques; // 🏺 Lista de tanques a mostrar

    // 🧱 Constructor: recibe el contexto y la lista de tanques
    public TanqueAdapter(Context context, List<TanqueAgua> tanques) {
        super(context, R.layout.item_tanque, tanques); // 🔗 Le decimos al padre qué layout usar
        this.context = context;
        this.tanques = tanques;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 🔄 getView se llama UNA VEZ POR CADA FILA de la lista
        // position = índice del tanque (0, 1, 2, ...)
        View row = convertView; // 🧱 Fila que vamos a reutilizar (si existe)
        ViewHolder holder;      // 📦 Guarda referencias para no buscar vistas a cada rato (optimización)

        if (row == null) {
            // 🆕 Si no hay vista para reutilizar, inflamos una nueva desde el XML item_tanque
            row = LayoutInflater.from(context).inflate(R.layout.item_tanque, parent, false);

            // 🧺 Creamos el ViewHolder y conectamos con los TextView del layout
            holder = new ViewHolder();
            holder.tvNombreTanque = row.findViewById(R.id.tvNombreTanque);   // 🏷 Nombre del tanque
            holder.tvResumenDatos = row.findViewById(R.id.tvResumenDatos);   // 📊 Resumen de sensores

            // 📌 Guardamos el ViewHolder dentro de la vista para reusarlo después
            row.setTag(holder);
        } else {
            // 🔁 Si row no es null, recuperamos el ViewHolder que ya tenía
            holder = (ViewHolder) row.getTag();
        }

        // 🧱 Obtenemos el tanque que toca en esta posición
        TanqueAgua tanque = tanques.get(position);

        // 🏷 Mostramos el nombre del tanque
        holder.tvNombreTanque.setText("Nombre: " + tanque.getNombre());

        // 🔌 Obtenemos el dispositivo asociado (los sensores del tanque)
        Dispositivo dispositivo = tanque.getDispositivo();

        if (dispositivo != null) {
            // ✅ Si hay dispositivo, podemos mostrar datos reales de sensores

            // 🧪 Evaluamos si el pH, conductividad y turbidez están en rangos buenos o malos
            String estadoPH = evaluarRango(dispositivo.getPh(), 0, 14);             // pH entre 0 y 14
            String estadoConductividad = evaluarRango(dispositivo.getConductividad(), 400, 800); // rango “normal”
            String estadoTurbidez = evaluarRango(dispositivo.getTurbidez(), 1, 5);  // turbidez baja

            // 💧 Nivel de agua:
            // Usamos la capacidad del tanque (capacidad máxima) y la lectura de ultrasonido
            double capacidad;
            try {
                // 💾 Convertimos el texto de capacidad a número
                capacidad = Double.parseDouble(tanque.getCapacidad());
            } catch (NumberFormatException e) {
                // 😵 Si no se puede convertir, asumimos capacidad 0
                capacidad = 0;
            }

            // 📏 Cantidad actual de agua medida por el sensor ultrasónico (en litros o equivalente)
            double cantidadAguaActual = dispositivo.getUltrasonico();

            // 🔎 Calculamos en qué nivel está el agua según la capacidad
            String estadoNivel;
            if (capacidad == 0) {
                // 🤷‍♂️ No sabemos la capacidad, así que no podemos calcular
                estadoNivel = "SIN CAP.";
            } else if (cantidadAguaActual < capacidad * 0.25) {
                // ⛔ Menos del 25% → nivel bajo
                estadoNivel = "BAJO";
            } else if (cantidadAguaActual < capacidad) {
                // ⚠ Entre 25% y 100% → nivel medio
                estadoNivel = "MEDIO";
            } else {
                // 💯 Igual o más que capacidad → lleno
                estadoNivel = "LLENO";
            }

            // 📏 Hacemos versiones cortas de los estados de pH, conductividad y turbidez
            String phCorto = estadoCorto(estadoPH);
            String condCorto = estadoCorto(estadoConductividad);
            String turbCorto = estadoCorto(estadoTurbidez);

            // 🧾 Resumen compacto en una sola línea con todos los datos clave
            String resumen = String.format(
                    "pH %.1f (%s) | Cond %.0f (%s) | Turb %.1f (%s) | Agua %.0f L (%s)",
                    dispositivo.getPh(), phCorto,
                    dispositivo.getConductividad(), condCorto,
                    dispositivo.getTurbidez(), turbCorto,
                    cantidadAguaActual, estadoNivel
            );
            holder.tvResumenDatos.setText(resumen);

            // 🎨 Elegimos el color del texto según si algo está fuera de rango
            boolean alerta =
                    !"OK".equals(phCorto) ||        // pH no está “OK”
                            !"OK".equals(condCorto) ||      // conductividad no está “OK”
                            !"OK".equals(turbCorto) ||      // turbidez no está “OK”
                            "BAJO".equals(estadoNivel);     // o el nivel de agua está bajo

            int verdeOscuro = Color.parseColor("#006400"); // 🟢 Todo bien
            int rojoOscuro = Color.parseColor("#8B0000");  // 🔴 Algo anda mal

            // 🖌️ Si hay alerta → rojo, si no → verde
            holder.tvResumenDatos.setTextColor(alerta ? rojoOscuro : verdeOscuro);

        } else {
            // 🚫 Si no hay dispositivo, no tenemos datos de sensores
            holder.tvResumenDatos.setText("Estado: sin datos de dispositivo");
            holder.tvResumenDatos.setTextColor(Color.GRAY); // ⚪ Gris = sin info
        }

        // ✅ Devolvemos la fila ya configurada para que se muestre en la lista
        return row;
    }

    // 🧺 Clase interna para mejorar rendimiento (ViewHolder pattern)
    // Así no buscamos findViewById en cada llamada a getView, solo la primera vez.
    private static class ViewHolder {
        TextView tvNombreTanque;  // 🏷 Muestra el nombre del tanque
        TextView tvResumenDatos;  // 📊 Muestra el resumen de sensores
    }

    // 🔍 Evalúa si el valor está dentro del rango, más bajo o más alto
    private String evaluarRango(double valor, double min, double max) {
        if (valor < min) {
            // ⬇ Valor por debajo de lo permitido
            return "menor de los parámetros permitidos";
        } else if (valor > max) {
            // ⬆ Valor por encima de lo permitido
            return "mayor de los parámetros permitidos";
        } else {
            // ✅ Dentro de lo normal
            return "dentro de los parámetros";
        }
    }

    // 🧾 Versión corta de los estados para que el resumen no sea tan largo
    private String estadoCorto(String estadoLargo) {
        switch (estadoLargo) {
            case "dentro de los parámetros":
                return "OK";   // ✅ Todo bien
            case "menor de los parámetros permitidos":
                return "BAJO"; // ⬇ Por debajo
            case "mayor de los parámetros permitidos":
                return "ALTO"; // ⬆ Por encima
            default:
                return "--";   // ❓ Desconocido
        }
    }
}
