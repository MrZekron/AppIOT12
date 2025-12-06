package com.example.appiot12;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

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

        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);
            holder = new ViewHolder();

            holder.tvCorreo = row.findViewById(R.id.tvCorreo);
            holder.tvEstadoCuenta = row.findViewById(R.id.tvEstadoCuenta);
            holder.tvDeuda = row.findViewById(R.id.tvDeuda);
            holder.btnBloquear = row.findViewById(R.id.btnBloquear);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Usuario usuario = usuarios.get(position);

        // Mostrar correo
        holder.tvCorreo.setText(usuario.getCorreo());

        // Mostrar estado de bloqueo
        if (usuario.isBloqueado()) {
            holder.tvEstadoCuenta.setText("Cuenta bloqueada ❌");
            holder.tvEstadoCuenta.setTextColor(Color.RED);
            holder.btnBloquear.setText("Desbloquear");
        } else {
            holder.tvEstadoCuenta.setText("Cuenta activa ✔");
            holder.tvEstadoCuenta.setTextColor(Color.GREEN);
            holder.btnBloquear.setText("Bloquear");
        }

        // ---------- NUEVO: ESTADO DE PAGO / DEUDA ----------

        String estadoDeuda = obtenerEstadoDeDeuda(usuario);
        holder.tvDeuda.setText(estadoDeuda);

        if (estadoDeuda.contains("Debe")) {
            holder.tvDeuda.setTextColor(Color.RED);
        } else {
            holder.tvDeuda.setTextColor(Color.GREEN);
        }

        // Botón bloquear / desbloquear
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

    // =====================================================
    //   MÉTODO PARA CALCULAR LA DEUDA REAL DEL USUARIO
    // =====================================================
    private String obtenerEstadoDeDeuda(Usuario usuario) {

        Map<String, TanqueAgua> tanques = usuario.getTanques();
        if (tanques == null || tanques.isEmpty()) {
            return "Sin dispositivos";
        }

        int deudaTotal = 0;

        for (TanqueAgua tanque : tanques.values()) {

            if (tanque.getDispositivo() == null) continue;
            if (tanque.getDispositivo().getPago() == null) continue;

            Pago pago = tanque.getDispositivo().getPago();

            deudaTotal += pago.getSaldoPendiente();
        }

        if (deudaTotal == 0) {
            return "Al día ✔";
        }

        return "Debe $" + deudaTotal;
    }

    static class ViewHolder {
        TextView tvCorreo, tvEstadoCuenta, tvDeuda;
        Button btnBloquear;
    }
}
