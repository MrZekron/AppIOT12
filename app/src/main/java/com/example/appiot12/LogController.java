package com.example.appiot12;
// 📦 Clase utilitaria para registrar acciones en el historial del usuario.
// Es como un “mini-HistorialLogger 2.0” compatible con Firebase 📊🔥

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LogController {

    /**
     * 📝 registrarAccion()
     *
     * Registra un evento en:
     *      usuarios/{uid}/historial/{idLog}
     *
     * Cada log incluye:
     *  - tipo: crear / editar / eliminar / compra / alerta / etc.
     *  - descripcion: texto entendible de qué ocurrió
     *  - timestamp: generado automáticamente por AccionLog
     *  - id único: UUID automático
     *
     * Este método sirve como “cámara de seguridad digital” del sistema.
     */
    public static void registrarAccion(String tipo, String descripcion) {

        // 🔐 Verificar usuario autenticado
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            // Si no hay usuario, no registramos nada (modo silencioso)
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Validación adicional (muy improbable que ocurra)
        if (uid == null || uid.isEmpty()) return;

        // 🆕 Crear estructura del log
        AccionLog log = new AccionLog(tipo, descripcion);

        // 📤 Guardar log en Firebase
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial")
                .child(log.getId())    // El ID del log se usa como key
                .setValue(log);        // Subimos el objeto completo
    }
}
