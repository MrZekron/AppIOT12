package com.example.appiot12.model;

import java.io.Serializable;

public class Servicio implements Serializable {

    private String idServicio;
    private String idTanque;
    private String idCliente;

    private int precioServicio;       // mensualidad en CLP
    private int precioMantenimiento;  // mantención en CLP
    private int cuotasTotales;
    private int cuotasPagadas;
    private int saldoPendiente;

    private String medioPago;         // "transferencia" | "efectivo" | "tarjeta"
    private String estadoServicio;    // "activo" | "suspendido" | "cancelado"

    private long fechaInicio;
    private long fechaProximoPago;
    private long ultimaActualizacion;

    public Servicio() {}

    public Servicio(String idServicio, String idTanque, String idCliente,
                    int precioServicio, int precioMantenimiento, int cuotasTotales) {
        this.idServicio = idServicio;
        this.idTanque = idTanque;
        this.idCliente = idCliente;
        this.precioServicio = Math.max(0, precioServicio);
        this.precioMantenimiento = Math.max(0, precioMantenimiento);
        this.cuotasTotales = Math.max(1, cuotasTotales);
        this.cuotasPagadas = 0;
        this.saldoPendiente = this.precioServicio * this.cuotasTotales;
        this.medioPago = "transferencia";
        this.estadoServicio = "activo";
        this.fechaInicio = System.currentTimeMillis();
        this.fechaProximoPago = this.fechaInicio + (30L * 24 * 60 * 60 * 1000);
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getIdServicio() { return idServicio; }
    public void setIdServicio(String idServicio) { this.idServicio = idServicio; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public int getPrecioServicio() { return precioServicio; }
    public void setPrecioServicio(int precioServicio) {
        this.precioServicio = Math.max(0, precioServicio);
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public int getPrecioMantenimiento() { return precioMantenimiento; }
    public void setPrecioMantenimiento(int precioMantenimiento) {
        this.precioMantenimiento = Math.max(0, precioMantenimiento);
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public int getCuotasTotales() { return cuotasTotales; }
    public void setCuotasTotales(int cuotasTotales) {
        this.cuotasTotales = Math.max(1, cuotasTotales);
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public int getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = Math.max(0, cuotasPagadas);
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public int getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(int saldoPendiente) {
        this.saldoPendiente = Math.max(0, saldoPendiente);
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago != null ? medioPago : "transferencia";
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getEstadoServicio() { return estadoServicio; }
    public void setEstadoServicio(String estadoServicio) {
        this.estadoServicio = estadoServicio != null ? estadoServicio : "activo";
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public long getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(long fechaInicio) { this.fechaInicio = fechaInicio; }

    public long getFechaProximoPago() { return fechaProximoPago; }
    public void setFechaProximoPago(long fechaProximoPago) {
        this.fechaProximoPago = fechaProximoPago;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public long getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(long ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public void registrarPago() {
        if (cuotasPagadas >= cuotasTotales) return;
        cuotasPagadas++;
        saldoPendiente = Math.max(0, saldoPendiente - precioServicio);
        fechaProximoPago += 30L * 24 * 60 * 60 * 1000;
        ultimaActualizacion = System.currentTimeMillis();
        if (cuotasPagadas >= cuotasTotales) estadoServicio = "activo";
    }

    public boolean estaAlDia() {
        return System.currentTimeMillis() <= fechaProximoPago;
    }

    public boolean estaActivo() {
        return "activo".equals(estadoServicio);
    }

    public int getValorCuota() {
        return precioServicio;
    }

    @Override
    public String toString() {
        return "Servicio[" + idTanque + "] " + estadoServicio +
               " | Cuotas: " + cuotasPagadas + "/" + cuotasTotales;
    }
}
