package com.example.appiot12.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.appiot12.model.HistorialEvento;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador que transforma los objetos HistorialEvento
 * en elementos visuales dentro del ListView.
 *
 * Cada fila muestra:
 * - Tipo de evento
 * - Descripción
 * - Fecha y hora
 */
public class HistorialAdapter extends ArrayAdapter<HistorialEvento> {

    // Formato de fecha para mostrar en pantalla
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    /**
     * Constructor del adapter.
     *
     * @param context contexto de la activity
     * @param eventos lista de eventos del historial
     */
    public HistorialAdapter(Context context, List<HistorialEvento> eventos) {
        super(context, 0, eventos);
    }

    /**
     * Este método crea o reutiliza cada fila del ListView
     * y le asigna los datos del evento correspondiente.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        HistorialEvento evento = getItem(position);

        // Si la vista no existe aún, se crea
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        // Referencias a los textos del layout
        TextView t1 = convertView.findViewById(android.R.id.text1);
        TextView t2 = convertView.findViewById(android.R.id.text2);

        if (evento != null) {

            // Primera línea: tipo + descripción
            String linea1 = "[" + safe(evento.getTipo()) + "] "
                    + safe(evento.getDescripcion());

            // Segunda línea: fecha formateada
            String linea2 = sdf.format(new Date(evento.getTimestamp()));

            t1.setText(linea1);
            t2.setText(linea2);
        }

        return convertView;
    }

    /**
     * Evita que aparezca "null" en pantalla si el texto no existe.
     */
    private String safe(String texto) {
        return texto == null ? "" : texto;
    }
}
