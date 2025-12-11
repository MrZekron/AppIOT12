package com.example.appiot12;
// 📦 Módulo encargado de registrar acciones del usuario para auditoría y trazabilidad.
// El “caja negra” digital del sistema ✈️📊

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class HistorialLogger {

    /**
     * 🧠 registrarAccion()
     *
     * Registra una acción en el historial del usuario actual.
     * Este log queda guardado en Firebase en:
     *
     *   usuarios/{uid}/historial/{idLog}
     *
     * Cada entrada contiene:
     *  - tipo: "crear", "editar", "eliminar", etc.
     *  - descripcion: texto amigable de la acción
     *  - timestamp: milisegundos exactos para orden temporal
     */
    public static void registrarAccion(String tipo, String descripcion) {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // 🛑 Si no hay usuario logueado → no registramos nada
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        // Seguridad adicional: evitar UID inválido (extremadamente raro, pero elegante)
        if (uid == null || uid.isEmpty()) return;

        // 🆕 Crear objeto de log
        // AccionLog ya genera:
        // - UUID único
        // - timestamp automático
        // - tipo + descripcion
        AccionLog log = new AccionLog(tipo, descripcion);

        // 📤 Guardar acción en Firebase bajo historial del usuario
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial")
                .child(log.getId())   // Usamos UUID como key del log
                .setValue(log);       // Subimos el objeto completo

        // 🎯 No necesitamos listeners aquí:
        // Para auditoría no importa la confirmación, solo registrar.
    }
}
