package com.example.appiot12.service;

import com.example.appiot12.model.HistorialEvento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistorialService {

    private static String uid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private static DatabaseReference getHistorialRef() {
        String uid = uid();
        if (uid == null) return null;
        return FirebaseDatabase.getInstance().getReference("historial");
    }

    public static void registrarEvento(String entidadAfectada, String accion) {
        registrarEvento(entidadAfectada, accion, null, null);
    }

    public static void registrarEvento(String entidadAfectada, String accion,
                                       String idEntidadAfectada, String detalle) {
        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        String idHistorial = ref.push().getKey();
        if (idHistorial == null) return;

        String idUsuario = uid();
        HistorialEvento evento = new HistorialEvento(
                idHistorial, idUsuario, accion, entidadAfectada, idEntidadAfectada);
        if (detalle != null) evento.setDetalle(detalle);

        ref.child(idHistorial).setValue(evento);
    }

    public static void registrarSensorDiario(String idTanque, double ph, double cond, double turb, double nivel) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("idTanque", idTanque);
            json.put("ph", ph);
            json.put("conductividad", cond);
            json.put("turbidez", turb);
            json.put("nivel", nivel);
            registrarEvento("dispositivo", "Lectura diaria de sensores", idTanque, json.toString());
        } catch (org.json.JSONException e) {
            registrarEvento("dispositivo", "Lectura diaria de sensores", idTanque, null);
        }
    }

    public static void registrarPago(int monto, int cuotasPagadas) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("monto", monto);
            json.put("cuotasPagadas", cuotasPagadas);
            registrarEvento("pago", "Pago de cuota registrado", null, json.toString());
        } catch (org.json.JSONException e) {
            registrarEvento("pago", "Pago de cuota registrado", null, null);
        }
    }

    public static void limpiarHistorialAntiguo() {
        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        long limite = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        ref.orderByChild("timestamp").endAt(limite).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    s.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
