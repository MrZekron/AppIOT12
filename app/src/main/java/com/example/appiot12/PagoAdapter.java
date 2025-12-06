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

/**
 * 🎨 ADAPTADOR DE PAGOS
 * Dibuja cada fila del historial de pagos y la formatea bonito.
 */
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

        // 🧱 Optimizamos con ViewHolder
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

        // 🔍 Pago actual
        Pago pago = pagos.get(position);

        if (pago == null) {
            holder.tvDescripcion.setText("Pago inválido");
            holder.tvMonto.setText("-");
            holder.tvFecha.setText("-");
            holder.tvEstado.setText("-");
            return row;
        }

        // ✏️ DESCRIPCIÓN AUTOMÁTICA DEL PAGO
        // ejemplo: "Compra dispositivo (2/6 cuotas)"
        String descripcion = "Compra dispositivo (" +
                pago.getCuotasPagadas() + "/" + pago.getCuotasTotales() + " cuotas)";

        holder.tvDescripcion.setText(descripcion);

        // 💰 MONTO ACTUAL A PAGAR (saldo restante)
        String montoTexto = "$" + pago.getSaldoPendiente();
        holder.tvMonto.setText(montoTexto);

        // 📅 FECHA DE COMPRA FORMATEADA
        try {
            String fechaFormateada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(pago.getFechaCompra()));
            holder.tvFecha.setText("Fecha: " + fechaFormateada);
        } catch (Exception e) {
            holder.tvFecha.setText("Fecha: -");
        }

        // 🟢🔴 ESTADO DEL PAGO
        if (pago.isPagado()) {
            holder.tvEstado.setText("Pagado ✔");
            holder.tvEstado.setTextColor(0xFF388E3C); // verde
        } else {
            holder.tvEstado.setText("Pendiente ❌");
            holder.tvEstado.setTextColor(0xFFD32F2F); // rojo
        }

        return row;
    }

    /** 🧱 Cache de vistas para rendimiento */
    private static class ViewHolder {
        TextView tvDescripcion, tvMonto, tvFecha, tvEstado;
    }
}
