package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class TecnicoTanque implements Serializable {

    private String idAsignacion;
    private String idUsuarioTecnico;  // FK → Usuario (rol=tecnico)
    private String idTanque;          // FK → Tanque
    private long fechaAsignacion;
    private boolean activo;

    public TecnicoTanque() {}

    public TecnicoTanque(String idAsignacion, String idUsuarioTecnico, String idTanque) {
        this.idAsignacion = idAsignacion;
        this.idUsuarioTecnico = idUsuarioTecnico;
        this.idTanque = idTanque;
        this.fechaAsignacion = System.currentTimeMillis();
        this.activo = true;
    }

    public String getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(String idAsignacion) { this.idAsignacion = idAsignacion; }

    public String getIdUsuarioTecnico() { return idUsuarioTecnico; }
    public void setIdUsuarioTecnico(String idUsuarioTecnico) { this.idUsuarioTecnico = idUsuarioTecnico; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public long getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(long fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public void desactivar() { this.activo = false; }

    @Override
    public String toString() {
        return "Técnico " + idUsuarioTecnico + " → Tanque " + idTanque + " (" + (activo ? "activo" : "inactivo") + ")";
    }
}
