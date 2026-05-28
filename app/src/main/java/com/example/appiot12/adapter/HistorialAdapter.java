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

public class HistorialAdapter extends ArrayAdapter<HistorialEvento> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public HistorialAdapter(Context context, List<HistorialEvento> eventos) {
        super(context, 0, eventos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistorialEvento evento = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView t1 = convertView.findViewById(android.R.id.text1);
        TextView t2 = convertView.findViewById(android.R.id.text2);

        if (evento != null) {
            t1.setText("[" + safe(evento.getTipo()) + "] " + safe(evento.getDescripcion()));
            t2.setText(sdf.format(new Date(evento.getTimestamp())));
        }

        return convertView;
    }

    private String safe(String texto) {
        return texto == null ? "" : texto;
    }
}
