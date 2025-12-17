package com.example.appiot12;
// 📦 Controlador único de logs del sistema.
// Esta clase es la "caja negra" oficial de AguaSegura ✈️📊

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/**
 * 🧠 LOG CONTROLLER
 *
 * ¿Qué hace?
 * 👉 Guarda lo que hace el usuario, como un cuaderno invisible 📓
 *
 * ¿Para qué sirve?
 * 👉 Para auditoría
 * 👉 Para historial
 * 👉 Para saber quién hizo qué y cuándo ⏰
 *
 * Explicado para un niño 👶:
 * 👉 Es como cuando anotas en un cuaderno:
 *    "Hoy creé un tanque" ✍️
 */
public class LogController {

    /**
     * 📝 registrarAccion
     *
     * Guarda una acción en Firebase en:
     * usuarios/{uid}/historial/{idLog}
     *
     * Cada registro tiene:
     * - tipo 👉 qué pasó (crear, editar, borrar, comprar)
     * - descripcion 👉 explicación simple
     * - timestamp 👉 cuándo pasó ⏰
     */
    public static void registrarAccion(String tipo, String descripcion) {

        // 🔐 Obtener usuario actual
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // ❌ Si no hay usuario, no hacemos nada (modo silencioso)
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        // 🛡️ Seguridad extra (rara vez pasa, pero somos profesionales)
        if (uid == null || uid.isEmpty()) return;

        // 🆕 Crear el log (AccionLog genera ID + timestamp solo)
        AccionLog log = new AccionLog(tipo, descripcion);

        // ☁️ Guardar en Firebase
        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("historial")
                .child(log.getId())
                .setValue(log);
    }
}
