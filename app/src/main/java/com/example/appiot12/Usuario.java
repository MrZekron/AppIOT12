package com.example.appiot12;
// 📦 Modelo que representa a un usuario dentro del sistema AguaSegura 👤💧
// Este objeto describe QUIÉN es el usuario, no QUÉ hace.

// 🔒 Firebase
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 🌟 MODELO DE USUARIO 🌟
 *
 * Este modelo se guarda en Firebase en:
 *    usuarios/{uid}
 *
 * Contiene SOLO información esencial del usuario:
 * ✔ Identidad
 * ✔ Rol
 * ✔ Estado administrativo
 *
 * ❌ No maneja lógica
 * ❌ No maneja sensores
 * ❌ No maneja pagos
 *
 * Es un modelo LIMPIO, SEGURO y ENTENDIBLE 👶✨
 */
public class Usuario implements Serializable {

    // ==========================
    // 🆔 IDENTIDAD
    // ==========================
    private String id;      // UID de FirebaseAuth
    private String correo;  // Email del usuario

    // ==========================
    // 🏷 ROL DEL SISTEMA
    // ==========================
    private String rol;     // "usuario" o "admin"

    // ==========================
    // 🚫 ESTADO ADMINISTRATIVO
    // ==========================
    private boolean bloqueado = false;

    // ==========================
    // 🔧 CONSTRUCTORES
    // ==========================

    // Constructor vacío → obligatorio para Firebase
    public Usuario() {}

    public Usuario(String id, String correo, String rol) {
        this.id = id;
        this.correo = correo;
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

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    // ==========================
    // 📤 CONVERSIÓN A MAPA
    // Ideal para Firebase updateChildren()
    // ==========================
    @Exclude
    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("correo", correo);
        map.put("rol", rol);
        map.put("bloqueado", bloqueado);

        // ❌ NO hay password
        // ❌ NO hay tanques
        return map;
    }

    // ==========================
    // 🧾 REPRESENTACIÓN HUMANA
    // ==========================
    @Override
    public String toString() {
        return correo +
                " | Rol: " + rol +
                " | Bloqueado: " + (bloqueado ? "Sí ❌" : "No ✔");
    }

    // ==========================
    // ⚖ EQUALS & HASHCODE
    // Comparación segura por identidad
    // ==========================
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;

        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
