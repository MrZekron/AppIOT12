package com.example.appiot12;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class AccionAdapter extends ArrayAdapter<AccionLog> {

    private final Context context;
    private final List<AccionLog> acciones;

    public AccionAdapter(Context context, List<AccionLog> acciones) {
        super(context, R.layout.item_accion, acciones);
        this.context = context;
        this.acciones = acciones;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = LayoutInflater.from(context).inflate(R.layout.item_accion, parent, false);

        TextView txtDescripcion = row.findViewById(R.id.txtDescripcion);
        TextView txtFecha = row.findViewById(R.id.txtFecha);

        AccionLog log = acciones.get(position);

        txtDescripcion.setText(log.getDescripcion());

        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(log.getTimestamp()));

        txtFecha.setText(fecha);

        return row;
    }
}
