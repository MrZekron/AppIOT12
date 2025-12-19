package com.example.appiot12;

public class Pago {

    private String idPago;
    private String idDispositivo;

    private int precioTotal;
    private int cuotasTotales;
    private int cuotasPagadas;
    private int saldoPendiente;

    private long fechaPago;

    // 🔥 NUEVO
    private String estado; // pendiente | aprobado | rechazado

    private boolean pagado;

    // 🔧 Constructor vacío
    public Pago() {}

    // 🎯 Constructor inicial (antes de pagar)
    public Pago(String idPago,
                int precioTotal,
                int cuotasTotales,
                long fechaPago) {

        this.idPago = idPago;
        this.precioTotal = precioTotal;
        this.cuotasTotales = cuotasTotales;
        this.fechaPago = fechaPago;

        this.cuotasPagadas = 0;
        this.saldoPendiente = precioTotal;
        this.pagado = false;
        this.estado = "pendiente";
        this.idDispositivo = null;
    }

    // =========================
    // GETTERS
    // =========================

    public String getIdPago() { return idPago; }
    public String getIdDispositivo() { return idDispositivo; }
    public int getPrecioTotal() { return precioTotal; }
    public int getCuotasTotales() { return cuotasTotales; }
    public int getCuotasPagadas() { return cuotasPagadas; }
    public int getSaldoPendiente() { return saldoPendiente; }
    public long getFechaPago() { return fechaPago; }
    public boolean isPagado() { return pagado; }
    public String getEstado() { return estado; }

    // =========================
    // SETTERS CONTROLADOS
    // =========================

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = Math.max(0, cuotasPagadas);
        recalcularEstado();
    }

    public void setSaldoPendiente(int saldoPendiente) {
        this.saldoPendiente = Math.max(0, saldoPendiente);
        recalcularEstado();
    }

    public void setEstado(String estado) {
        this.estado = estado;
        if ("aprobado".equals(estado)) {
            this.pagado = true;
            this.saldoPendiente = 0;
        }
    }

    // =========================
    // 🧠 LÓGICA CENTRAL
    // =========================

    private void recalcularEstado() {
        boolean saldoPagado = saldoPendiente <= 0;
        boolean cuotasCompletas = cuotasPagadas >= cuotasTotales;
        pagado = saldoPagado || cuotasCompletas;
        if (pagado) {
            saldoPendiente = 0;
            estado = "aprobado";
        }
    }
}
