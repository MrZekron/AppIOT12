package com.example.appiot12;
// 📦 Controlador lógico de usuarios.
// Aquí vive el "cerebro administrativo" 🧠⚙️

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * 🌟 USUARIO CONTROLLER 🌟
 *
 * Responsabilidades:
 * ✔ Calcular deuda total del usuario
 * ✔ Calcular días de atraso
 * ✔ Bloquear / desbloquear cuentas
 *
 * ❌ NO maneja UI
 * ❌ NO pinta pantallas
 *
 * 👉 Solo lógica de negocio
 */
public class UsuarioController {

    // ============================================================
    // 📊 OBTENER RESUMEN FINANCIERO DEL USUARIO
    // ============================================================
    public static void obtenerResumenFinanciero(
            String userId,
            UsuarioFinanzasCallback callback
    ) {

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(userId)
                .child("pagos")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // 🟢 Usuario sin compras
                        if (!snapshot.exists()) {
                            callback.onSuccess(0, 0);
                            return;
                        }

                        int deudaTotal = 0;
                        long ultimaFechaPago = 0;

                        // 🔄 Recorremos todos los pagos
                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Pago pago = snap.getValue(Pago.class);
                            if (pago == null) continue;

                            deudaTotal += pago.getSaldoPendiente();
                            ultimaFechaPago = Math.max(
                                    ultimaFechaPago,
                                    pago.getFechaPago()
                            );
                        }

                        // 📅 Calcular días de atraso
                        long diasAtraso = 0;

                        if (ultimaFechaPago > 0) {
                            diasAtraso = (System.currentTimeMillis() - ultimaFechaPago)
                                    / (1000 * 60 * 60 * 24);
                        }

                        callback.onSuccess(deudaTotal, diasAtraso);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ============================================================
    // 🔐 BLOQUEAR O DESBLOQUEAR USUARIO
    // ============================================================
    public static void cambiarEstadoBloqueo(
            String userId,
            boolean bloquear
    ) {

        // true  → bloqueado ❌
        // false → activo ✔
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(userId)
                .child("bloqueado")
                .setValue(bloquear);
    }

    // ============================================================
    // 📡 CALLBACK PARA FINANZAS
    // ============================================================
    public interface UsuarioFinanzasCallback {

        /**
         * ✔ Respuesta exitosa
         *
         * @param deudaTotal  dinero pendiente
         * @param diasAtraso  días desde el último pago
         */
        void onSuccess(int deudaTotal, long diasAtraso);

        /**
         * ❌ Error leyendo Firebase
         */
        void onError(String error);
    }
}
