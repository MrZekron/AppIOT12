package com.example.appiot12.service;
// 📦 Módulo de auditoría del proyecto Agua Segura.
// Este archivo es la "caja negra" del sistema ✈️📊
// Todo lo importante que hace el usuario queda registrado aquí.

import com.example.appiot12.model.AccionLog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * 🧾 HistorialLogger
 *
 * ¿Qué hace esta clase?
 * 👉 Guarda acciones importantes del usuario
 * 👉 Las deja registradas en Firebase
 * 👉 Permite auditoría y trazabilidad
 *
 * Explicado para un niño:
 * 👉 Es como un cuaderno secreto donde se anota
 *    todo lo importante que hiciste 📒✍️
 */
public class HistorialLogger {

    // =====================================================
    // 🧠 REGISTRAR UNA ACCIÓN DEL USUARIO
    // =====================================================
    public static void registrarAccion(String tipo, String descripcion) {

        // 🔐 Obtenemos la autenticación actual
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // 🛑 Si no hay usuario logueado, no hacemos nada
        if (auth.getCurrentUser() == null) {
            return;
        }

        // 🆔 UID del usuario
        String uid = auth.getCurrentUser().getUid();

        // 🛑 Seguridad extra (por si acaso)
        if (uid == null || uid.isEmpty()) {
            return;
        }

        // 🧾 Creamos el log de la acción
        // AccionLog genera automáticamente:
        // 🆔 ID único
        // ⏰ Timestamp
        AccionLog log = new AccionLog(tipo, descripcion);

        // ☁️ Referencia al historial del usuario
        DatabaseReference refHistorial = FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial");

        // 📤 Guardamos el log usando su ID como llave
        refHistorial
                .child(log.getId())
                .setValue(log);

        // 🎯 No usamos listeners:
        // Para auditoría solo importa registrar, no confirmar.
    }
}
