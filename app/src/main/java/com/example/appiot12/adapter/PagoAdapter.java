package com.example.appiot12.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.appiot12.model.Pago;

import java.util.List;
import java.util.Locale;

/**
 * Adaptador para mostrar pagos en ListView.
 *
 * Muestra:
 * - nombre del producto
 * - resumen de cuotas
 * - saldo pendiente
 * - estado del pago
 * - estado de envío
 */
public class PagoAdapter extends ArrayAdapter<Pago> {

    public PagoAdapter(Context context, List<Pago> pagos) {
        super(context, 0, pagos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Pago pago = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        if (pago == null) {
            text1.setText("Pago no disponible");
            text2.setText("");
            return convertView;
        }

        String nombre = safe(pago.getNombreProducto());
        if (nombre.isEmpty()) {
            nombre = "Dispositivo";
        }

        String resumen1 = String.format(
                Locale.getDefault(),
                "%s | %s",
                nombre,
                formatearEstadoPago(pago.getEstadoPago())
        );

        String resumen2 = String.format(
                Locale.getDefault(),
                "Cuotas: %d/%d | Saldo: $%d | Envío: %s",
                pago.getCuotasPagadas(),
                pago.getCuotasTotales(),
                pago.getSaldoPendiente(),
                formatearEstadoEnvio(pago.getEstadoEnvio())
        );

        text1.setText(resumen1);
        text2.setText(resumen2);

        // Color del estado principal
        switch (safe(pago.getEstadoPago()).toLowerCase(Locale.ROOT)) {
            case "pagado":
                text1.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "parcial":
                text1.setTextColor(Color.parseColor("#F9A825"));
                break;
            default:
                text1.setTextColor(Color.parseColor("#C62828"));
                break;
        }

        return convertView;
    }

    private String safe(String valor) {
        return valor == null ? "" : valor;
    }

    private String formatearEstadoPago(String estado) {
        String e = safe(estado).toLowerCase(Locale.ROOT);

        switch (e) {
            case "pagado":
                return "Pagado";
            case "parcial":
                return "Pago parcial";
            case "fallido":
                return "Fallido";
            default:
                return "Pendiente";
        }
    }

    private String formatearEstadoEnvio(String estado) {
        String e = safe(estado).toLowerCase(Locale.ROOT);

        switch (e) {
            case "enviado":
                return "Enviado";
            case "entregado":
                return "Entregado";
            case "preparando":
                return "Preparando";
            default:
                return "Preparando";
        }
    }
}
