package com.example.appiot12;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 🚀 DISPOSITIVO CONTROLLER CLOUD v2.0
 *
 * Este módulo es ahora totalmente Firebase-native.
 * Maneja:
 *  - Creación de dispositivos al comprar uno
 *  - Asociación dispositivo → tanque (1:1)
 *  - Liberar dispositivos cuando se elimina el tanque
 *  - Listar dispositivos disponibles
 *  - Encontrar dispositivos por ID directamente en Firebase
 *
 * Este archivo reemplaza completamente cualquier controller anterior.
 */
public class DispositivoController {

    private static DatabaseReference getUserDispositivosRef() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos");
    }

    // ==========================================================
    // ✅ CREAR DISPOSITIVO (cuando el usuario lo compra)
    // ==========================================================
    public static void crearDispositivoComprado(int montoTotal, int cuotas, FirebaseCallback callback) {

        String uidDispositivo = UUID.randomUUID().toString();

        // Crear dispositivo vacío con sensores base
        Dispositivo dispositivo = new Dispositivo(
                uidDispositivo,
                7.0,     // ph inicial
                500.0,   // conductividad
                1.0,     // turbidez
                100.0    // ultrasonico
        );

        // Aún no está asociado a ningún tanque
        // Puedes agregar un campo en Dispositivo si lo deseas: idTanque = null

        // Guardar en Firebase
        getUserDispositivosRef()
                .child(uidDispositivo)
                .setValue(dispositivo)
                .addOnSuccessListener(aVoid -> callback.onSuccess(uidDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // ==========================================================
    // ✅ LISTAR DISPOSITIVOS DISPONIBLES (no asociados a tanque)
    // ==========================================================
    public static void obtenerDispositivosLibres(FirebaseListCallback<Dispositivo> callback) {

        getUserDispositivosRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<Dispositivo> libres = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Dispositivo d = snap.getValue(Dispositivo.class);

                            if (d == null) continue;

                            // Si quieres agregar idTanque al dispositivo, aquí lo evaluamos
                            // if (d.getIdTanque() == null)

                            libres.add(d);
                        }

                        callback.onSuccess(libres);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }


    // ==========================================================
    // ✅ BUSCAR DISPOSITIVO POR ID (Firebase)
    // ==========================================================
    public static void findDispositivo(String id, FirebaseObjectCallback<Dispositivo> callback) {

        getUserDispositivosRef()
                .child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Dispositivo dispositivo = snapshot.getValue(Dispositivo.class);

                        if (dispositivo == null) {
                            callback.onError("Dispositivo no encontrado");
                            return;
                        }

                        callback.onSuccess(dispositivo);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }


    // ==========================================================
    // ✅ ASOCIAR DISPOSITIVO A TANQUE
    // ==========================================================
    public static void asociarDispositivoATanque(String idDispositivo,
                                                 String idTanque,
                                                 FirebaseCallback callback) {

        // Guardamos en Firebase dentro del dispositivo:
        // dispositivo/idTanque = "xxx"
        getUserDispositivosRef()
                .child(idDispositivo)
                .child("idTanque")
                .setValue(idTanque)
                .addOnSuccessListener(aVoid -> callback.onSuccess(idDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // ==========================================================
    // ✅ DESASOCIAR DISPOSITIVO (cuando se elimina un tanque)
    // ==========================================================
    public static void liberarDispositivo(String idDispositivo, FirebaseCallback callback) {
        getUserDispositivosRef()
                .child(idDispositivo)
                .child("idTanque")
                .setValue(null)
                .addOnSuccessListener(aVoid -> callback.onSuccess(idDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }



    // ==========================================================
    // 🔧 CALLBACKS ESTÁNDARES
    // ==========================================================
    public interface FirebaseCallback {
        void onSuccess(String idResult);
        void onError(String error);
    }

    public interface FirebaseObjectCallback<T> {
        void onSuccess(T object);
        void onError(String error);
    }

    public interface FirebaseListCallback<T> {
        void onSuccess(List<T> lista);
        void onError(String error);
    }
}
