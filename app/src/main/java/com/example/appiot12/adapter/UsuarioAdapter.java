package com.example.appiot12.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.appiot12.R;
import com.example.appiot12.model.Pago;
import com.example.appiot12.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UsuarioAdapter extends ArrayAdapter<Usuario> {

    public interface OnBlockListener {
        void onBlock(Usuario usuario, boolean nuevoEstado);
    }

    private final Context context;
    private final List<Usuario> usuarios;
    private OnBlockListener blockListener;

    public UsuarioAdapter(Context context, List<Usuario> usuarios) {
        super(context, R.layout.item_usuario, usuarios);
        this.context = context;
        this.usuarios = usuarios;
    }

    public void setOnBlockListener(OnBlockListener listener) {
        this.blockListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);
            holder = new ViewHolder();
            holder.tvCorreo = convertView.findViewById(R.id.tvCorreo);
            holder.tvEstado = convertView.findViewById(R.id.tvEstadoCuenta);
            holder.tvDeuda = convertView.findViewById(R.id.tvDeuda);
            holder.tvAtraso = convertView.findViewById(R.id.tvTiempoAtraso);
            holder.tvCuotas = convertView.findViewById(R.id.tvCuotas);
            holder.tvDispositivos = convertView.findViewById(R.id.tvDispositivos);
            holder.btnBloquear = convertView.findViewById(R.id.btnBloquear);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Usuario usuario = usuarios.get(position);
        String display = usuario.getNombre() != null ? usuario.getNombre() : usuario.getEmail();
        holder.tvCorreo.setText(display);

        if (!usuario.isActivo()) {
            holder.tvEstado.setText("Cuenta bloqueada");
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_error));
            holder.btnBloquear.setText("Desbloquear");
        } else {
            holder.tvEstado.setText("Cuenta activa");
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.color_success));
            holder.btnBloquear.setText("Bloquear");
        }

        holder.tvDeuda.setText("Cargando…");
        holder.tvDeuda.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
        holder.tvAtraso.setText("Calculando…");
        holder.tvAtraso.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
        holder.tvCuotas.setText("Calculando…");
        holder.tvCuotas.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
        holder.tvDispositivos.setText("Cargando…");
        holder.tvDispositivos.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));

        cargarResumenFinanciero(usuario.getId(), holder);
        cargarConteoDispositivos(usuario.getId(), holder);

        holder.btnBloquear.setOnClickListener(v -> {
            if (blockListener != null) {
                blockListener.onBlock(usuario, !usuario.isActivo());
            }
        });

        return convertView;
    }

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
                            holder.tvDeuda.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
                            holder.tvAtraso.setText("Atraso: 0 días");
                            holder.tvAtraso.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                            holder.tvCuotas.setText("Cuotas pendientes: 0");
                            holder.tvCuotas.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                            return;
                        }

                        int deuda = 0;
                        int cuotasPendientes = 0;
                        long ultimaFecha = 0;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Pago pago = snap.getValue(Pago.class);
                            if (pago == null) continue;
                            deuda += pago.getSaldoPendiente();
                            int pendientes = pago.getCuotasTotales() - pago.getCuotasPagadas();
                            if (pendientes > 0) cuotasPendientes += pendientes;
                            long fecha = obtenerFechaMovimiento(pago);
                            if (fecha > ultimaFecha) ultimaFecha = fecha;
                        }

                        if (deuda == 0) {
                            holder.tvDeuda.setText("Al día");
                            holder.tvDeuda.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                        } else {
                            holder.tvDeuda.setText("Debe $" + deuda);
                            holder.tvDeuda.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                        }

                        long dias = 0;
                        if (ultimaFecha > 0 && deuda > 0) {
                            dias = (System.currentTimeMillis() - ultimaFecha) / (1000L * 60L * 60L * 24L);
                        }

                        holder.tvAtraso.setText("Atraso: " + dias + " días");
                        if (dias == 0) {
                            holder.tvAtraso.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                        } else if (dias <= 15) {
                            holder.tvAtraso.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
                        } else {
                            holder.tvAtraso.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                        }

                        holder.tvCuotas.setText("Cuotas pendientes: " + cuotasPendientes);
                        if (cuotasPendientes == 0) {
                            holder.tvCuotas.setTextColor(ContextCompat.getColor(context, R.color.color_success));
                        } else if (cuotasPendientes <= 2) {
                            holder.tvCuotas.setTextColor(ContextCompat.getColor(context, R.color.color_warning));
                        } else {
                            holder.tvCuotas.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        holder.tvDeuda.setText("Error");
                        holder.tvDeuda.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                        holder.tvAtraso.setText("Error");
                        holder.tvAtraso.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                        holder.tvCuotas.setText("Error");
                        holder.tvCuotas.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                        holder.tvDispositivos.setText("Error");
                        holder.tvDispositivos.setTextColor(ContextCompat.getColor(context, R.color.color_error));
                    }
                });
    }

    private void cargarConteoDispositivos(String userId, ViewHolder holder) {
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(userId)
                .child("dispositivos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int count = (int) snapshot.getChildrenCount();
                        holder.tvDispositivos.setText("Dispositivos: " + count);
                        if (count == 0) {
                            holder.tvDispositivos.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary));
                        } else {
                            holder.tvDispositivos.setTextColor(ContextCompat.getColor(context, R.color.color_primary));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        holder.tvDispositivos.setText("Dispositivos: —");
                    }
                });
    }

    private long obtenerFechaMovimiento(Pago pago) {
        if (pago.getUltimaActualizacion() > 0) return pago.getUltimaActualizacion();
        if (pago.getFechaCreacion() > 0) return pago.getFechaCreacion();
        return 0;
    }

    static class ViewHolder {
        TextView tvCorreo, tvEstado, tvDeuda, tvAtraso, tvCuotas, tvDispositivos;
        Button btnBloquear;
    }
}
