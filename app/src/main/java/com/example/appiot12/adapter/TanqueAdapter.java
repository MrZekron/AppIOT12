package com.example.appiot12.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.appiot12.R;
import com.example.appiot12.model.Dispositivo;
import com.example.appiot12.model.TanqueAgua;
import com.google.firebase.auth.FirebaseAuth;
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
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tanque, parent, false);
            holder = new ViewHolder();
            holder.tvNombre = convertView.findViewById(R.id.tvNombreTanque);
            holder.tvResumen = convertView.findViewById(R.id.tvResumenDatos);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TanqueAgua tanque = tanques.get(position);
        holder.tvNombre.setText("Nombre: " + tanque.getNombre());

        if (tanque.getIdDispositivo() == null || tanque.getIdDispositivo().isEmpty()) {
            holder.tvResumen.setText("Estado: SIN DISPOSITIVO");
            holder.tvResumen.setTextColor(Color.GRAY);
            return convertView;
        }

        holder.tvResumen.setText("Cargando sensores…");
        holder.tvResumen.setTextColor(Color.GRAY);

        final View rowFinal = convertView;
        final ViewHolder holderFinal = holder;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(tanque.getIdDispositivo())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (rowFinal.getParent() == null) return;

                        if (!snapshot.exists()) {
                            holderFinal.tvResumen.setText("DISPOSITIVO NO ENCONTRADO");
                            holderFinal.tvResumen.setTextColor(Color.RED);
                            return;
                        }

                        Dispositivo d = snapshot.getValue(Dispositivo.class);
                        if (d == null) {
                            holderFinal.tvResumen.setText("ERROR DE LECTURA");
                            holderFinal.tvResumen.setTextColor(Color.RED);
                            return;
                        }

                        String phEstado   = evaluarRango(d.getPh(), 6.5, 8.5);
                        String condEstado = evaluarRango(d.getConductividad(), 0, 700);
                        String turbEstado = evaluarRango(d.getTurbidez(), 0, 5);
                        double capacidad  = parseDoubleSeguro(tanque.getCapacidad());
                        double nivel      = d.getUltrasonico();
                        String nivelEstado = evaluarNivel(nivel, capacidad);

                        holderFinal.tvResumen.setText(String.format(
                                "pH %.1f (%s) | Cond %.0f (%s) | Turb %.1f (%s) | Agua %.0f L (%s)",
                                d.getPh(), phEstado, d.getConductividad(), condEstado,
                                d.getTurbidez(), turbEstado, nivel, nivelEstado));

                        boolean alerta = !"OK".equals(phEstado) || !"OK".equals(condEstado)
                                || !"OK".equals(turbEstado) || "BAJO".equals(nivelEstado);

                        holderFinal.tvResumen.setTextColor(alerta
                                ? Color.parseColor("#8B0000")
                                : Color.parseColor("#006400"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holderFinal.tvResumen.setText("ERROR FIREBASE");
                        holderFinal.tvResumen.setTextColor(Color.RED);
                    }
                });

        return convertView;
    }

    private String evaluarRango(double v, double min, double max) {
        if (v < min) return "BAJO";
        if (v > max) return "ALTO";
        return "OK";
    }

    private String evaluarNivel(double nivel, double capacidad) {
        if (capacidad <= 0) return "SIN DATA";
        if (nivel < capacidad * 0.25) return "BAJO";
        if (nivel < capacidad) return "MEDIO";
        return "LLENO";
    }

    private double parseDoubleSeguro(String v) {
        try {
            return Double.parseDouble(v);
        } catch (Exception e) {
            return 0;
        }
    }

    static class ViewHolder {
        TextView tvNombre;
        TextView tvResumen;
    }
}
