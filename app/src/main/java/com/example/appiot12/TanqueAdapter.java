package com.example.appiot12;
// 📦 Adaptador que convierte TanqueAgua → fila visual (item_tanque.xml)
// Es como un traductor: datos técnicos → texto entendible 👶💧

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

/**
 * ⭐ TANQUE ADAPTER ⭐
 *
 * Explicado fácil:
 * 👉 Tenemos muchos tanques
 * 👉 Cada tanque puede o no tener sensores
 * 👉 Este adaptador los dibuja bonitos en una lista 📋✨
 *
 * Buenas prácticas:
 * ✔ ViewHolder (rápido)
 * ✔ Firebase solo una vez por fila
 * ✔ Sin crashes por reciclaje
 * ✔ Código claro y mantenible
 */
public class TanqueAdapter extends ArrayAdapter<TanqueAgua> {

    private final Context context;              // 🌍 Dónde se dibuja la lista
    private final List<TanqueAgua> tanques;     // 🛢 Lista de tanques

    public TanqueAdapter(Context context, List<TanqueAgua> tanques) {
        super(context, R.layout.item_tanque, tanques);
        this.context = context;
        this.tanques = tanques;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        // ======================================================
        // ♻️ VIEW HOLDER (reciclaje inteligente)
        // ======================================================
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_tanque, parent, false);

            holder = new ViewHolder();
            holder.tvNombre = convertView.findViewById(R.id.tvNombreTanque);
            holder.tvResumen = convertView.findViewById(R.id.tvResumenDatos);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TanqueAgua tanque = tanques.get(position);

        // ======================================================
        // 📝 NOMBRE DEL TANQUE
        // ======================================================
        holder.tvNombre.setText("Nombre: " + tanque.getNombre());

        // ======================================================
        // 🚫 TANQUE SIN DISPOSITIVO
        // ======================================================
        if (tanque.getIdDispositivo() == null || tanque.getIdDispositivo().isEmpty()) {
            mostrarSinDispositivo(holder);
            return convertView;
        }

        // Mientras Firebase responde ⏳
        holder.tvResumen.setText("Cargando sensores…");
        holder.tvResumen.setTextColor(Color.GRAY);

        // ======================================================
        // ☁️ LEER DISPOSITIVO DESDE FIREBASE
        // ======================================================
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

                        // Si la vista ya no existe → salimos (seguridad 🔐)
                        if (convertView.getParent() == null) return;

                        if (!snapshot.exists()) {
                            mostrarError(holder, "DISPOSITIVO NO ENCONTRADO");
                            return;
                        }

                        Dispositivo d = snapshot.getValue(Dispositivo.class);

                        if (d == null) {
                            mostrarError(holder, "ERROR DE LECTURA");
                            return;
                        }

                        // ======================================================
                        // 🧪 EVALUAR SENSORES
                        // ======================================================
                        String phEstado   = evaluarRango(d.getPh(), 6.5, 8.5);
                        String condEstado = evaluarRango(d.getConductividad(), 0, 700);
                        String turbEstado = evaluarRango(d.getTurbidez(), 0, 5);

                        // ======================================================
                        // 💧 NIVEL DEL TANQUE
                        // ======================================================
                        double capacidad = parseDoubleSeguro(tanque.getCapacidad());
                        double nivel = d.getUltrasonico();
                        String nivelEstado = evaluarNivel(nivel, capacidad);

                        // ======================================================
                        // 📊 TEXTO FINAL
                        // ======================================================
                        String resumen = String.format(
                                "pH %.1f (%s) | Cond %.0f (%s) | Turb %.1f (%s) | Agua %.0f L (%s)",
                                d.getPh(), phEstado,
                                d.getConductividad(), condEstado,
                                d.getTurbidez(), turbEstado,
                                nivel, nivelEstado
                        );

                        holder.tvResumen.setText(resumen);

                        // ======================================================
                        // 🚦 COLOR DE ALERTA
                        // ======================================================
                        boolean alerta =
                                !"OK".equals(phEstado) ||
                                        !"OK".equals(condEstado) ||
                                        !"OK".equals(turbEstado) ||
                                        "BAJO".equals(nivelEstado);

                        holder.tvResumen.setTextColor(
                                alerta ? Color.parseColor("#8B0000") : Color.parseColor("#006400")
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mostrarError(holder, "ERROR FIREBASE");
                    }
                });

        return convertView;
    }

    // ======================================================
    // 🧠 HELPERS (lógica reutilizable)
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
    // 📦 VIEW HOLDER
    // ======================================================
    static class ViewHolder {
        TextView tvNombre;
        TextView tvResumen;
    }
}
