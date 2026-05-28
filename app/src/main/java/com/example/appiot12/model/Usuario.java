package com.example.appiot12.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Usuario implements Serializable {

    private String id;
    private String correo;
    private String rol;
    private boolean bloqueado = false;
    private String idCliente;  // FK → Cliente (null si rol=admin o rol=tecnico)

    // Firebase requiere constructor vacío
    public Usuario() {}

    public Usuario(String id, String correo, String rol) {
        this.id = id;
        this.correo = correo;
        this.rol = rol;
        this.bloqueado = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("correo", correo);
        map.put("rol", rol);
        map.put("bloqueado", bloqueado);
        if (idCliente != null) map.put("idCliente", idCliente);
        return map;
    }

    @Override
    public String toString() {
        return correo + " | Rol: " + rol + " | Bloqueado: " + (bloqueado ? "Sí" : "No");
    }

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
