package com.example.appiot12.controller;

// 📦 Controlador lógico de usuarios.
// Aquí vive la lógica administrativa relacionada con pagos y bloqueo.

import androidx.annotation.NonNull;

import com.example.appiot12.model.Pago;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * 🌟 UsuarioController 🌟
 *
 * Responsabilidades:
 * ✔ Calcular deuda total del usuario
 * ✔ Calcular días de atraso
 * ✔ Bloquear / desbloquear cuentas
 *
 * ❌ No maneja UI
 * ❌ No renderiza pantallas
 *
 * Solo lógica de negocio.
 */
public class UsuarioController {

    // ============================================================
    // 📊 OBTENER RESUMEN FINANCIERO DEL USUARIO
    // ============================================================
    /**
     * Lee los pagos del usuario y calcula:
     * - deuda total pendiente
     * - días de atraso desde el último movimiento relevante
     *
     * Regla usada:
     * - si el pago tiene ultimaActualizacion > 0, se usa esa fecha
     * - si no, se usa fechaCreacion
     *
     * @param userId ID del usuario
     * @param callback callback con el resultado
     */
    public static void obtenerResumenFinanciero(
            String userId,
            UsuarioFinanzasCallback callback
    ) {

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

                        // Usuario sin pagos registrados
                        if (!snapshot.exists()) {
                            callback.onSuccess(0, 0);
                            return;
                        }

                        int deudaTotal = 0;
                        long ultimaFechaMovimiento = 0;

                        // Recorremos todos los pagos
                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Pago pago = snap.getValue(Pago.class);
                            if (pago == null) {
                                continue;
                            }

                            // Acumular deuda pendiente
                            deudaTotal += Math.max(0, pago.getSaldoPendiente());

                            // Elegimos la fecha más útil disponible
                            long fechaMovimiento = obtenerFechaMovimiento(pago);

                            if (fechaMovimiento > ultimaFechaMovimiento) {
                                ultimaFechaMovimiento = fechaMovimiento;
                            }
                        }

                        // Calcular días de atraso
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

    // ============================================================
    // 🕒 OBTENER FECHA DE REFERENCIA DEL PAGO
    // ============================================================
    /**
     * Devuelve la fecha más útil para medir atraso.
     *
     * Prioridad:
     * 1. ultimaActualizacion
     * 2. fechaCreacion
     *
     * @param pago objeto pago
     * @return timestamp en milisegundos
     */
    private static long obtenerFechaMovimiento(Pago pago) {
        if (pago == null) {
            return 0;
        }

        if (pago.getUltimaActualizacion() > 0) {
            return pago.getUltimaActualizacion();
        }

        if (pago.getFechaCreacion() > 0) {
            return pago.getFechaCreacion();
        }

        return 0;
    }

    // ============================================================
    // 🔐 BLOQUEAR O DESBLOQUEAR USUARIO
    // ============================================================
    /**
     * Cambia el estado de bloqueo del usuario.
     *
     * @param userId ID del usuario
     * @param bloquear true = bloqueado, false = activo
     */
    public static void cambiarEstadoBloqueo(
            String userId,
            boolean bloquear
    ) {

        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

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
         * Respuesta exitosa.
         *
         * @param deudaTotal dinero pendiente
         * @param diasAtraso días de atraso calculados
         */
        void onSuccess(int deudaTotal, long diasAtraso);

        /**
         * Error al leer Firebase.
         *
         * @param error mensaje de error
         */
        void onError(String error);
    }
}
