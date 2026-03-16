package com.example.appiot12;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Servicio central para registrar eventos del historial del usuario.
 *
 * Aquí se guardan:
 * - eventos simples
 * - lecturas diarias de sensores
 * - pagos realizados
 * - limpieza automática de historial antiguo
 */
public class HistorialService {

    /**
     * Obtiene el UID del usuario autenticado.
     *
     * @return UID del usuario actual o null si no hay sesión iniciada.
     */
    private static String uid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Obtiene la referencia al historial del usuario actual.
     *
     * @return referencia Firebase al historial o null si no hay usuario logueado.
     */
    private static DatabaseReference getHistorialRef() {
        String uid = uid();
        if (uid == null) {
            return null;
        }

        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");
    }

    /**
     * Registra un evento simple en el historial.
     *
     * @param tipo tipo del evento (ej: LOGIN, TANQUE, DISPOSITIVO, etc.)
     * @param descripcion descripción legible del evento
     */
    public static void registrarEvento(String tipo, String descripcion) {

        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        String id = ref.push().getKey();
        if (id == null) return;

        HistorialEvento evento = new HistorialEvento(
                id,
                tipo,
                descripcion,
                System.currentTimeMillis()
        );

        ref.child(id).setValue(evento);
    }

    /**
     * Registra una lectura diaria de sensores asociada a un tanque.
     *
     * @param idTanque ID del tanque
     * @param ph valor de pH
     * @param cond valor de conductividad
     * @param turb valor de turbidez
     * @param nivel nivel de agua
     */
    public static void registrarSensorDiario(
            String idTanque,
            double ph,
            double cond,
            double turb,
            double nivel
    ) {

        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        String id = ref.push().getKey();
        if (id == null) return;

        HistorialEvento evento = new HistorialEvento(
                id,
                "SENSOR",
                "Estado diario del agua",
                System.currentTimeMillis()
        );

        // Guardamos datos específicos del evento de sensores
        evento.setIdTanque(idTanque);
        evento.setPh(ph);
        evento.setConductividad(cond);
        evento.setTurbidez(turb);
        evento.setNivel(nivel);

        ref.child(id).setValue(evento);
    }

    /**
     * Registra un pago realizado por el usuario.
     *
     * @param monto monto pagado
     * @param cuotasPagadas cantidad de cuotas pagadas hasta ahora
     */
    public static void registrarPago(double monto, int cuotasPagadas) {

        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        String id = ref.push().getKey();
        if (id == null) return;

        HistorialEvento evento = new HistorialEvento(
                id,
                "PAGO",
                "Pago de cuota",
                System.currentTimeMillis()
        );

        // Guardamos datos específicos del pago
        evento.setMonto(monto);
        evento.setCuotasPagadas(cuotasPagadas);

        ref.child(id).setValue(evento);
    }

    /**
     * Elimina del historial los eventos con más de 30 días de antigüedad.
     */
    public static void limpiarHistorialAntiguo() {

        DatabaseReference ref = getHistorialRef();
        if (ref == null) return;

        long limite = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        ref.orderByChild("timestamp")
                .endAt(limite)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot s : snapshot.getChildren()) {
                            s.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Aquí podrías agregar log si luego quieres depurar errores Firebase
                    }
                });
    }
}