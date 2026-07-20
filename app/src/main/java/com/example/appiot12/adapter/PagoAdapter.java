package com.example.appiot12.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.appiot12.R;
import com.example.appiot12.model.Pago;

import java.util.List;
import java.util.Locale;

public class PagoAdapter extends ArrayAdapter<Pago> {

    public interface OnPagarCuotaListener {
        void onPagarCuota(Pago pago);
    }

    private final Context context;
    private OnPagarCuotaListener listener;

    public PagoAdapter(Context context, List<Pago> pagos) {
        super(context, R.layout.item_pago, pagos);
        this.context = context;
    }

    public void setOnPagarCuotaListener(OnPagarCuotaListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_pago, parent, false);
        }

        Pago pago = getItem(position);
        if (pago == null) return convertView;

        TextView tvDesc   = convertView.findViewById(R.id.tvDescripcionPago);
        TextView tvCuotas = convertView.findViewById(R.id.tvCuotasStatus);
        TextView tvMonto  = convertView.findViewById(R.id.tvMontoPago);
        TextView tvEstado = convertView.findViewById(R.id.tvEstadoPago);
        Button   btnPagar = convertView.findViewById(R.id.btnPagarCuota);

        String nombre = pago.getNombreProducto() != null && !pago.getNombreProducto().isEmpty()
                ? pago.getNombreProducto() : "Dispositivo AguaSegura";
        tvDesc.setText(nombre);

        tvCuotas.setText(String.format(Locale.getDefault(),
                "Cuotas: %d/%d pagadas", pago.getCuotasPagadas(), pago.getCuotasTotales()));

        tvMonto.setText(String.format(Locale.getDefault(),
                "Saldo pendiente: $%,d", pago.getSaldoPendiente()));

        tvEstado.setText(formatearEstado(pago.getEstadoPago()));

        int colorRes = colorParaEstado(pago.getEstadoPago());
        tvEstado.setTextColor(ContextCompat.getColor(context, colorRes));
        tvMonto.setTextColor(ContextCompat.getColor(context, colorRes));

        boolean tienePendientes = pago.getSaldoPendiente() > 0 && !pago.isPagado();
        btnPagar.setVisibility(tienePendientes ? View.VISIBLE : View.GONE);
        btnPagar.setOnClickListener(v -> {
            if (listener != null) listener.onPagarCuota(pago);
        });

        return convertView;
    }

    private String formatearEstado(String estado) {
        if (estado == null) return "Pendiente";
        switch (estado.toLowerCase(Locale.ROOT)) {
            case "pagado":  return "Pagado";
            case "parcial": return "Pago parcial";
            default:        return "Pendiente";
        }
    }

    private int colorParaEstado(String estado) {
        if (estado == null) return R.color.color_error;
        switch (estado.toLowerCase(Locale.ROOT)) {
            case "pagado":  return R.color.color_success;
            case "parcial": return R.color.color_warning;
            default:        return R.color.color_error;
        }
    }
}
