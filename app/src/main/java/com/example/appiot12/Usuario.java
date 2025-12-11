package com.example.appiot12;
// 📦 Modelo principal que representa a cada usuario del sistema AguaSegura.
// Este modelo se guarda en Firebase y se usa en gestión de usuarios, autenticación,
// roles, bloqueo/desbloqueo y visualización administrativa.

import com.google.firebase.database.Exclude; // 🔒 Oculta campos en Firebase cuando corresponde

import java.io.Serializable;       // Permite enviar Usuario entre Activities
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 🌟 MODELO DE USUARIO 🌟
 *
 * Contiene:
 *  ✔ id              → UID de FirebaseAuth
 *  ✔ correo          → email del usuario
 *  ✔ password        → NO se guarda en Firebase gracias a @Exclude
 *  ✔ rol             → "usuario" o "admin"
 *  ✔ tanques         → mapa de tanques propiedad del usuario
 *  ✔ bloqueado       → si está suspendido por el administrador
 *
 * IMPORTANTE:
 *  - Los pagos YA NO viven aquí.
 *  - Cada usuario tiene su nodo independiente en Firebase:
 *        usuarios/{uid}/
 *  - Serializable permite enviarlo por Intent sin errores.
 */
public class Usuario implements Serializable {

    // ==========================
    // 🧩 DATOS BÁSICOS DEL USUARIO
    // ==========================
    private String id;       // UID de FirebaseAuth
    private String correo;   // Email visible y público
    private String password; // 🚫 No se sube a Firebase (Solo en sesión actual)
    private String rol;      // admin / usuario

    // ==========================
    // 🧩 RELACIÓN CON TANQUES
    // ==========================
    private Map<String, TanqueAgua> tanques = new HashMap<>();
    // Se almacena como un mapa para acceso rápido y compatibilidad con Firebase.

    // ==========================
    // 🧩 ESTADO ADMINISTRATIVO
    // ==========================
    private boolean bloqueado = false; // true = suspendido por admin

    // ==========================
    // 🧩 CONSTRUCTORES
    // ==========================

    // Constructor vacío requerido por Firebase
    public Usuario() {}

    public Usuario(String id, String correo, String password, String rol) {
        this.id = id;
        this.correo = correo;
        this.password = password; // ⚠️ No se guarda en Firebase gracias a @Exclude
        this.rol = rol;
        this.bloqueado = false;
    }

    // ==========================
    // 📌 GETTERS & SETTERS
    // ==========================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    // 🔒 @Exclude → Firebase ignora este campo.
    // Contraseña JAMÁS debe almacenarse en Realtime Database.
    @Exclude
    public String getPassword() { return password; }

    @Exclude
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Map<String, TanqueAgua> getTanques() { return tanques; }
    public void setTanques(Map<String, TanqueAgua> tanques) { this.tanques = tanques; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    // ==========================
    // ➕ AÑADIR TANQUE AL MAPA
    // ==========================
    public void addTanque(TanqueAgua tanque) {
        if (tanques == null) tanques = new HashMap<>();
        tanques.put(tanque.getIdTanque(), tanque);
    }

    // ==========================
    // 📤 CONVERTER A MAPA (para subir a Firebase)
    // ==========================
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("correo", correo);
        map.put("rol", rol);
        map.put("tanques", tanques);
        map.put("bloqueado", bloqueado);

        // ❌ NO INCLUYE PASSWORD → Seguridad garantizada
        return map;
    }

    // ==========================
    // 📌 toString elegante
    // ==========================
    @Override
    public String toString() {
        return correo +
                " | Rol: " + rol +
                " | Bloqueado: " + (bloqueado ? "Sí ❌" : "No ✔") +
                " | Tanques: " + (tanques != null ? tanques.size() : 0);
    }

    // ==========================
    // ⚖ EQUALS & HASHCODE
    // Para colecciones, Comparadores, Sets
    // ==========================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;

        Usuario usuario = (Usuario) o;

        return Objects.equals(id, usuario.id) &&
                Objects.equals(correo, usuario.correo) &&
                Objects.equals(rol, usuario.rol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, correo, rol);
    }
}
