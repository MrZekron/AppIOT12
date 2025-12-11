package com.example.appiot12;
// 📦 Clase de modelo para representar un PAGO asociado a la compra de un dispositivo.
// Actúa como "motor financiero" dentro del sistema AguaSegura 💸⚙️

/**
 * ⭐ CLASE PAGO ⭐
 *
 * Representa:
 *   - El total del dispositivo
 *   - La cantidad de cuotas pactadas
 *   - Cuántas cuotas ya fueron pagadas
 *   - El saldo pendiente
 *   - La fecha en que se realizó la compra
 *   - El dispositivo al cual pertenece el pago
 *   - Estado final: pagado / no pagado
 *
 * Esta clase alimenta:
 *   ✔ CentroPagos
 *   ✔ PagoAdapter
 *   ✔ Historial de compras
 *   ✔ Dashboard financiero del usuario
 */
public class Pago {

    // 🆔 Identificador único del pago
    private String idPago;

    // 🆔 Relación directa con un dispositivo comprado
    private String idDispositivo;

    // 💰 Monto total del dispositivo comprado
    private int precioTotal;

    // 🔢 Número total de cuotas acordadas
    private int cuotasTotales;

    // 🔢 Cuántas cuotas ya han sido pagadas
    private int cuotasPagadas;

    // 💵 Saldo actual pendiente de pago
    private int saldoPendiente;

    // 📅 Momento de compra en milisegundos (timestamp)
    private long fechaPago;

    // ✔️ Estado del pago (true = pagado completamente)
    private boolean pagado;

    // 🔧 Constructor vacío requerido por Firebase
    public Pago() {}

    /**
     * 🎯 Constructor oficial completo
     *
     * Crea un pago nuevo con:
     *   - saldo total igual al precio
     *   - 0 cuotas pagadas
     *   - estado "no pagado"
     */
    public Pago(String idPago, int precioTotal, int cuotasTotales, long fechaPago, String idDispositivo) {
        this.idPago = idPago;
        this.precioTotal = precioTotal;
        this.cuotasTotales = cuotasTotales;
        this.fechaPago = fechaPago;
        this.idDispositivo = idDispositivo;

        this.cuotasPagadas = 0;         // Recién creado → ningún pago realizado
        this.saldoPendiente = precioTotal; // Pendiente = total
        this.pagado = false;               // Aún no está pagado
    }

    // ============================================================
    // GETTERS & SETTERS (con lógica automática opcional)
    // ============================================================

    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public int getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(int precioTotal) {
        this.precioTotal = precioTotal;
        actualizarEstadoPago();
    }

    public int getCuotasTotales() { return cuotasTotales; }
    public void setCuotasTotales(int cuotasTotales) {
        this.cuotasTotales = cuotasTotales;
        actualizarEstadoPago();
    }

    public int getCuotasPagadas() { return cuotasPagadas; }
    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
        actualizarEstadoPago(); // 🧠 Si llega al total → pagado = true
    }

    public int getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(int saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
        actualizarEstadoPago(); // 🧮 Recalcular estado automático
    }

    public long getFechaPago() { return fechaPago; }
    public void setFechaPago(long fechaPago) { this.fechaPago = fechaPago; }

    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }

    // ============================================================
    // 🧠 LÓGICA DE NEGOCIO FINANCIERA
    // ============================================================

    /**
     * Regla de oro del módulo financiero:
     * Un pago se considera COMPLETADO cuando ocurre:
     *   ✔ saldoPendiente <= 0  → pagado
     *   ✔ cuotasPagadas >= cuotasTotales → pagado
     *
     * Si ninguna se cumple → sigue activo.
     */
    private void actualizarEstadoPago() {

        // Caso 1 → Se pagó todo el saldo
        if (saldoPendiente <= 0) {
            pagado = true;
            saldoPendiente = 0; // Seguridad contable
            return;
        }

        // Caso 2 → Se pagaron todas las cuotas pactadas
        if (cuotasPagadas >= cuotasTotales) {
            pagado = true;
            saldoPendiente = 0;
            return;
        }

        // Caso contrario → el pago sigue activo
        pagado = false;
    }
}
