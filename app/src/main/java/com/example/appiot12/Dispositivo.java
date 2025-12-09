package com.example.appiot12;

/**
 * 🌟 CLASE DISPOSITIVO 🌟
 *
 * Representa el corazón tecnológico del tanque 🧠💧.
 *
 * IMPORTANTE:
 * - NO guarda pagos (los maneja usuarios/{id}/pagos).
 * - Cada dispositivo puede estar asociado solo a 1 tanque.
 * - Si idTanque = null → el dispositivo está libre.
 */

public class Dispositivo {

    // 🆔 Identificador único del dispositivo
    private String id;

    // ⭐ Nuevo! Tanque al que pertenece (o null)
    private String idTanque;

    // === SENSORES REALES ===
    private double ph;
    private double conductividad;
    private double turbidez;
    private double ultrasonico;

    // === ESTADOS CALCULADOS ===
    private String estadoPH;
    private String estadoConductividad;
    private String estadoTurbidez;

    // === CONSTRUCTOR VACÍO (Firebase lo necesita) ===
    public Dispositivo() {}

    // === CONSTRUCTOR COMPLETO ===
    public Dispositivo(String id, double ph, double conductividad, double turbidez, double ultrasonico) {
        this.id = id;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;

        this.estadoPH = "N/A";
        this.estadoConductividad = "N/A";
        this.estadoTurbidez = "N/A";

        this.idTanque = null;  // ⭐ dispositivo libre al crearse
    }

    // === GETTERS & SETTERS ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) { this.conductividad = conductividad; }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) { this.turbidez = turbidez; }

    public double getUltrasonico() { return ultrasonico; }
    public void setUltrasonico(double ultrasonico) { this.ultrasonico = ultrasonico; }

    public String getEstadoPH() { return estadoPH; }
    public void setEstadoPH(String estadoPH) { this.estadoPH = estadoPH; }

    public String getEstadoConductividad() { return estadoConductividad; }
    public void setEstadoConductividad(String estadoConductividad) { this.estadoConductividad = estadoConductividad; }

    public String getEstadoTurbidez() { return estadoTurbidez; }
    public void setEstadoTurbidez(String estadoTurbidez) { this.estadoTurbidez = estadoTurbidez; }
}
