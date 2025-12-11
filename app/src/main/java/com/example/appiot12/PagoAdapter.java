package com.example.appiot12;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PagoAdapter extends ArrayAdapter<Pago> {

    private final Context context;
    private final List<Pago> pagos;

    public PagoAdapter(Context context, List<Pago> pagos) {
        super(context, R.layout.item_pago, pagos);
        this.context = context;
        this.pagos = pagos;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.item_pago, parent, false);
            holder = new ViewHolder();

            holder.tvDescripcion = row.findViewById(R.id.tvDescripcionPago);
            holder.tvMonto = row.findViewById(R.id.tvMontoPago);
            holder.tvFecha = row.findViewById(R.id.tvFechaPago);
            holder.tvEstado = row.findViewById(R.id.tvEstadoPago);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Pago pago = pagos.get(position);

        if (pago == null) {
            holder.tvDescripcion.setText("Pago inválido");
            holder.tvMonto.setText("-");
            holder.tvFecha.setText("-");
            holder.tvEstado.setText("-");
            return row;
        }

        // ============================
        // 📝 DESCRIPCIÓN DEL PAGO
        // ============================

        String descripcion = "Compra de dispositivo (" +
                pago.getCuotasPagadas() + "/" + pago.getCuotasTotales() + " cuotas)";

        holder.tvDescripcion.setText(descripcion);

        // ============================
        // 💰 SALDO PENDIENTE
        // ============================

        holder.tvMonto.setText("$" + pago.getSaldoPendiente());

        // ============================
        // 📅 FECHA DE COMPRA
        // ============================
        try {
            long fecha = pago.getFechaPago(); // ← CORRECTO
            String fechaFormateada =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(new Date(fecha));

            holder.tvFecha.setText("Fecha: " + fechaFormateada);

        } catch (Exception e) {
            holder.tvFecha.setText("Fecha: -");
        }

        // ============================
        // 🟢🔴 ESTADO DEL PAGO
        // ============================

        if (pago.isPagado()) {
            holder.tvEstado.setText("Pagado ✔");
            holder.tvEstado.setTextColor(0xFF388E3C); // verde
        } else {
            holder.tvEstado.setText("Pendiente ❌");
            holder.tvEstado.setTextColor(0xFFD32F2F); // rojo
        }

        return row;
    }

    private static class ViewHolder {
        TextView tvDescripcion, tvMonto, tvFecha, tvEstado;
    }
}
