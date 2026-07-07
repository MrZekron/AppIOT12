package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Cliente implements Serializable {

    private String idCliente;
    private String idUsuario;       // FK → Usuario (Firebase Auth uid)
    private String nombreCompleto;
    private String rut;
    private String telefono;
    private String emailContacto;
    private long fechaRegistro;
    private boolean activo;

    public Cliente() {}

    public Cliente(String idCliente, String idUsuario, String nombreCompleto,
                   String rut, String telefono, String emailContacto) {
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.rut = rut;
        this.telefono = telefono;
        this.emailContacto = emailContacto;
        this.fechaRegistro = System.currentTimeMillis();
        this.activo = true;
    }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public long getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(long fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombreCompleto != null ? nombreCompleto.trim() : "";
    }
}
