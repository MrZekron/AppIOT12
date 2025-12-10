package com.example.appiot12;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LogController {

    public static void registrarAccion(String tipo, String descripcion) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null) return;

        AccionLog log = new AccionLog(tipo, descripcion);

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial")
                .child(log.getId())
                .setValue(log);
    }
}
