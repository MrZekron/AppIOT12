package com.example.appiot12.controller;

import androidx.annotation.NonNull;

import com.example.appiot12.model.Pago;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UsuarioController {

    public static void obtenerResumenFinanciero(String userId, UsuarioFinanzasCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onError("ID de usuario inválido");
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(userId)
                .child("pagos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onSuccess(0, 0);
                            return;
                        }

                        int deudaTotal = 0;
                        long ultimaFechaMovimiento = 0;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Pago pago = snap.getValue(Pago.class);
                            if (pago == null) continue;

                            deudaTotal += Math.max(0, pago.getSaldoPendiente());

                            long fechaMovimiento = obtenerFechaMovimiento(pago);
                            if (fechaMovimiento > ultimaFechaMovimiento) {
                                ultimaFechaMovimiento = fechaMovimiento;
                            }
                        }

                        long diasAtraso = 0;
                        if (ultimaFechaMovimiento > 0 && deudaTotal > 0) {
                            diasAtraso = (System.currentTimeMillis() - ultimaFechaMovimiento)
                                    / (1000L * 60L * 60L * 24L);
                        }

                        callback.onSuccess(deudaTotal, diasAtraso);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    private static long obtenerFechaMovimiento(Pago pago) {
        if (pago == null) return 0;
        if (pago.getUltimaActualizacion() > 0) return pago.getUltimaActualizacion();
        if (pago.getFechaCreacion() > 0) return pago.getFechaCreacion();
        return 0;
    }

    public static void cambiarEstadoActivo(String userId, boolean activo) {
        if (userId == null || userId.trim().isEmpty()) return;

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(userId)
                .child("activo")
                .setValue(activo);
    }

    public interface UsuarioFinanzasCallback {
        void onSuccess(int deudaTotal, long diasAtraso);
        void onError(String error);
    }
}
