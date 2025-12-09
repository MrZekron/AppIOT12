package com.example.appiot12;

import java.io.Serializable;
import java.util.UUID;

public class Pago implements Serializable {

    private String idPago;            // ID único del pago
    private String idDispositivo;     // ID del dispositivo asociado

    private int montoTotal;           // Precio total
    private int cuotasTotales;        // Número total de cuotas
    private int cuotasPagadas;        // Cuántas cuotas se han pagado
    private int saldoPendiente;       // Cuánto falta por pagar
    private boolean pagado;           // True si ya se completó el pago
    private long fechaCompra;         // Timestamp

    // CONSTRUCTOR VACÍO requerido por Firebase
    public Pago() {}

    // CONSTRUCTOR QUE USA LA APP
    public Pago(int montoTotal, int cuotasTotales, String idDispositivo) {
        this.idPago = UUID.randomUUID().toString();
        this.idDispositivo = idDispositivo;
        this.montoTotal = montoTotal;
        this.cuotasTotales = cuotasTotales;
        this.cuotasPagadas = 0;
        this.saldoPendiente = montoTotal;
        this.pagado = false;
        this.fechaCompra = System.currentTimeMillis();
    }

    // MÉTODO PARA PAGAR UNA CUOTA
    public void pagarCuota(int montoCuota) {
        if (pagado) return;

        cuotasPagadas++;
        saldoPendiente -= montoCuota;

        if (saldoPendiente <= 0) {
            saldoPendiente = 0;
            pagado = true;
        }
    }

    // GETTERS & SETTERS
    public String getIdPago() { return idPago; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public int getMontoTotal() { return montoTotal; }
    public int getCuotasTotales() { return cuotasTotales; }
    public int getCuotasPagadas() { return cuotasPagadas; }
    public int getSaldoPendiente() { return saldoPendiente; }
    public boolean isPagado() { return pagado; }
    public long getFechaCompra() { return fechaCompra; }

    public void setCuotasPagadas(int cuotasPagadas) { this.cuotasPagadas = cuotasPagadas; }
    public void setSaldoPendiente(int saldoPendiente) { this.saldoPendiente = saldoPendiente; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }
}
