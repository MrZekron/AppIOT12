package com.example.appiot12;

import java.io.Serializable;
import java.util.UUID;

public class Pago implements Serializable {

    private String idPago;           // ID único del pago
    private int montoTotal;          // Precio completo del dispositivo
    private int cuotasTotales;       // Número total de cuotas
    private int cuotasPagadas;       // Cuántas cuotas se han pagado
    private int saldoPendiente;      // Cuánto falta por pagar
    private boolean pagado;          // True si ya pagó todo
    private long fechaCompra;        // Timestamp de compra

    public Pago() {
        // Firebase lo necesita
    }

    // Constructor para un dispositivo comprado con cuotas
    public Pago(int montoTotal, int cuotasTotales) {
        this.idPago = UUID.randomUUID().toString();
        this.montoTotal = montoTotal;
        this.cuotasTotales = cuotasTotales;
        this.cuotasPagadas = 0;
        this.fechaCompra = System.currentTimeMillis();
        this.saldoPendiente = montoTotal;
        this.pagado = false;
    }

    // Método para registrar el pago de una cuota
    public void pagarCuota(int montoCuota) {
        if (pagado) return;

        cuotasPagadas++;
        saldoPendiente -= montoCuota;

        if (saldoPendiente <= 0) {
            saldoPendiente = 0;
            pagado = true;
        }
    }

    // GETTERS Y SETTERS

    public String getIdPago() { return idPago; }

    public int getMontoTotal() { return montoTotal; }

    public int getCuotasTotales() { return cuotasTotales; }

    public int getCuotasPagadas() { return cuotasPagadas; }

    public int getSaldoPendiente() { return saldoPendiente; }

    public boolean isPagado() { return pagado; }

    public long getFechaCompra() { return fechaCompra; }

    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
    }

    public void setSaldoPendiente(int saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
    }
}
