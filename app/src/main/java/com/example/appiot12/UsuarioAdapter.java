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

            holder.tvCorreo = row.findViewById(R.id.tvCorreoUsuario);
            holder.tvEstadoCuenta = row.findViewById(R.id.tvEstadoCuenta);
            holder.btnBloquear = row.findViewById(R.id.btnBloquear);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Usuario usuario = usuarios.get(position);

        // Mostrar correo
        holder.tvCorreo.setText(usuario.getCorreo());

        // Mostrar estado CORRECTO
        if (usuario.isBloqueado()) {
            holder.tvEstadoCuenta.setText("Cuenta bloqueada ❌");
            holder.tvEstadoCuenta.setTextColor(Color.RED);
            holder.btnBloquear.setText("Desbloquear");
        } else {
            holder.tvEstadoCuenta.setText("Cuenta activa ✔");
            holder.tvEstadoCuenta.setTextColor(Color.GREEN);
            holder.btnBloquear.setText("Bloquear");
        }

        // Acción del botón Bloquear/Desbloquear
        holder.btnBloquear.setOnClickListener(v -> {

            boolean nuevoEstado = !usuario.isBloqueado();
            usuario.setBloqueado(nuevoEstado);

            FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(usuario.getId())
                    .child("bloqueado")
                    .setValue(nuevoEstado);

            // Refrescar visual
            notifyDataSetChanged();
        });

        return row;
    }

    static class ViewHolder {
        TextView tvCorreo, tvEstadoCuenta;
        Button btnBloquear;
    }
}
