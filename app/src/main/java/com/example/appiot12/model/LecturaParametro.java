package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class LecturaParametro implements Serializable {

    private String idLectura;
    private String idDispositivo;   // FK → Dispositivo (único FK per spec)
    private long timestamp;

    private double ph;
    private double turbidez;
    private double conductividad;
    private double nivelPorcentaje; // 0 – 100 %
    private double nivelCm;         // lectura raw del sensor ultrasónico

    public LecturaParametro() {}

    public LecturaParametro(String idLectura, String idDispositivo,
                             double ph, double conductividad,
                             double turbidez, double nivelCm, double nivelPorcentaje) {
        this.idLectura = idLectura;
        this.idDispositivo = idDispositivo;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.nivelCm = nivelCm;
        this.nivelPorcentaje = nivelPorcentaje;
        this.timestamp = System.currentTimeMillis();
    }

    public String getIdLectura() { return idLectura; }
    public void setIdLectura(String idLectura) { this.idLectura = idLectura; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) { this.turbidez = turbidez; }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) { this.conductividad = conductividad; }

    public double getNivelPorcentaje() { return nivelPorcentaje; }
    public void setNivelPorcentaje(double nivelPorcentaje) { this.nivelPorcentaje = nivelPorcentaje; }

    public double getNivelCm() { return nivelCm; }
    public void setNivelCm(double nivelCm) { this.nivelCm = nivelCm; }
}
