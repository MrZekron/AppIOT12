package com.example.appiot12;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class HistorialLogger {

    public static void registrarAccion(String tipo, String descripcion) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        if (uid == null || uid.isEmpty()) return;

        // Crear log correctamente
        AccionLog log = new AccionLog(tipo, descripcion);

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial")
                .child(log.getId())
                .setValue(log);
    }
}
