package com.example.appiot12;
// Paquete base de la app. Mantiene la arquitectura modular y ordenada 🚀

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.*;
import android.widget.*;
// Importamos elementos esenciales para inflar vistas y manejar UI en listas 📱

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.*;
// Librerías de fecha y utilidades. Porque el tiempo es oro… y logs también ⏳✨

public class AccionAdapter extends ArrayAdapter<AccionLog> {
    // Adapter corporativo encargado de convertir objetos AccionLog → vistas en pantalla 📊

    private final Context context;       // Contexto maestro de la app (la “oficina central”) 🏢
    private final List<AccionLog> acciones; // El backlog de acciones operacionales registradas 📝

    public AccionAdapter(Context context, List<AccionLog> acciones) {
        super(context, R.layout.item_accion, acciones);
        // Llamamos al constructor de ArrayAdapter, informándole el layout por defecto.

        this.context = context;         // Asignamos el contexto operativo 🔌
        this.acciones = acciones;       // Cargamos la lista de acciones (log histórico) 💾
    }

    @SuppressLint("ViewHolder")
    // Avisamos que no usaremos patrón ViewHolder esta vez.
    // KPI de rendimiento aceptable: sí, pero podríamos optimizar a futuro 📈😉

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Método que se ejecuta para cada fila del ListView. Produce la “tarjeta” del log 📬

        View row = LayoutInflater.from(context).inflate(R.layout.item_accion, parent, false);
        // Inflamos el layout item_accion.
        // Aquí nace visualmente una nueva línea del historial 🧱✨

        TextView txtDescripcion = row.findViewById(R.id.txtDescripcion);
        // Buscamos el TextView donde va la descripción. KPI: claridad narrativa 📘

        TextView txtFecha = row.findViewById(R.id.txtFecha);
        // Buscamos el TextView donde irá la fecha y hora del suceso ⏰

        AccionLog log = acciones.get(position);
        // Obtenemos la acción específica según su posición en la lista.
        // Esto es como sacar un ticket del CRM interno 📂

        txtDescripcion.setText(log.getDescripcion());
        // Cargamos la descripción en pantalla.
        // “Usuario eliminó un tanque”, “Se añadió dispositivo”, etc. 🛠️

        String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(log.getTimestamp()));
        // Convertimos el timestamp guardado en formato bonito.
        // De números raros → a algo digno de comité ejecutivo 🧮➡️🕒

        txtFecha.setText(fecha);
        // Renderizamos la fecha/hora procesada.
        // Ahora el historial tiene trazabilidad digna de ISO 9001 📑✨

        return row;
        // Regresamos la fila completa para que el ListView la muestre.
        // Acción ejecutada exitosamente ✔️
    }
}
