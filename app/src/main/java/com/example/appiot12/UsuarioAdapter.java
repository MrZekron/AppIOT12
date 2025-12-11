package com.example.appiot12;
// 📦 Adaptador responsable de mostrar cada usuario dentro del panel administrativo.
// Incluye estado, bloqueo, deuda pendiente y atraso estimado.

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
 * 🌟 USUARIO ADAPTER 🌟
 *
 * Renderiza:
 *   ✔ Correo del usuario
 *   ✔ Estado de bloqueo (activo / bloqueado)
 *   ✔ Deuda total pendiente
 *   ✔ Tiempo en atraso (días)
 *   ✔ Botón de bloquear / desbloquear
 *
 * Funciona como un mini–dashboard corporativo por usuario ⚙️.
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
        // 🧠 OPTIMIZACIÓN: ViewHolder Pattern
        // ============================================================
        if (row == null) {

            // Inflamos el layout del usuario
            row = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);

            // Creamos el holder que almacena referencias
            holder = new ViewHolder();
            holder.tvCorreo = row.findViewById(R.id.tvCorreo);
            holder.tvEstadoCuenta = row.findViewById(R.id.tvEstadoCuenta);
            holder.tvDeuda = row.findViewById(R.id.tvDeuda);
            holder.tvTiempoAtraso = row.findViewById(R.id.tvTiempoAtraso);
            holder.btnBloquear = row.findViewById(R.id.btnBloquear);

            row.setTag(holder);
        } else {
            // Reusamos la vista (muchísimo más rápido)
            holder = (ViewHolder) row.getTag();
        }

        Usuario usuario = usuarios.get(position);

        // ============================================================
        // 📧 CORREO DEL USUARIO
        // ============================================================
        holder.tvCorreo.setText(usuario.getCorreo());

        // ============================================================
        // 🔐 ESTADO DE CUENTA (BLOQUEADO / ACTIVO)
        // ============================================================
        if (usuario.isBloqueado()) {

            holder.tvEstadoCuenta.setText("Cuenta bloqueada ❌");
            holder.tvEstadoCuenta.setTextColor(Color.RED);

            // Botón cambia a DESBLOQUEAR
            holder.btnBloquear.setText("Desbloquear");

        } else {

            holder.tvEstadoCuenta.setText("Cuenta activa ✔");
            holder.tvEstadoCuenta.setTextColor(Color.GREEN);

            // Botón cambia a BLOQUEAR
            holder.btnBloquear.setText("Bloquear");
        }

        // ============================================================
        // 💰 INICIALIZAR CAMPOS DE DEUDA Y ATRASO
        // ============================================================
        holder.tvDeuda.setText("Cargando...");
        holder.tvTiempoAtraso.setText("Calculando...");
        holder.tvDeuda.setTextColor(Color.GRAY);
        holder.tvTiempoAtraso.setTextColor(Color.GRAY);

        // Cargar deuda real desde Firebase
        cargarDeudaUsuario(usuario, holder.tvDeuda, holder.tvTiempoAtraso);

        // ============================================================
        // 🚫 BOTÓN BLOQUEAR / DESBLOQUEAR (ADMIN ONLY)
        // Actualiza Firebase y refresca la vista
        // ============================================================
        holder.btnBloquear.setOnClickListener(v -> {

            boolean nuevoEstado = !usuario.isBloqueado(); // toggle rápido
            usuario.setBloqueado(nuevoEstado);

            // Guardamos estado en Firebase
            FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(usuario.getId())
                    .child("bloqueado")
                    .setValue(nuevoEstado);

            // Refrescamos UI
            notifyDataSetChanged();
        });

        return row;
    }

    // ============================================================
    // 🔍 LECTURA REAL DE PAGOS DEL USUARIO
    //     + CÁLCULO DE DEUDA TOTAL
    //     + CÁLCULO DE ATRASO EN DÍAS
    // ============================================================
    private void cargarDeudaUsuario(Usuario usuario, TextView tvDeuda, TextView tvAtraso) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getId())
                .child("pagos") // Nodo donde viven los pagos del usuario
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        // ================================
                        // 🔹 Usuario sin compras
                        // ================================
                        if (!snapshot.exists()) {
                            tvDeuda.setText("Sin compras");
                            tvDeuda.setTextColor(Color.GRAY);

                            tvAtraso.setText("Atrasado: 0 días");
                            tvAtraso.setTextColor(Color.GREEN);

                            return;
                        }

                        int deudaTotal = 0;
                        long ultimoPago = 0; // timestamp más reciente

                        // ================================
                        // 💳 SUMAR TODA LA DEUDA
                        // ================================
                        for (DataSnapshot pagoSnap : snapshot.getChildren()) {

                            Pago pago = pagoSnap.getValue(Pago.class);
                            if (pago == null) continue;

                            deudaTotal += pago.getSaldoPendiente();

                            // Obtener fecha del último pago registrado
                            if (pago.getFechaPago() > ultimoPago) {
                                ultimoPago = pago.getFechaPago();
                            }
                        }

                        // ================================
                        // 💰 MOSTRAR DEUDA
                        // ================================
                        if (deudaTotal == 0) {
                            tvDeuda.setText("Al día ✔");
                            tvDeuda.setTextColor(Color.GREEN);
                        } else {
                            tvDeuda.setText("Debe $" + deudaTotal);
                            tvDeuda.setTextColor(Color.RED);
                        }

                        // ================================
                        // 📅 CALCULAR ATRASO EN DÍAS
                        // ================================
                        if (ultimoPago == 0) {
                            tvAtraso.setText("Atrasado: 0 días");
                            tvAtraso.setTextColor(Color.GREEN);
                            return;
                        }

                        long hoy = System.currentTimeMillis();
                        long diff = hoy - ultimoPago;

                        // Convertir milisegundos → días
                        long diasAtraso = diff / (1000 * 60 * 60 * 24);

                        tvAtraso.setText("Atrasado: " + diasAtraso + " días");

                        // ================================
                        // 🚦 SEMÁFORO CORPORATIVO
                        // ================================
                        if (diasAtraso == 0) {
                            tvAtraso.setTextColor(Color.GREEN);

                        } else if (diasAtraso <= 15) {
                            tvAtraso.setTextColor(Color.parseColor("#FBC02D")); // Amarillo corporativo

                        } else {
                            tvAtraso.setTextColor(Color.RED); // Alerta
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Error leyendo pagos
                        tvDeuda.setText("Error");
                        tvDeuda.setTextColor(Color.RED);

                        tvAtraso.setText("Error");
                        tvAtraso.setTextColor(Color.RED);
                    }
                });
    }

    // ============================================================
    // 🔧 HOLDER DE VISTAS
    // ============================================================
    static class ViewHolder {
        TextView tvCorreo, tvEstadoCuenta, tvDeuda, tvTiempoAtraso;
        Button btnBloquear;
    }
}
