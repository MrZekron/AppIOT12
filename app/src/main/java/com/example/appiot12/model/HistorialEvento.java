package com.example.appiot12.model;

public class HistorialEvento {

    private String id;
    private String tipo;
    private String descripcion;
    private long timestamp;

    private String idTanque;
    private double ph;
    private double conductividad;
    private double turbidez;
    private double nivel;

    private double monto;
    private int cuotasPagadas;

    // Firebase requiere constructor vacío
    public HistorialEvento() {}

    public HistorialEvento(String id, String tipo, String descripcion, long timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) { this.conductividad = conductividad; }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) { this.turbidez = turbidez; }

    public double getNivel() { return nivel; }
    public void setNivel(double nivel) { this.nivel = nivel; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public int getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(int cuotasPagadas) { this.cuotasPagadas = cuotasPagadas; }
}
