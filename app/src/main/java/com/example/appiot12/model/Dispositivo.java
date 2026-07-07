package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Dispositivo implements Serializable {

    private String id;
    private String idTanque;
    private String estado;           // activo / mantenimiento / baja
    private boolean activo = true;
    private long fechaInstalacion;
    private long fechaRetiro;        // 0 si aún activo

    // Lecturas en tiempo real (RTDB: /tanques/{id}/lectura_actual/)
    private double ph;
    private double conductividad;
    private double turbidez;
    private double ultrasonico;
    private long timestamp;

    // Estados derivados (calculados localmente, no persistidos)
    private transient String estadoPH;
    private transient String estadoConductividad;
    private transient String estadoTurbidez;

    public Dispositivo() {}

    public Dispositivo(String id, double ph, double conductividad, double turbidez, double ultrasonico) {
        this.id = id;
        this.activo = true;
        this.estado = "activo";
        this.fechaInstalacion = System.currentTimeMillis();
        this.fechaRetiro = 0;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;
        this.timestamp = System.currentTimeMillis();
        actualizarEstados();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public long getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(long fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }

    public long getFechaRetiro() { return fechaRetiro; }
    public void setFechaRetiro(long fechaRetiro) { this.fechaRetiro = fechaRetiro; }

    public void darDeBaja() {
        this.activo = false;
        this.estado = "baja";
        this.fechaRetiro = System.currentTimeMillis();
        this.idTanque = null;
    }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; evaluarEstadoPH(); }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) { this.conductividad = conductividad; evaluarEstadoConductividad(); }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) { this.turbidez = turbidez; evaluarEstadoTurbidez(); }

    public double getUltrasonico() { return ultrasonico; }
    public void setUltrasonico(double ultrasonico) { this.ultrasonico = ultrasonico; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEstadoPH() { if (estadoPH == null) evaluarEstadoPH(); return estadoPH; }
    public String getEstadoConductividad() { if (estadoConductividad == null) evaluarEstadoConductividad(); return estadoConductividad; }
    public String getEstadoTurbidez() { if (estadoTurbidez == null) evaluarEstadoTurbidez(); return estadoTurbidez; }

    public void actualizarEstados() {
        evaluarEstadoPH();
        evaluarEstadoConductividad();
        evaluarEstadoTurbidez();
    }

    public boolean estaAsociadoATanque() {
        return idTanque != null && !idTanque.trim().isEmpty();
    }

    public void desasociarTanque() { this.idTanque = null; }

    public String getEstadoGeneral() {
        actualizarEstados();
        if (estadoPH.contains("Peligro") || estadoConductividad.contains("Peligro") || estadoTurbidez.contains("Peligro"))
            return "Peligro";
        if (estadoPH.contains("Alerta") || estadoConductividad.contains("Alerta") || estadoTurbidez.contains("Alerta"))
            return "Alerta";
        return "Normal";
    }

    public String generarResumen() {
        return "Estado del agua:\n" +
                "- pH: " + ph + " (" + getEstadoPH() + ")\n" +
                "- Conductividad: " + conductividad + " (" + getEstadoConductividad() + ")\n" +
                "- Turbidez: " + turbidez + " (" + getEstadoTurbidez() + ")\n" +
                "- Nivel: " + ultrasonico + "\n" +
                "- Estado general: " + getEstadoGeneral();
    }

    private void evaluarEstadoPH() {
        if (ph >= 6.5 && ph <= 8.5) estadoPH = "Normal";
        else if ((ph >= 6.0 && ph < 6.5) || (ph > 8.5 && ph <= 9.0)) estadoPH = "Alerta";
        else estadoPH = "Peligro";
    }

    private void evaluarEstadoConductividad() {
        if (conductividad <= 1500) estadoConductividad = "Normal";
        else if (conductividad <= 2500) estadoConductividad = "Alerta";
        else estadoConductividad = "Peligro";
    }

    private void evaluarEstadoTurbidez() {
        if (turbidez <= 5) estadoTurbidez = "Normal";
        else if (turbidez <= 10) estadoTurbidez = "Alerta";
        else estadoTurbidez = "Peligro";
    }

    @Override
    public String toString() {
        return id != null && !id.trim().isEmpty() ? "Dispositivo " + id : "Dispositivo sin ID";
    }
}
