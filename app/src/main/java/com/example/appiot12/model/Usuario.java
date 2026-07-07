package com.example.appiot12.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@IgnoreExtraProperties
public class Usuario implements Serializable {

    private String id;
    @PropertyName("correo")
    private String email;
    private String nombre;
    private String rol;             // admin / cliente / tecnico
    private boolean activo = true;  // false = cuenta suspendida
    private long fechaCreacion;
    private String idCliente;       // FK → Cliente (solo si rol=cliente)

    public Usuario() {}

    public Usuario(String id, String email, String rol) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.activo = true;
        this.fechaCreacion = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("correo")
    public String getEmail() { return email; }
    @PropertyName("correo")
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("correo", email);
        map.put("nombre", nombre);
        map.put("rol", rol);
        map.put("activo", activo);
        map.put("fechaCreacion", fechaCreacion);
        if (idCliente != null) map.put("idCliente", idCliente);
        return map;
    }

    @Override
    public String toString() {
        return email + " | Rol: " + rol + " | Activo: " + (activo ? "Sí" : "No");
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
