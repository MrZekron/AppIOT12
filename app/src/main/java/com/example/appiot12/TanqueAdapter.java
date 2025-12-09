package com.example.appiot12;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TanqueAdapter extends ArrayAdapter<TanqueAgua> {

    private final Context context;
    private final List<TanqueAgua> tanques;

    public TanqueAdapter(Context context, List<TanqueAgua> tanques) {
        super(context, R.layout.item_tanque, tanques);
        this.context = context;
        this.tanques = tanques;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.item_tanque, parent, false);
            holder = new ViewHolder();

            holder.tvNombreTanque = row.findViewById(R.id.tvNombreTanque);
            holder.tvResumenDatos = row.findViewById(R.id.tvResumenDatos);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        TanqueAgua tanque = tanques.get(position);

        holder.tvNombreTanque.setText("Nombre: " + tanque.getNombre());

        // =============================
        //   SIN DISPOSITIVO ASIGNADO
        // =============================
        if (tanque.getIdDispositivo() == null || tanque.getIdDispositivo().isEmpty()) {
            holder.tvResumenDatos.setText("Estado: SIN DISPOSITIVO");
            holder.tvResumenDatos.setTextColor(Color.GRAY);
            return row;
        }

        // =============================
        //   CARGANDO DATOS DEL DISPOSITIVO
        // =============================
        holder.tvResumenDatos.setText("Cargando sensores...");
        holder.tvResumenDatos.setTextColor(Color.GRAY);

        FirebaseDatabase.getInstance()
                .getReference("dispositivos")
                .child(tanque.getIdDispositivo())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            holder.tvResumenDatos.setText("DISPOSITIVO NO ENCONTRADO");
                            holder.tvResumenDatos.setTextColor(Color.RED);
                            return;
                        }

                        Dispositivo dispositivo = snapshot.getValue(Dispositivo.class);

                        if (dispositivo == null) {
                            holder.tvResumenDatos.setText("Error al leer dispositivo");
                            holder.tvResumenDatos.setTextColor(Color.RED);
                            return;
                        }

                        // =============================
                        //     PROCESAR SENSORES
                        // =============================
                        String estadoPH = evaluarRango(dispositivo.getPh(), 0, 14);
                        String estadoConductividad = evaluarRango(dispositivo.getConductividad(), 400, 800);
                        String estadoTurbidez = evaluarRango(dispositivo.getTurbidez(), 1, 5);

                        double capacidad;
                        try {
                            capacidad = Double.parseDouble(tanque.getCapacidad());
                        } catch (Exception e) {
                            capacidad = 0;
                        }

                        double nivel = dispositivo.getUltrasonico();
                        String estadoNivel = "SIN DATA";

                        if (capacidad > 0) {
                            if (nivel < capacidad * 0.25) estadoNivel = "BAJO";
                            else if (nivel < capacidad) estadoNivel = "MEDIO";
                            else estadoNivel = "LLENO";
                        }

                        // =============================
                        //   ARMAR RESUMEN DE SENSORES
                        // =============================
                        String resumen = String.format(
                                "pH %.1f (%s) | Cond %.0f (%s) | Turb %.1f (%s) | Agua %.0f L (%s)",
                                dispositivo.getPh(), estadoCorto(estadoPH),
                                dispositivo.getConductividad(), estadoCorto(estadoConductividad),
                                dispositivo.getTurbidez(), estadoCorto(estadoTurbidez),
                                nivel, estadoNivel
                        );

                        holder.tvResumenDatos.setText(resumen);

                        // =============================
                        //    COLORES DEPENDIENDO ALERTA
                        // =============================
                        boolean alerta =
                                !"OK".equals(estadoCorto(estadoPH)) ||
                                        !"OK".equals(estadoCorto(estadoConductividad)) ||
                                        !"OK".equals(estadoCorto(estadoTurbidez)) ||
                                        "BAJO".equals(estadoNivel);

                        int verde = Color.parseColor("#006400");
                        int rojo = Color.parseColor("#8B0000");

                        holder.tvResumenDatos.setTextColor(alerta ? rojo : verde);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        holder.tvResumenDatos.setText("ERROR");
                        holder.tvResumenDatos.setTextColor(Color.RED);
                    }
                });

        return row;
    }

    private static class ViewHolder {
        TextView tvNombreTanque;
        TextView tvResumenDatos;
    }

    private String evaluarRango(double v, double min, double max) {
        if (v < min) return "BAJO";
        if (v > max) return "ALTO";
        return "OK";
    }

    private String estadoCorto(String e) {
        switch (e) {
            case "OK": return "OK";
            case "BAJO": return "BAJO";
            case "ALTO": return "ALTO";
            default: return "--";
        }
    }
}
