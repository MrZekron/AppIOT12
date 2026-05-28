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
        return FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("historial");
    }

    public static void registrarEvento(String tipo, String descripcion) {
        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        String id = ref.push().getKey();
        if (id == null) return;

        ref.child(id).setValue(new HistorialEvento(id, tipo, descripcion, System.currentTimeMillis()));
    }

    public static void registrarSensorDiario(String idTanque, double ph, double cond, double turb, double nivel) {
        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        String id = ref.push().getKey();
        if (id == null) return;

        HistorialEvento evento = new HistorialEvento(id, "SENSOR", "Estado diario del agua", System.currentTimeMillis());
        evento.setIdTanque(idTanque);
        evento.setPh(ph);
        evento.setConductividad(cond);
        evento.setTurbidez(turb);
        evento.setNivel(nivel);

        ref.child(id).setValue(evento);
    }

    public static void registrarPago(double monto, int cuotasPagadas) {
        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        String id = ref.push().getKey();
        if (id == null) return;

        HistorialEvento evento = new HistorialEvento(id, "PAGO", "Pago de cuota", System.currentTimeMillis());
        evento.setMonto(monto);
        evento.setCuotasPagadas(cuotasPagadas);

        ref.child(id).setValue(evento);
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
