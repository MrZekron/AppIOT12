package com.example.appiot12;
// 📦 Adaptador encargado de transformar objetos Pago → vistas (item_pago.xml)
// Es el motor visual del módulo financiero de AguaSegura 💸✨

import android.content.Context;
import android.view.LayoutInflater; // 🏭 Creador de layouts dinámicos
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // 📋 Adaptador base para listas simples
import android.widget.TextView; // ✏️ Cada línea del ListView

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat; // 🗓 Formato de fecha
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ⭐ ADAPTADOR FINANCIERO DE PAGOS ⭐
 *
 * Muestra:
 *   - descripción del pago
 *   - saldo pendiente
 *   - fecha
 *   - estado (Pagado / Pendiente)
 *
 * Utiliza ViewHolder para rendimiento 🔥.
 */
public class PagoAdapter extends ArrayAdapter<Pago> {

    private final Context context;  // 🌍 Entorno donde vive el ListView
    private final List<Pago> pagos; // 💰 Lista de pagos a mostrar

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

        // ============================================================
        // 🧠 OPTIMIZACIÓN: VIEWHOLDER PATTERN
        // Reutiliza vistas para ahorrar memoria y acelerar scroll.
        // ============================================================
        if (row == null) {
            // No existe vista previa → inflamos una nueva
            row = LayoutInflater.from(context).inflate(R.layout.item_pago, parent, false);

            // Creamos el “cajón” para guardar referencias
            holder = new ViewHolder();
            holder.tvDescripcion = row.findViewById(R.id.tvDescripcionPago);
            holder.tvMonto = row.findViewById(R.id.tvMontoPago);
            holder.tvFecha = row.findViewById(R.id.tvFechaPago);
            holder.tvEstado = row.findViewById(R.id.tvEstadoPago);

            row.setTag(holder); // Asociamos ViewHolder a la vista
        }
        else {
            // Reciclamos una vista existente → rendimiento TOP 🚀
            holder = (ViewHolder) row.getTag();
        }

        // Obtenemos el pago actual
        Pago pago = pagos.get(position);

        if (pago == null) {
            // Caso poco probable pero seguro ante errores
            holder.tvDescripcion.setText("Pago inválido");
            holder.tvMonto.setText("-");
            holder.tvFecha.setText("-");
            holder.tvEstado.setText("-");
            return row;
        }

        // ============================================================
        // 📝 DESCRIPCIÓN DEL PAGO
        // ============================================================
        // Ejemplo: "Compra de dispositivo (2/6 cuotas)"
        String descripcion = "Compra de dispositivo (" +
                pago.getCuotasPagadas() + "/" + pago.getCuotasTotales() + " cuotas)";
        holder.tvDescripcion.setText(descripcion);

        // ============================================================
        // 💵 MONTO PENDIENTE
        // ============================================================
        holder.tvMonto.setText("$" + pago.getSaldoPendiente());

        // ============================================================
        // 📅 FECHA DE COMPRA
        // ============================================================
        try {
            long fecha = pago.getFechaPago(); // timestamp original
            String fechaFormateada =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(new Date(fecha));

            holder.tvFecha.setText("Fecha: " + fechaFormateada);

        } catch (Exception e) {
            holder.tvFecha.setText("Fecha: -");
        }

        // ============================================================
        // 🔵🟢🔴 ESTADO DEL PAGO
        // ============================================================

        if (pago.isPagado()) {
            holder.tvEstado.setText("Pagado ✔");
            holder.tvEstado.setTextColor(0xFF388E3C); // Verde corporativo
        } else {
            holder.tvEstado.setText("Pendiente ❌");
            holder.tvEstado.setTextColor(0xFFD32F2F); // Rojo de alerta
        }

        return row;
    }

    // ============================================================
    // 📦 Holder para la vista, mejora rendimiento del ListView
    // ============================================================
    private static class ViewHolder {
        TextView tvDescripcion;
        TextView tvMonto;
        TextView tvFecha;
        TextView tvEstado;
    }
}
