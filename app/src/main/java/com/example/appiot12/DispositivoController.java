package com.example.appiot12;
// 📦 Controlador central del módulo IoT del proyecto Agua Segura.
// Aquí se gestiona TODO el ciclo de vida de los dispositivos 🤖💧☁️

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
// ☁️ Firebase Auth + Realtime Database = cerebro en la nube

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 🚀 DispositivoController
 *
 * ¿Qué hace esta clase?
 * 👉 Crear dispositivos cuando el usuario compra uno
 * 👉 Listar dispositivos disponibles (libres)
 * 👉 Buscar un dispositivo por ID
 * 👉 Asociar un dispositivo a un tanque
 * 👉 Liberar un dispositivo cuando se borra un tanque
 *
 * Explicado para un niño:
 * 👉 Es el encargado de decir dónde vive cada robot 🤖🏠
 */
public class DispositivoController {

    // =====================================================
    // 🔐 OBTENER REFERENCIA A /usuarios/{uid}/dispositivos
    // =====================================================
    private static DatabaseReference getDispositivosUsuarioRef() {

        // 👤 Obtenemos el usuario actual
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null; // ❌ No hay usuario
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ☁️ Devolvemos la ruta exacta de los dispositivos del usuario
        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos");
    }

    // =====================================================
    // 🛒 CREAR DISPOSITIVO CUANDO SE COMPRA
    // =====================================================
    public static void crearDispositivoComprado(FirebaseCallback callback) {

        DatabaseReference ref = getDispositivosUsuarioRef();

        if (ref == null) {
            callback.onError("Usuario no autenticado ❌");
            return;
        }

        // 🆔 Generamos ID único
        String idDispositivo = UUID.randomUUID().toString();

        // 🤖 Creamos dispositivo con valores iniciales seguros
        Dispositivo dispositivo = new Dispositivo(
                idDispositivo,
                7.0,     // 🧪 pH neutro
                500.0,   // ⚡ Conductividad base
                1.0,     // 🌫️ Turbidez limpia
                100.0    // 📏 Nivel inicial
        );

        // ☁️ Guardamos el dispositivo en Firebase
        ref.child(idDispositivo)
                .setValue(dispositivo)
                .addOnSuccessListener(a ->
                        callback.onSuccess(idDispositivo)
                )
                .addOnFailureListener(e ->
                        callback.onError(e.getMessage())
                );
    }

    // =====================================================
    // 📦 OBTENER DISPOSITIVOS LIBRES (SIN TANQUE)
    // =====================================================
    public static void obtenerDispositivosLibres(
            FirebaseListCallback<Dispositivo> callback
    ) {

        DatabaseReference ref = getDispositivosUsuarioRef();

        if (ref == null) {
            callback.onError("Usuario no autenticado ❌");
            return;
        }

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                List<Dispositivo> dispositivosLibres = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    Dispositivo d = snap.getValue(Dispositivo.class);

                    if (d == null) continue;

                    // 🏠 Si no tiene tanque, está libre
                    if (d.getIdTanque() == null) {
                        dispositivosLibres.add(d);
                    }
                }

                callback.onSuccess(dispositivosLibres);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // =====================================================
    // 🔍 BUSCAR DISPOSITIVO POR ID
    // =====================================================
    public static void buscarDispositivoPorId(
            String idDispositivo,
            FirebaseObjectCallback<Dispositivo> callback
    ) {

        DatabaseReference ref = getDispositivosUsuarioRef();

        if (ref == null) {
            callback.onError("Usuario no autenticado ❌");
            return;
        }

        ref.child(idDispositivo)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Dispositivo dispositivo =
                                snapshot.getValue(Dispositivo.class);

                        if (dispositivo == null) {
                            callback.onError("Dispositivo no encontrado 🔍❌");
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

    // =====================================================
    // 🔗 ASOCIAR DISPOSITIVO A TANQUE
    // =====================================================
    public static void asociarDispositivoATanque(
            String idDispositivo,
            String idTanque,
            FirebaseCallback callback
    ) {

        DatabaseReference ref = getDispositivosUsuarioRef();

        if (ref == null) {
            callback.onError("Usuario no autenticado ❌");
            return;
        }

        // 🔗 Guardamos el ID del tanque dentro del dispositivo
        ref.child(idDispositivo)
                .child("idTanque")
                .setValue(idTanque)
                .addOnSuccessListener(a ->
                        callback.onSuccess(idDispositivo)
                )
                .addOnFailureListener(e ->
                        callback.onError(e.getMessage())
                );
    }

    // =====================================================
    // ♻️ LIBERAR DISPOSITIVO (QUEDA DISPONIBLE)
    // =====================================================
    public static void liberarDispositivo(
            String idDispositivo,
            FirebaseCallback callback
    ) {

        DatabaseReference ref = getDispositivosUsuarioRef();

        if (ref == null) {
            callback.onError("Usuario no autenticado ❌");
            return;
        }

        // 📦 Dejamos idTanque en null
        ref.child(idDispositivo)
                .child("idTanque")
                .setValue(null)
                .addOnSuccessListener(a ->
                        callback.onSuccess(idDispositivo)
                )
                .addOnFailureListener(e ->
                        callback.onError(e.getMessage())
                );
    }

    // =====================================================
    // 🔧 CALLBACKS (RESPUESTAS DE FIREBASE)
    // =====================================================

    /**
     * ✅ Callback simple (devuelve un ID)
     */
    public interface FirebaseCallback {
        void onSuccess(String idResult); // ✔️ Operación exitosa
        void onError(String error);      // ❌ Error con mensaje
    }

    /**
     * 📦 Callback para un solo objeto
     */
    public interface FirebaseObjectCallback<T> {
        void onSuccess(T object); // ✔️ Objeto recibido
        void onError(String error);
    }

    /**
     * 📋 Callback para listas
     */
    public interface FirebaseListCallback<T> {
        void onSuccess(List<T> lista); // ✔️ Lista recibida
        void onError(String error);
    }
}
