package com.example.appiot12.model;

import java.io.Serializable;

public class LecturaParametro implements Serializable {

    private String idLectura;
    private String idDispositivo;
    private String idTanque;
    private String idCliente;     // FK redundante para consultas sin joins

    private double ph;
    private double conductividad;
    private double turbidez;
    private double ultrasonico;

    private long timestamp;

    public LecturaParametro() {}

    public LecturaParametro(String idLectura, String idDispositivo,
                             String idTanque, String idCliente,
                             double ph, double conductividad,
                             double turbidez, double ultrasonico) {
        this.idLectura = idLectura;
        this.idDispositivo = idDispositivo;
        this.idTanque = idTanque;
        this.idCliente = idCliente;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;
        this.timestamp = System.currentTimeMillis();
    }

    public String getIdLectura() { return idLectura; }
    public void setIdLectura(String idLectura) { this.idLectura = idLectura; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) { this.conductividad = conductividad; }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) { this.turbidez = turbidez; }

    public double getUltrasonico() { return ultrasonico; }
    public void setUltrasonico(double ultrasonico) { this.ultrasonico = ultrasonico; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
