package com.example.appiot12.controller;

import androidx.annotation.NonNull;

import com.example.appiot12.model.Dispositivo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DispositivoController {

    private static DatabaseReference getDispositivosUsuarioRef() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return null;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("dispositivos");
    }

    public static void crearDispositivoComprado(FirebaseCallback callback) {
        DatabaseReference ref = getDispositivosUsuarioRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String idDispositivo = UUID.randomUUID().toString();
        Dispositivo dispositivo = new Dispositivo(idDispositivo, 7.0, 500.0, 1.0, 100.0);

        ref.child(idDispositivo).setValue(dispositivo)
                .addOnSuccessListener(a -> callback.onSuccess(idDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void obtenerDispositivosLibres(FirebaseListCallback<Dispositivo> callback) {
        DatabaseReference ref = getDispositivosUsuarioRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Dispositivo> dispositivosLibres = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Dispositivo d = snap.getValue(Dispositivo.class);
                    if (d != null && d.getIdTanque() == null) {
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

    public static void buscarDispositivoPorId(String idDispositivo, FirebaseObjectCallback<Dispositivo> callback) {
        DatabaseReference ref = getDispositivosUsuarioRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        ref.child(idDispositivo).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public static void asociarDispositivoATanque(String idDispositivo, String idTanque, FirebaseCallback callback) {
        DatabaseReference ref = getDispositivosUsuarioRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        ref.child(idDispositivo).child("idTanque").setValue(idTanque)
                .addOnSuccessListener(a -> callback.onSuccess(idDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void liberarDispositivo(String idDispositivo, FirebaseCallback callback) {
        DatabaseReference ref = getDispositivosUsuarioRef();
        if (ref == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        ref.child(idDispositivo).child("idTanque").setValue(null)
                .addOnSuccessListener(a -> callback.onSuccess(idDispositivo))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

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
