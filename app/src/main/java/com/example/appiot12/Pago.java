package com.example.appiot12;

public class Pago {

    private String idPago;
    private String idDispositivo;

    private int precioTotal;
    private int cuotasTotales;
    private int cuotasPagadas;

    private int saldoPendiente;

    private long fechaPago;   // fecha de compra (timestamp)

    private boolean pagado;   // ← ESTE FLAG FALTABA

    public Pago() {}

    // Constructor oficial
    public Pago(String idPago, int precioTotal, int cuotasTotales, long fechaPago, String idDispositivo) {
        this.idPago = idPago;
        this.precioTotal = precioTotal;
        this.cuotasTotales = cuotasTotales;
        this.fechaPago = fechaPago;
        this.cuotasPagadas = 0;
        this.saldoPendiente = precioTotal;
        this.idDispositivo = idDispositivo;
        this.pagado = false;
    }

    // ================================
    // GETTERS & SETTERS
    // ================================

    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public int getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(int precioTotal) { this.precioTotal = precioTotal; }

    public int getCuotasTotales() { return cuotasTotales; }
    public void setCuotasTotales(int cuotasTotales) { this.cuotasTotales = cuotasTotales; }

    public int getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
        actualizarEstadoPago();
    }

    public int getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(int saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
        actualizarEstadoPago();
    }

    public long getFechaPago() { return fechaPago; }
    public void setFechaPago(long fechaPago) { this.fechaPago = fechaPago; }

    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }

    // ================================
    // LÓGICA DE NEGOCIO
    // ================================
    private void actualizarEstadoPago() {
        if (saldoPendiente <= 0 || cuotasPagadas >= cuotasTotales) {
            pagado = true;
            saldoPendiente = 0;
        } else {
            pagado = false;
        }
    }
}
