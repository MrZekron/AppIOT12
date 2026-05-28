package com.example.appiot12.model;

import java.io.Serializable;

public class Cliente implements Serializable {

    private String idCliente;
    private String idUsuario;      // FK → Usuario (Firebase Auth uid)
    private String nombre;
    private String apellido;
    private String rut;
    private String telefono;
    private String correoContacto;
    private long fechaRegistro;
    private boolean activo;

    public Cliente() {}

    public Cliente(String idCliente, String idUsuario, String nombre, String apellido,
                   String rut, String telefono, String correoContacto) {
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
        this.telefono = telefono;
        this.correoContacto = correoContacto;
        this.fechaRegistro = System.currentTimeMillis();
        this.activo = true;
    }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreoContacto() { return correoContacto; }
    public void setCorreoContacto(String correoContacto) { this.correoContacto = correoContacto; }

    public long getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(long fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    @Override
    public String toString() {
        return getNombreCompleto().trim();
    }
}
