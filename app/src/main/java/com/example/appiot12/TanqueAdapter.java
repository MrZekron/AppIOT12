package com.example.appiot12;
// 📦 Adaptador que convierte TanqueAgua → fila visual (item_tanque.xml)
// Traductor de datos técnicos → texto entendible 👶💧

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

        View row;
        ViewHolder holder;

        // ======================================================
        // ♻️ ViewHolder pattern (rendimiento + seguridad)
        // ======================================================
        if (convertView == null) {
            row = LayoutInflater.from(context)
                    .inflate(R.layout.item_tanque, parent, false);

            holder = new ViewHolder();
            holder.tvNombre = row.findViewById(R.id.tvNombreTanque);
            holder.tvResumen = row.findViewById(R.id.tvResumenDatos);

            row.setTag(holder);
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
        }

        TanqueAgua tanque = tanques.get(position);

        // Nombre del tanque
        holder.tvNombre.setText("Nombre: " + tanque.getNombre());

        // ======================================================
        // 🚫 Tanque sin dispositivo
        // ======================================================
        if (tanque.getIdDispositivo() == null || tanque.getIdDispositivo().isEmpty()) {
            mostrarSinDispositivo(holder);
            return row;
        }

        // Estado temporal
        holder.tvResumen.setText("Cargando sensores…");
        holder.tvResumen.setTextColor(Color.GRAY);

        // ======================================================
        // 🔐 CAPTURAS FINALES (CLAVE PARA NO CRASHEAR)
        // ======================================================
        final View rowFinal = row;
        final ViewHolder holderFinal = holder;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String idDispositivo = tanque.getIdDispositivo();

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(idDispositivo)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // 🛡️ Protección contra reciclaje de vistas
                        if (rowFinal.getParent() == null) return;

                        if (!snapshot.exists()) {
                            mostrarError(holderFinal, "DISPOSITIVO NO ENCONTRADO");
                            return;
                        }

                        Dispositivo d = snapshot.getValue(Dispositivo.class);
                        if (d == null) {
                            mostrarError(holderFinal, "ERROR DE LECTURA");
                            return;
                        }

                        // ==========================
                        // 🧪 Evaluación de sensores
                        // ==========================
                        String phEstado   = evaluarRango(d.getPh(), 6.5, 8.5);
                        String condEstado = evaluarRango(d.getConductividad(), 0, 700);
                        String turbEstado = evaluarRango(d.getTurbidez(), 0, 5);

                        double capacidad = parseDoubleSeguro(tanque.getCapacidad());
                        double nivel = d.getUltrasonico();
                        String nivelEstado = evaluarNivel(nivel, capacidad);

                        String resumen = String.format(
                                "pH %.1f (%s) | Cond %.0f (%s) | Turb %.1f (%s) | Agua %.0f L (%s)",
                                d.getPh(), phEstado,
                                d.getConductividad(), condEstado,
                                d.getTurbidez(), turbEstado,
                                nivel, nivelEstado
                        );

                        holderFinal.tvResumen.setText(resumen);

                        boolean alerta =
                                !"OK".equals(phEstado) ||
                                        !"OK".equals(condEstado) ||
                                        !"OK".equals(turbEstado) ||
                                        "BAJO".equals(nivelEstado);

                        holderFinal.tvResumen.setTextColor(
                                alerta
                                        ? Color.parseColor("#8B0000") // rojo alerta
                                        : Color.parseColor("#006400") // verde ok
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mostrarError(holderFinal, "ERROR FIREBASE");
                    }
                });

        return row;
    }

    // ======================================================
    // 🧠 HELPERS
    // ======================================================
    private void mostrarSinDispositivo(ViewHolder h) {
        h.tvResumen.setText("Estado: SIN DISPOSITIVO");
        h.tvResumen.setTextColor(Color.GRAY);
    }

    private void mostrarError(ViewHolder h, String msg) {
        h.tvResumen.setText(msg);
        h.tvResumen.setTextColor(Color.RED);
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

    // ======================================================
    // 📦 ViewHolder
    // ======================================================
    static class ViewHolder {
        TextView tvNombre;
        TextView tvResumen;
    }
}
