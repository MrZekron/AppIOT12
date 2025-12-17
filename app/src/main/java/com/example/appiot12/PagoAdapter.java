package com.example.appiot12;
// 📦 Adaptador que convierte objetos Pago en filas visibles (item_pago.xml)
// Es el traductor visual del dinero 💸➡️👀

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
 * ⭐ ADAPTADOR DE PAGOS ⭐
 *
 * Explicado para un niño 👶:
 * 👉 Tenemos muchos pagos guardados
 * 👉 El ListView no los entiende
 * 👉 Este adaptador se los explica y los dibuja en pantalla 🎨
 *
 * Muestra por cada pago:
 *   ✔ Qué es
 *   ✔ Cuánto falta por pagar
 *   ✔ Cuándo se compró
 *   ✔ Si ya está pagado o no
 */
public class PagoAdapter extends ArrayAdapter<Pago> {

    private final Context context;      // 🌍 Dónde se dibuja la lista
    private final List<Pago> pagos;     // 💰 Lista de pagos

    public PagoAdapter(Context context, List<Pago> pagos) {
        super(context, R.layout.item_pago, pagos);
        this.context = context;
        this.pagos = pagos;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        ViewHolder holder;

        // ============================================================
        // ♻️ VIEW HOLDER PATTERN (rendimiento)
        // ============================================================
        if (convertView == null) {
            // No hay vista reciclable → crear una nueva
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_pago, parent, false);

            holder = new ViewHolder();
            holder.tvDescripcion = convertView.findViewById(R.id.tvDescripcionPago);
            holder.tvMonto = convertView.findViewById(R.id.tvMontoPago);
            holder.tvFecha = convertView.findViewById(R.id.tvFechaPago);
            holder.tvEstado = convertView.findViewById(R.id.tvEstadoPago);

            convertView.setTag(holder);
        } else {
            // Reutilizamos vista existente (rápido y eficiente 🚀)
            holder = (ViewHolder) convertView.getTag();
        }

        // ============================================================
        // 📦 OBTENER PAGO ACTUAL
        // ============================================================
        Pago pago = pagos.get(position);

        if (pago == null) {
            // Caso extremadamente raro, pero seguro 🛡️
            holder.tvDescripcion.setText("Pago no disponible");
            holder.tvMonto.setText("-");
            holder.tvFecha.setText("-");
            holder.tvEstado.setText("-");
            return convertView;
        }

        // ============================================================
        // 📝 DESCRIPCIÓN
        // ============================================================
        // Ejemplo: Compra de dispositivo (1/6 cuotas)
        holder.tvDescripcion.setText(
                "Compra de dispositivo (" +
                        pago.getCuotasPagadas() +
                        "/" +
                        pago.getCuotasTotales() +
                        " cuotas)"
        );

        // ============================================================
        // 💵 MONTO PENDIENTE
        // ============================================================
        holder.tvMonto.setText("$" + pago.getSaldoPendiente());

        // ============================================================
        // 📅 FECHA DE COMPRA
        // ============================================================
        holder.tvFecha.setText(formatearFecha(pago.getFechaPago()));

        // ============================================================
        // 🚦 ESTADO DEL PAGO (SEMÁFORO FINANCIERO)
        // ============================================================
        if (pago.isPagado()) {
            holder.tvEstado.setText("Pagado ✔");
            holder.tvEstado.setTextColor(0xFF388E3C); // 🟢 Verde
        } else {
            holder.tvEstado.setText("Pendiente ❌");
            holder.tvEstado.setTextColor(0xFFD32F2F); // 🔴 Rojo
        }

        return convertView;
    }

    // ============================================================
    // 🗓 FORMATEAR FECHA (helper limpio)
    // ============================================================
    private String formatearFecha(long timestamp) {
        try {
            return "Fecha: " + new SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
            ).format(new Date(timestamp));
        } catch (Exception e) {
            return "Fecha: -";
        }
    }

    // ============================================================
    // 📦 VIEW HOLDER (cajón de referencias)
    // ============================================================
    private static class ViewHolder {
        TextView tvDescripcion;
        TextView tvMonto;
        TextView tvFecha;
        TextView tvEstado;
    }
}
