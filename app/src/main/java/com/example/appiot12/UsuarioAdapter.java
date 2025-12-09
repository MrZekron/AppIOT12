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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

            holder.tvCorreo = row.findViewById(R.id.tvCorreo);
            holder.tvEstadoCuenta = row.findViewById(R.id.tvEstadoCuenta);
            holder.tvDeuda = row.findViewById(R.id.tvDeuda);
            holder.btnBloquear = row.findViewById(R.id.btnBloquear);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Usuario usuario = usuarios.get(position);

        // ===============================
        //  MOSTRAR CORREO
        // ===============================
        holder.tvCorreo.setText(usuario.getCorreo());

        // ===============================
        //  ESTADO DE BLOQUEO
        // ===============================
        if (usuario.isBloqueado()) {
            holder.tvEstadoCuenta.setText("Cuenta bloqueada ❌");
            holder.tvEstadoCuenta.setTextColor(Color.RED);
            holder.btnBloquear.setText("Desbloquear");
        } else {
            holder.tvEstadoCuenta.setText("Cuenta activa ✔");
            holder.tvEstadoCuenta.setTextColor(Color.GREEN);
            holder.btnBloquear.setText("Bloquear");
        }

        // ===============================
        //   ESTADO DE DEUDA (CORREGIDO)
        // ===============================

        holder.tvDeuda.setText("Cargando...");
        holder.tvDeuda.setTextColor(Color.GRAY);

        cargarDeudaUsuario(usuario, holder.tvDeuda);

        // ===============================
        //  BOTÓN BLOQUEAR / DESBLOQUEAR
        // ===============================
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
    //      CARGA REAL DE PAGOS DESDE FIREBASE
    //      NUEVO (CORRECTO, YA NO DEPENDE DE TANQUES)
    // ============================================================
    private void cargarDeudaUsuario(Usuario usuario, TextView tvDeuda) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getId())
                .child("pagos")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            tvDeuda.setText("Sin compras");
                            tvDeuda.setTextColor(Color.GRAY);
                            return;
                        }

                        int deudaTotal = 0;

                        for (DataSnapshot pagoSnap : snapshot.getChildren()) {
                            Pago pago = pagoSnap.getValue(Pago.class);
                            if (pago == null) continue;

                            deudaTotal += pago.getSaldoPendiente();
                        }

                        if (deudaTotal == 0) {
                            tvDeuda.setText("Al día ✔");
                            tvDeuda.setTextColor(Color.GREEN);
                        } else {
                            tvDeuda.setText("Debe $" + deudaTotal);
                            tvDeuda.setTextColor(Color.RED);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        tvDeuda.setText("Error");
                        tvDeuda.setTextColor(Color.RED);
                    }
                });
    }

    // ============================================================
    //      HOLDER DE VISTAS
    // ============================================================
    static class ViewHolder {
        TextView tvCorreo, tvEstadoCuenta, tvDeuda;
        Button btnBloquear;
    }
}
