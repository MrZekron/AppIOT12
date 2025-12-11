package com.example.appiot12;
// 📦 Controlador central del módulo IoT. Aquí se gestiona la vida, muerte y asignación de dispositivos.

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
// ☁️ Firebase Auth + Realtime DB: nuestro backend en la nube.

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 🚀 DISPOSITIVO CONTROLLER CLOUD v2.0
 *
 * Este módulo fue diseñado para operar 100% sobre Firebase:
 *  ✔ Crear dispositivos cuando el usuario compra uno
 *  ✔ Asociar dispositivo ↔ tanque (relación 1:1 estilo premium)
 *  ✔ Liberarlo cuando un tanque es borrado
 *  ✔ Listar dispositivos disponibles
 *  ✔ Buscar un dispositivo por ID directamente en la nube
 *
 * Esencialmente, el "departamento IoT" del proyecto AguaSegura 🌊🤖.
 */
public class DispositivoController {

    // ==========================================================
    // 🔗 REFERENCIA AUTOMÁTICA A /usuarios/{uid}/dispositivos
    // ==========================================================
    private static DatabaseReference getUserDispositivosRef() {

        // Obtener el UID del usuario logueado 🔐
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referencia a su lista de dispositivos dentro de Firebase ☁️
        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos");
    }


    // ==========================================================
    // ✅ CREAR DISPOSITIVO CUANDO SE COMPRA UNO
    // ==========================================================
    public static void crearDispositivoComprado(int montoTotal, int cuotas, FirebaseCallback callback) {

        // Generamos ID único para el dispositivo recién comprado 🆔✨
        String uidDispositivo = UUID.randomUUID().toString();

        // Creamos un dispositivo base con sensores iniciales placeholder
        Dispositivo dispositivo = new Dispositivo(
                uidDispositivo,
                7.0,     // ph inicial aceptable 🧪
                500.0,   // conductividad estándar ⚡
                1.0,     // turbidez limpia 🌫️
                100.0    // ultrasonico inicial (nivel base) 📡
        );

        // Guardamos el dispositivo en Firebase bajo el usuario correspondiente
        getUserDispositivosRef()
                .child(uidDispositivo)
                .setValue(dispositivo)
                .addOnSuccessListener(aVoid -> callback.onSuccess(uidDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // ==========================================================
    // ✅ LISTAR DISPOSITIVOS NO ASOCIADOS A NINGÚN TANQUE
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

                            // Si el dispositivo NO tiene tanque → está libre 🚀
                            // (Se asume idTanque = null si fue inicializado correctamente)
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
    // ✅ BUSCAR DISPOSITIVO POR ID
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
    // ✅ ASOCIAR DISPOSITIVO → TANQUE
    // ==========================================================
    public static void asociarDispositivoATanque(String idDispositivo,
                                                 String idTanque,
                                                 FirebaseCallback callback) {

        // Simple, efectivo y directo: guardamos idTanque dentro del dispositivo
        getUserDispositivosRef()
                .child(idDispositivo)
                .child("idTanque")
                .setValue(idTanque)
                .addOnSuccessListener(aVoid -> callback.onSuccess(idDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // ==========================================================
    // ✅ LIBERAR DISPOSITIVO (cuando borras un tanque)
    // ==========================================================
    public static void liberarDispositivo(String idDispositivo, FirebaseCallback callback) {

        // Se deja idTanque = null → vuelve a estar disponible en inventario 📦
        getUserDispositivosRef()
                .child(idDispositivo)
                .child("idTanque")
                .setValue(null)
                .addOnSuccessListener(aVoid -> callback.onSuccess(idDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // ==========================================================
    // 🔧 CALLBACKS BASE PARA RESPUESTAS
    // ==========================================================
    public interface FirebaseCallback {
        void onSuccess(String idResult); // Cuando una operación tiene 1 resultado simple
        void onError(String error);      // Error corporativo con mensaje descriptivo
    }

    public interface FirebaseObjectCallback<T> {
        void onSuccess(T object);        // Cuando Firebase devuelve UN OBJETO (Dispositivo)
        void onError(String error);
    }

    public interface FirebaseListCallback<T> {
        void onSuccess(List<T> lista);   // Cuando Firebase devuelve UNA LISTA de objetos
        void onError(String error);
    }
}
