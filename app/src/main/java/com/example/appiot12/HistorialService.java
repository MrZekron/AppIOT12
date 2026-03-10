package com.example.appiot12;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class HistorialService {

    // =========================
    // 🔐 UID
    // =========================
    private static String uid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // =========================
    // 📌 REGISTRO SIMPLE
    // =========================
    public static void registrarEvento(String tipo, String descripcion) {

        String uid = uid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        String id = ref.push().getKey();

        HistorialEvento evento = new HistorialEvento(
                id,
                tipo,
                descripcion,
                System.currentTimeMillis()
        );

        ref.child(id).setValue(evento);
    }

    // =========================
    // 📊 SENSOR DIARIO
    // =========================
    public static void registrarSensorDiario(
            String idTanque,
            double ph,
            double cond,
            double turb,
            double nivel
    ) {

        String uid = uid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        String id = ref.push().getKey();

        HistorialEvento e = new HistorialEvento(
                id,
                "SENSOR",
                "Estado diario del agua",
                System.currentTimeMillis()
        );

        e.idTanque = idTanque;
        e.ph = ph;
        e.conductividad = cond;
        e.turbidez = turb;
        e.nivel = nivel;

        ref.child(id).setValue(e);
    }

    // =========================
    // 💳 REGISTRO DE PAGO
    // =========================
    public static void registrarPago(double monto, int cuotasPagadas) {

        String uid = uid();
        if (uid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        String id = ref.push().getKey();

        HistorialEvento e = new HistorialEvento(
                id,
                "PAGO",
                "Pago de cuota",
                System.currentTimeMillis()
        );

        e.monto = monto;
        e.cuotasPagadas = cuotasPagadas;

        ref.child(id).setValue(e);
    }

    // =========================
    // 🧹 LIMPIEZA 30 DÍAS
    // =========================
    public static void limpiarHistorialAntiguo() {

        String uid = uid();
        if (uid == null) return;

        long limite = System.currentTimeMillis()
                - (30L * 24 * 60 * 60 * 1000);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        ref.orderByChild("timestamp")
                .endAt(limite)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snap) {
                        for (DataSnapshot s : snap.getChildren()) {
                            s.getRef().removeValue();
                        }
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
    }
}
