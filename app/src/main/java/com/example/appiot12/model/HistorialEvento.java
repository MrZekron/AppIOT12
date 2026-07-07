package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class HistorialEvento implements Serializable {

    private String idHistorial;
    private String idUsuario;           // FK → Usuario (quien realizó la acción)
    private String accion;              // descripción de la acción realizada
    private String entidadAfectada;     // tanque / dispositivo / pago / alerta / etc.
    private String idEntidadAfectada;   // ID del objeto afectado
    private long timestamp;
    private String detalle;             // JSON serializado con datos adicionales

    public HistorialEvento() {}

    public HistorialEvento(String idHistorial, String idUsuario, String accion,
                           String entidadAfectada, String idEntidadAfectada) {
        this.idHistorial = idHistorial;
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.entidadAfectada = entidadAfectada;
        this.idEntidadAfectada = idEntidadAfectada;
        this.timestamp = System.currentTimeMillis();
    }

    public String getIdHistorial() { return idHistorial; }
    public void setIdHistorial(String idHistorial) { this.idHistorial = idHistorial; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidadAfectada() { return entidadAfectada; }
    public void setEntidadAfectada(String entidadAfectada) { this.entidadAfectada = entidadAfectada; }

    public String getIdEntidadAfectada() { return idEntidadAfectada; }
    public void setIdEntidadAfectada(String idEntidadAfectada) { this.idEntidadAfectada = idEntidadAfectada; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    // Aliases para compatibilidad con HistorialAdapter
    public String getTipo() { return entidadAfectada; }
    public void setTipo(String tipo) { this.entidadAfectada = tipo; }

    public String getDescripcion() { return accion; }
    public void setDescripcion(String descripcion) { this.accion = descripcion; }

    // Alias para compatibilidad con código que usa "id" como campo antiguo
    public String getId() { return idHistorial; }
    public void setId(String id) { this.idHistorial = id; }

    @Override
    public String toString() {
        return "[" + entidadAfectada + "] " + accion;
    }
}
