package com.example.appiot12;

/**
 * Modelo de pago de un dispositivo.
 *
 * Esta clase representa:
 * - el total a pagar
 * - las cuotas
 * - cuánto se ha pagado
 * - cuánto falta
 * - estado de pago
 * - estado de envío
 *
 * Firebase necesita:
 * - constructor vacío
 * - getters y setters
 */
public class Pago {

    // =========================
    // IDENTIFICACIÓN
    // =========================
    private String idPago;
    private String idTanque;
    private String idDispositivo;
    private String nombreProducto;

    // =========================
    // DATOS ECONÓMICOS
    // =========================
    private int precioTotal;
    private int cuotasTotales;
    private int cuotasPagadas;
    private int saldoPendiente;

    // =========================
    // ESTADOS
    // =========================
    private boolean pagado;
    private String estadoPago;     // pendiente, parcial, pagado, fallido
    private String estadoEnvio;    // preparando, enviado, entregado

    // =========================
    // DATOS MERCADO PAGO / LINK
    // =========================
    private String checkoutUrl;    // link de pago o preferencia web
    private String mpPreferenceId; // opcional
    private String mpPaymentId;    // opcional

    // =========================
    // FECHAS
    // =========================
    private long fechaCreacion;
    private long ultimaActualizacion;

    /**
     * Constructor vacío obligatorio para Firebase.
     */
    public Pago() {
    }

    /**
     * Constructor principal.
     */
    public Pago(String idPago,
                String idTanque,
                String idDispositivo,
                String nombreProducto,
                int precioTotal,
                int cuotasTotales) {

        this.idPago = idPago;
        this.idTanque = idTanque;
        this.idDispositivo = idDispositivo;
        this.nombreProducto = nombreProducto;
        this.precioTotal = Math.max(0, precioTotal);
        this.cuotasTotales = Math.max(1, cuotasTotales);
        this.cuotasPagadas = 0;
        this.saldoPendiente = Math.max(0, precioTotal);
        this.pagado = false;
        this.estadoPago = "pendiente";
        this.estadoEnvio = "preparando";
        this.checkoutUrl = "";
        this.mpPreferenceId = "";
        this.mpPaymentId = "";
        this.fechaCreacion = System.currentTimeMillis();
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    // =========================
    // GETTERS Y SETTERS
    // =========================
    public String getIdPago() {
        return idPago;
    }

    public void setIdPago(String idPago) {
        this.idPago = idPago;
    }

    public String getIdTanque() {
        return idTanque;
    }

    public void setIdTanque(String idTanque) {
        this.idTanque = idTanque;
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(int precioTotal) {
        this.precioTotal = Math.max(0, precioTotal);
        recalcularEstado();
    }

    public int getCuotasTotales() {
        return cuotasTotales;
    }

    public void setCuotasTotales(int cuotasTotales) {
        this.cuotasTotales = Math.max(1, cuotasTotales);
        recalcularEstado();
    }

    public int getCuotasPagadas() {
        return cuotasPagadas;
    }

    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = Math.max(0, cuotasPagadas);
        recalcularEstado();
    }

    public int getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(int saldoPendiente) {
        this.saldoPendiente = Math.max(0, saldoPendiente);
        recalcularEstado();
    }

    public boolean isPagado() {
        return pagado;
    }

    public void setPagado(boolean pagado) {
        this.pagado = pagado;
        recalcularEstado();
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago == null ? "pendiente" : estadoPago;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio == null ? "preparando" : estadoEnvio;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl == null ? "" : checkoutUrl;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getMpPreferenceId() {
        return mpPreferenceId;
    }

    public void setMpPreferenceId(String mpPreferenceId) {
        this.mpPreferenceId = mpPreferenceId == null ? "" : mpPreferenceId;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public String getMpPaymentId() {
        return mpPaymentId;
    }

    public void setMpPaymentId(String mpPaymentId) {
        this.mpPaymentId = mpPaymentId == null ? "" : mpPaymentId;
        this.ultimaActualizacion = System.currentTimeMillis();
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(long ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // =========================
    // LÓGICA DE NEGOCIO
    // =========================

    /**
     * Devuelve el valor estimado de una cuota.
     */
    public int getValorCuota() {
        if (cuotasTotales <= 0) {
            return precioTotal;
        }
        return (int) Math.ceil((double) precioTotal / cuotasTotales);
    }

    /**
     * Registra el pago de una cuota.
     */
    public void pagarUnaCuota() {
        if (pagado) {
            return;
        }

        if (cuotasPagadas < cuotasTotales) {
            cuotasPagadas++;
        }

        saldoPendiente = Math.max(0, saldoPendiente - getValorCuota());
        ultimaActualizacion = System.currentTimeMillis();

        recalcularEstado();
    }

    /**
     * Marca el pago completo.
     */
    public void pagarTodo() {
        cuotasPagadas = cuotasTotales;
        saldoPendiente = 0;
        pagado = true;
        ultimaActualizacion = System.currentTimeMillis();
        recalcularEstado();
    }

    /**
     * Indica si quedan cuotas por pagar.
     */
    public boolean tieneCuotasPendientes() {
        return cuotasPagadas < cuotasTotales && saldoPendiente > 0;
    }

    /**
     * Recalcula estados derivados del pago.
     */
    private void recalcularEstado() {
        if (saldoPendiente <= 0 || cuotasPagadas >= cuotasTotales || pagado) {
            pagado = true;
            saldoPendiente = 0;
            cuotasPagadas = Math.min(cuotasPagadas, cuotasTotales);
            estadoPago = "pagado";
        } else if (cuotasPagadas > 0) {
            pagado = false;
            estadoPago = "parcial";
        } else {
            pagado = false;
            estadoPago = "pendiente";
        }

        ultimaActualizacion = System.currentTimeMillis();
    }
}