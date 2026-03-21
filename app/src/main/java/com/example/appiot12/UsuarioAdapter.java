package com.example.appiot12;

// 📦 Adaptador visual para el panel de administración.
// SOLO muestra datos, no contiene lógica de negocio pesada.

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * 🌟 UsuarioAdapter 🌟
 *
 * Muestra:
 * ✔ correo
 * ✔ estado de cuenta
 * ✔ deuda
 * ✔ días de atraso
 */
public class UsuarioAdapter extends ArrayAdapter<Usuario> {

    private final Context context;
    private final List<Usuario> usuarios;

    public UsuarioAdapter(Context context, List<Usuario> usuarios) {
        super(context, R.layout.item_usuario, usuarios);
        this.context = context;
        this.usuarios = usuarios;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolder holder;

        // =========================
        // 🧠 ViewHolder pattern
        // =========================
        if (row == null) {

            row = LayoutInflater.from(context)
                    .inflate(R.layout.item_usuario, parent, false);

            holder = new ViewHolder();
            holder.tvCorreo = row.findViewById(R.id.tvCorreo);
            holder.tvEstado = row.findViewById(R.id.tvEstadoCuenta);
            holder.tvDeuda = row.findViewById(R.id.tvDeuda);
            holder.tvAtraso = row.findViewById(R.id.tvTiempoAtraso);
            holder.btnBloquear = row.findViewById(R.id.btnBloquear);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Usuario usuario = usuarios.get(position);

        // =========================
        // 📧 Correo
        // =========================
        holder.tvCorreo.setText(usuario.getCorreo());

        // =========================
        // 🔐 Estado
        // =========================
        if (usuario.isBloqueado()) {
            holder.tvEstado.setText("Cuenta bloqueada ❌");
            holder.tvEstado.setTextColor(Color.RED);
            holder.btnBloquear.setText("Desbloquear");
        } else {
            holder.tvEstado.setText("Cuenta activa ✔");
            holder.tvEstado.setTextColor(Color.GREEN);
            holder.btnBloquear.setText("Bloquear");
        }

        // =========================
        // 💰 Placeholder mientras carga
        // =========================
        holder.tvDeuda.setText("Cargando…");
        holder.tvDeuda.setTextColor(Color.GRAY);

        holder.tvAtraso.setText("Calculando…");
        holder.tvAtraso.setTextColor(Color.GRAY);

        // 🔄 Cargar datos reales
        cargarResumenFinanciero(usuario.getId(), holder);

        // =========================
        // 🚫 Bloquear / desbloquear
        // =========================
        holder.btnBloquear.setOnClickListener(v -> {

            boolean nuevoEstado = !usuario.isBloqueado();
            usuario.setBloqueado(nuevoEstado);

            FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(usuario.getId())
                    .child("bloqueado")
                    .setValue(nuevoEstado);

            notifyDataSetChanged();
        });

        return row;
    }

    // ============================================================
    // 📊 CARGAR DEUDA + ATRASO
    // ============================================================
    private void cargarResumenFinanciero(String userId, ViewHolder holder) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(userId)
                .child("pagos")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            holder.tvDeuda.setText("Sin compras");
                            holder.tvDeuda.setTextColor(Color.GRAY);

                            holder.tvAtraso.setText("Atraso: 0 días");
                            holder.tvAtraso.setTextColor(Color.GREEN);
                            return;
                        }

                        int deuda = 0;
                        long ultimaFecha = 0;

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Pago pago = snap.getValue(Pago.class);
                            if (pago == null) continue;

                            deuda += pago.getSaldoPendiente();

                            // 🔥 CORRECCIÓN CLAVE
                            long fecha = obtenerFechaMovimiento(pago);
                            if (fecha > ultimaFecha) {
                                ultimaFecha = fecha;
                            }
                        }

                        // =========================
                        // 💰 DEUDA
                        // =========================
                        if (deuda == 0) {
                            holder.tvDeuda.setText("Al día ✔");
                            holder.tvDeuda.setTextColor(Color.GREEN);
                        } else {
                            holder.tvDeuda.setText("Debe $" + deuda);
                            holder.tvDeuda.setTextColor(Color.RED);
                        }

                        // =========================
                        // 📅 ATRASO
                        // =========================
                        long dias = 0;

                        if (ultimaFecha > 0 && deuda > 0) {
                            dias = (System.currentTimeMillis() - ultimaFecha)
                                    / (1000L * 60L * 60L * 24L);
                        }

                        holder.tvAtraso.setText("Atraso: " + dias + " días");

                        if (dias == 0) {
                            holder.tvAtraso.setTextColor(Color.GREEN);
                        } else if (dias <= 15) {
                            holder.tvAtraso.setTextColor(Color.parseColor("#FBC02D"));
                        } else {
                            holder.tvAtraso.setTextColor(Color.RED);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        holder.tvDeuda.setText("Error ❌");
                        holder.tvDeuda.setTextColor(Color.RED);

                        holder.tvAtraso.setText("Error");
                        holder.tvAtraso.setTextColor(Color.RED);
                    }
                });
    }

    // ============================================================
    // 🕒 OBTENER FECHA VÁLIDA DEL PAGO
    // ============================================================
    private long obtenerFechaMovimiento(Pago pago) {

        if (pago.getUltimaActualizacion() > 0) {
            return pago.getUltimaActualizacion();
        }

        if (pago.getFechaCreacion() > 0) {
            return pago.getFechaCreacion();
        }

        return 0;
    }

    // ============================================================
    // 🧱 VIEW HOLDER
    // ============================================================
    static class ViewHolder {
        TextView tvCorreo, tvEstado, tvDeuda, tvAtraso;
        Button btnBloquear;
    }
}