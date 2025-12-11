package com.example.appiot12;
// 📦 Adaptador encargado de transformar objetos TanqueAgua → filas del ListView.
// Es la tarjeta de presentación de cada tanque en el “Dashboard del Usuario”.

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * ⭐ ADAPTADOR DE TANQUES ⭐
 *
 * Muestra:
 *   ✔ Nombre del tanque
 *   ✔ Estado del dispositivo asociado
 *   ✔ Sensores clave (pH, turbidez, conductividad, nivel)
 *   ✔ Color de alerta dinámico
 *
 * Funciona como una MARRAQUETA caliente: carga rápido + datos en vivo 🔥.
 */
public class TanqueAdapter extends ArrayAdapter<TanqueAgua> {

    private final Context context;
    private final List<TanqueAgua> tanques;

    public TanqueAdapter(Context context, List<TanqueAgua> tanques) {
        super(context, R.layout.item_tanque, tanques);
        this.context = context;
        this.tanques = tanques;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolder holder;

        // ======================================================
        // 🧠 PATRÓN VIEWHOLDER → optimiza scrolling masivo
        // ======================================================
        if (row == null) {
            row = LayoutInflater.from(context).inflate(R.layout.item_tanque, parent, false);

            holder = new ViewHolder();
            holder.tvNombreTanque = row.findViewById(R.id.tvNombreTanque);
            holder.tvResumenDatos = row.findViewById(R.id.tvResumenDatos);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        TanqueAgua tanque = tanques.get(position);

        // ======================================================
        // 📝 NOMBRE DEL TANQUE
        // ======================================================
        holder.tvNombreTanque.setText("Nombre: " + tanque.getNombre());

        // ======================================================
        // ❌ SIN DISPOSITIVO ASOCIADO
        // ======================================================
        if (tanque.getIdDispositivo() == null || tanque.getIdDispositivo().isEmpty()) {

            holder.tvResumenDatos.setText("Estado: SIN DISPOSITIVO");
            holder.tvResumenDatos.setTextColor(Color.GRAY);

            return row; // Nada más que hacer
        }

        // ======================================================
        // 🔄 MOSTRAR "CARGANDO..." mientras vienen sensores
        // ======================================================
        holder.tvResumenDatos.setText("Cargando sensores...");
        holder.tvResumenDatos.setTextColor(Color.GRAY);

        // ======================================================
        // ✔ RUTA CORRECTA PARA DISPOSITIVOS
        //
        //   usuarios/{uid}/dispositivos/{idDispositivo}
        //
        // ANTES estaba mal apuntado → no cargaba NUNCA el resumen
        // ======================================================

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos")
                .child(tanque.getIdDispositivo())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        // ⚠ NO EXISTE EL DISPOSITIVO → error de integridad
                        if (!snapshot.exists()) {
                            holder.tvResumenDatos.setText("DISPOSITIVO NO ENCONTRADO");
                            holder.tvResumenDatos.setTextColor(Color.RED);
                            return;
                        }

                        // Intentamos convertir JSON → objeto
                        Dispositivo dispositivo = snapshot.getValue(Dispositivo.class);

                        if (dispositivo == null) {
                            holder.tvResumenDatos.setText("Error al leer dispositivo");
                            holder.tvResumenDatos.setTextColor(Color.RED);
                            return;
                        }

                        // ======================================================
                        // 🧪 PROCESAR SENSORES (pH / Conductividad / Turbidez)
                        // ======================================================
                        String estadoPH = evaluarRango(dispositivo.getPh(), 6.5, 8.5);
                        String estadoConductividad = evaluarRango(dispositivo.getConductividad(), 0, 700);
                        String estadoTurbidez = evaluarRango(dispositivo.getTurbidez(), 0, 5);

                        // ======================================================
                        // 💧 CALCULAR NIVEL DEL TANQUE
                        // ======================================================
                        double capacidad;
                        try {
                            capacidad = Double.parseDouble(tanque.getCapacidad());
                        } catch (Exception e) {
                            capacidad = 0; // fallback seguro
                        }

                        double nivel = dispositivo.getUltrasonico();
                        String estadoNivel = "SIN DATA";

                        if (capacidad > 0) {
                            if (nivel < capacidad * 0.25) estadoNivel = "BAJO";
                            else if (nivel < capacidad) estadoNivel = "MEDIO";
                            else estadoNivel = "LLENO";
                        }

                        // ======================================================
                        // 📊 RESUMEN FORMATEADO TIPO "DASHBOARD"
                        // ======================================================
                        String resumen = String.format(
                                "pH %.1f (%s) | Cond %.0f (%s) | Turb %.1f (%s) | Agua %.0f L (%s)",
                                dispositivo.getPh(), estadoPH,
                                dispositivo.getConductividad(), estadoConductividad,
                                dispositivo.getTurbidez(), estadoTurbidez,
                                nivel, estadoNivel
                        );

                        holder.tvResumenDatos.setText(resumen);

                        // ======================================================
                        // 🎨 COLOR DEPENDIENDO DEL ESTADO DEL TANQUE
                        // ======================================================
                        boolean alerta =
                                !"OK".equals(estadoPH) ||
                                        !"OK".equals(estadoConductividad) ||
                                        !"OK".equals(estadoTurbidez) ||
                                        "BAJO".equals(estadoNivel);

                        int verde = Color.parseColor("#006400");
                        int rojo = Color.parseColor("#8B0000");

                        holder.tvResumenDatos.setTextColor(alerta ? rojo : verde);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        holder.tvResumenDatos.setText("ERROR");
                        holder.tvResumenDatos.setTextColor(Color.RED);
                    }
                });

        return row;
    }

    // ======================================================
    // 📦 HOLDER para reutilizar vistas
    // ======================================================
    private static class ViewHolder {
        TextView tvNombreTanque;
        TextView tvResumenDatos;
    }

    // ======================================================
    // 🎯 EVALUAR SENSOR (OK / BAJO / ALTO)
    // ======================================================
    private String evaluarRango(double v, double min, double max) {
        if (v < min) return "BAJO";
        if (v > max) return "ALTO";
        return "OK";
    }
}
