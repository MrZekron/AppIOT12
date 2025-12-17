package com.example.appiot12;
// 📦 Paquete base del proyecto Agua Segura.
// Mantiene todo ordenado, como una oficina bien gestionada 🗂️🚀

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
// 📱 Herramientas para crear y manejar filas dentro de una lista

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
// ⏰ Utilidades para manejar fechas de forma bonita y entendible

/**
 * 🧾 AccionAdapter
 *
 * Este adaptador se encarga de:
 * 👉 Tomar una lista de acciones (AccionLog)
 * 👉 Transformarlas en filas visuales
 * 👉 Mostrarlas en un ListView
 *
 * En simple:
 * Es el traductor entre los datos y lo que ve el usuario 👀📊
 */
public class AccionAdapter extends ArrayAdapter<AccionLog> {

    private final Context context;          // 🏢 Contexto de la app (dónde estamos parados)
    private final List<AccionLog> acciones; // 📝 Lista de acciones registradas (historial)

    /**
     * 🛠️ Constructor del adaptador
     *
     * @param context  contexto de la aplicación
     * @param acciones lista de acciones a mostrar
     */
    public AccionAdapter(@NonNull Context context, @NonNull List<AccionLog> acciones) {
        super(context, R.layout.item_accion, acciones);
        // 📌 Le decimos al ArrayAdapter qué layout usar y qué datos manejar

        this.context = context;
        this.acciones = acciones;
    }

    /**
     * 🧱 ViewHolder
     *
     * Truco profesional para:
     * ✅ No buscar las vistas una y otra vez
     * ✅ Mejorar rendimiento
     * ✅ Evitar código redundante
     *
     * Dicho fácil:
     * Guardamos las piezas para no armarlas de nuevo 🔩🙂
     */
    private static class ViewHolder {
        TextView txtDescripcion; // 📘 Texto de la acción
        TextView txtFecha;       // ⏰ Texto de la fecha
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // 🔄 Este método se llama cada vez que se muestra una fila en la lista

        ViewHolder holder;

        // 🧐 Si la fila no existe, la creamos
        if (convertView == null) {

            // 🧱 Inflamos el layout del item
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_accion, parent, false);

            // 🧰 Creamos el ViewHolder
            holder = new ViewHolder();

            // 🔍 Buscamos los TextView dentro del layout
            holder.txtDescripcion = convertView.findViewById(R.id.txtDescripcion);
            holder.txtFecha = convertView.findViewById(R.id.txtFecha);

            // 🏷️ Guardamos el holder dentro de la vista
            convertView.setTag(holder);

        } else {
            // ♻️ Si la fila ya existe, reutilizamos lo que ya estaba listo
            holder = (ViewHolder) convertView.getTag();
        }

        // 📂 Obtenemos la acción correspondiente a esta posición
        AccionLog accion = acciones.get(position);

        // 📝 Mostramos la descripción de la acción
        holder.txtDescripcion.setText(accion.getDescripcion());

        // 🕒 Convertimos el timestamp en una fecha entendible
        String fechaFormateada = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date(accion.getTimestamp()));

        // 📅 Mostramos la fecha en pantalla
        holder.txtFecha.setText(fechaFormateada);

        // ✅ Devolvemos la fila lista para mostrarse
        return convertView;
    }
}
