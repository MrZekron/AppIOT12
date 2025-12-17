package com.example.appiot12;
// 📦 Adaptador visual para el panel de administración.
// Su misión es MOSTRAR información, no gobernar el sistema 👀🎨

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
 * 🌟 ADAPTADOR DE USUARIOS (ADMIN) 🌟
 *
 * Muestra por usuario:
 * ✔ Correo
 * ✔ Estado (activo / bloqueado)
 * ✔ Deuda pendiente
 * ✔ Días de atraso estimados
 *
 * ⚠️ NO contiene reglas de negocio complejas
 * ⚠️ NO decide políticas del sistema
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

        // ============================================================
        // 🧠 ViewHolder Pattern → rápido y seguro
        // ============================================================
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

        // ============================================================
        // 📧 Correo
        // ============================================================
        holder.tvCorreo.setText(usuario.getCorreo());

        // ============================================================
        // 🔐 Estado de cuenta
        // ============================================================
        if (usuario.isBloqueado()) {
            holder.tvEstado.setText("Cuenta bloqueada ❌");
            holder.tvEstado.setTextColor(Color.RED);
            holder.btnBloquear.setText("Desbloquear");
        } else {
            holder.tvEstado.setText("Cuenta activa ✔");
            holder.tvEstado.setTextColor(Color.GREEN);
            holder.btnBloquear.setText("Bloquear");
        }

        // ============================================================
        // 💰 Estado financiero (placeholder mientras carga)
        // ============================================================
        holder.tvDeuda.setText("Cargando…");
        holder.tvDeuda.setTextColor(Color.GRAY);

        holder.tvAtraso.setText("Calculando…");
        holder.tvAtraso.setTextColor(Color.GRAY);

        // Cargar deuda real desde Firebase
        cargarResumenFinanciero(usuario.getId(), holder);

        // ============================================================
        // 🚫 Bloquear / Desbloquear usuario
        // ============================================================
        holder.btnBloquear.setOnClickListener(v -> {

            boolean nuevoEstado = !usuario.isBloqueado();
            usuario.setBloqueado(nuevoEstado);

            FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(usuario.getId())
                    .child("bloqueado")
                    .setValue(nuevoEstado);

            notifyDataSetChanged(); // Refrescar vista
        });

        return row;
    }

    // ============================================================
    // 📊 Cargar deuda + atraso (solo lectura)
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
                            ultimaFecha = Math.max(ultimaFecha, pago.getFechaPago());
                        }

                        // 💰 Mostrar deuda
                        if (deuda == 0) {
                            holder.tvDeuda.setText("Al día ✔");
                            holder.tvDeuda.setTextColor(Color.GREEN);
                        } else {
                            holder.tvDeuda.setText("Debe $" + deuda);
                            holder.tvDeuda.setTextColor(Color.RED);
                        }

                        // 📅 Calcular atraso
                        long dias = (System.currentTimeMillis() - ultimaFecha)
                                / (1000 * 60 * 60 * 24);

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
    // 🧱 ViewHolder
    // ============================================================
    static class ViewHolder {
        TextView tvCorreo, tvEstado, tvDeuda, tvAtraso;
        Button btnBloquear;
    }
}
