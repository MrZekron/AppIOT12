package com.example.appiot12;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 🌟 MODELO DE USUARIO 🌟
 *
 * Información del usuario:
 * - ID, correo, rol
 * - Tanques que posee
 * - Estado de bloqueo
 *
 * IMPORTANTE:
 * Ya NO maneja pagos. Los pagos viven dentro del Dispositivo.
 */
public class Usuario implements Serializable {

    private String id;
    private String correo;
    private String password;
    private String rol;

    // Tanques del usuario
    private Map<String, TanqueAgua> tanques = new HashMap<>();

    // Estado de bloqueo
    private boolean bloqueado = false;

    public Usuario() {}

    public Usuario(String id, String correo, String password, String rol) {
        this.id = id;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.bloqueado = false;
    }

    // GETTERS & SETTERS

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

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

    // Añadir tanque al usuario
    public void addTanque(TanqueAgua tanque) {
        if (tanques == null) tanques = new HashMap<>();
        tanques.put(tanque.getIdTanque(), tanque);
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("correo", correo);
        map.put("rol", rol);
        map.put("tanques", tanques);
        map.put("bloqueado", bloqueado);
        return map;
    }

    @Override
    public String toString() {
        return correo +
                " | Rol: " + rol +
                " | Bloqueado: " + (bloqueado ? "Sí ❌" : "No ✔") +
                " | Tanques: " + (tanques != null ? tanques.size() : 0);
    }

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
