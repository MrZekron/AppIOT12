package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Pago implements Serializable {

    private String idPago;
    private String idCliente;       // FK → Cliente
    private String idDispositivo;   // FK → Dispositivo
    private String idServicio;      // FK → Servicio
    private String tipoPago;        // mensualidad / mantenimiento
    private int monto;              // CLP
    private long fechaPago;
    private String estado;          // aprobado / rechazado / pendiente
    private String numeroOperacion; // ID transacción Mercado Pago
    private String medioPago;       // tarjeta_debito / tarjeta_credito / transferencia

    // Campos adicionales de la app (UI / flujo de compra)
    private String idTanque;
    private String nombreProducto;
    private int precioTotal;
    private int cuotasTotales;
    private int cuotasPagadas;
    private int saldoPendiente;
    private boolean pagado;
    private String estadoPago;
    private String estadoEnvio;
    private String checkoutUrl;
    private String mpPreferenceId;
    private long fechaCreacion;
    private long ultimaActualizacion;

    public Pago() {}

    public Pago(String idPago, String idCliente, String idDispositivo, String idServicio,
                String tipoPago, int monto) {
        this.idPago = idPago;
        this.idCliente = idCliente;
        this.idDispositivo = idDispositivo;
        this.idServicio = idServicio;
        this.tipoPago = tipoPago;
        this.monto = Math.max(0, monto);
        this.fechaPago = System.currentTimeMillis();
        this.estado = "pendiente";
        this.precioTotal = this.monto;
        this.cuotasTotales = 1;
        this.cuotasPagadas = 0;
        this.saldoPendiente = this.monto;
        this.pagado = false;
        this.estadoPago = "pendiente";
        this.estadoEnvio = "preparando";
        this.checkoutUrl = "";
        this.mpPreferenceId = "";
        this.numeroOperacion = "";
        this.fechaCreacion = System.currentTimeMillis();
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    // Spec fields
    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public String getIdServicio() { return idServicio; }
    public void setIdServicio(String idServicio) { this.idServicio = idServicio; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public int getMonto() { return monto; }
    public void setMonto(int monto) { this.monto = Math.max(0, monto); }

    public long getFechaPago() { return fechaPago; }
    public void setFechaPago(long fechaPago) { this.fechaPago = fechaPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) {
        this.numeroOperacion = numeroOperacion != null ? numeroOperacion : "";
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }

    // App fields (flujo de compra / compatibilidad PagoAdapter)
    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(int precioTotal) { this.precioTotal = Math.max(0, precioTotal); recalcularEstado(); }

    public int getCuotasTotales() { return cuotasTotales; }
    public void setCuotasTotales(int cuotasTotales) { this.cuotasTotales = Math.max(1, cuotasTotales); recalcularEstado(); }

    public int getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(int cuotasPagadas) { this.cuotasPagadas = Math.max(0, cuotasPagadas); recalcularEstado(); }

    public int getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(int saldoPendiente) { this.saldoPendiente = Math.max(0, saldoPendiente); recalcularEstado(); }

    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; recalcularEstado(); }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago == null ? "pendiente" : estadoPago;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getEstadoEnvio() { return estadoEnvio; }
    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio == null ? "preparando" : estadoEnvio;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl == null ? "" : checkoutUrl;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getMpPreferenceId() { return mpPreferenceId; }
    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId == null ? "" : mpPreferenceId;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(long ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    public int getValorCuota() {
        if (cuotasTotales <= 0) return precioTotal;
        return (int) Math.ceil((double) precioTotal / cuotasTotales);
    }

    public void pagarUnaCuota() {
        if (pagado) return;
        if (cuotasPagadas < cuotasTotales) cuotasPagadas++;
        saldoPendiente = Math.max(0, saldoPendiente - getValorCuota());
        ultimaActualizacion = System.currentTimeMillis();
        recalcularEstado();
    }

    public void pagarTodo() {
        cuotasPagadas = cuotasTotales;
        saldoPendiente = 0;
        pagado = true;
        ultimaActualizacion = System.currentTimeMillis();
        recalcularEstado();
    }

    public boolean tieneCuotasPendientes() {
        return cuotasPagadas < cuotasTotales && saldoPendiente > 0;
    }

    private void recalcularEstado() {
        if (saldoPendiente <= 0 || cuotasPagadas >= cuotasTotales || pagado) {
            pagado = true;
            saldoPendiente = 0;
            cuotasPagadas = Math.min(cuotasPagadas, cuotasTotales);
            estadoPago = "aprobado";
            estado = "aprobado";
        } else if (cuotasPagadas > 0) {
            pagado = false;
            estadoPago = "parcial";
        } else {
            pagado = false;
            estadoPago = "pendiente";
            estado = "pendiente";
        }
        ultimaActualizacion = System.currentTimeMillis();
    }
}
