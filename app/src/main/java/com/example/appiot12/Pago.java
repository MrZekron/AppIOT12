package com.example.appiot12;
// 📦 Modelo que representa un PAGO dentro del sistema AguaSegura 💧💸

/**
 * ⭐ CLASE PAGO ⭐
 *
 * Explicado para un niño 👶:
 * 👉 Es como cuando compras algo caro
 * 👉 Puedes pagar todo de una vez o en partes
 * 👉 La app recuerda cuánto debes y cuánto ya pagaste 🧠
 *
 * Esta clase se usa en:
 *   ✔ ComprarDispositivo
 *   ✔ CentroPagos
 *   ✔ HistorialCompra
 *   ✔ PagoAdapter
 */
public class Pago {

    // 🆔 Identificador único del pago
    private String idPago;

    // 🔗 Dispositivo asociado a este pago
    private String idDispositivo;

    // 💰 Precio total del dispositivo
    private int precioTotal;

    // 🔢 Total de cuotas acordadas
    private int cuotasTotales;

    // 🔢 Cuántas cuotas ya fueron pagadas
    private int cuotasPagadas;

    // 💵 Dinero que aún falta por pagar
    private int saldoPendiente;

    // 📅 Fecha de creación del pago (timestamp)
    private long fechaPago;

    // ✔️ ¿Está completamente pagado?
    private boolean pagado;

    // ============================================================
    // 🔧 CONSTRUCTOR VACÍO (OBLIGATORIO PARA FIREBASE)
    // ============================================================
    public Pago() {
        // Firebase necesita este constructor vacío para reconstruir el objeto
    }

    /**
     * 🎯 CONSTRUCTOR PRINCIPAL
     *
     * Crea un pago nuevo:
     * ✔ Sin cuotas pagadas
     * ✔ Saldo completo pendiente
     * ✔ Estado: NO pagado
     */
    public Pago(String idPago,
                int precioTotal,
                int cuotasTotales,
                long fechaPago,
                String idDispositivo) {

        this.idPago = idPago;
        this.precioTotal = precioTotal;
        this.cuotasTotales = cuotasTotales;
        this.fechaPago = fechaPago;
        this.idDispositivo = idDispositivo;

        this.cuotasPagadas = 0;
        this.saldoPendiente = precioTotal;
        this.pagado = false;
    }

    // ============================================================
    // GETTERS (LECTURA SEGURA)
    // ============================================================

    public String getIdPago() { return idPago; }

    public String getIdDispositivo() { return idDispositivo; }

    public int getPrecioTotal() { return precioTotal; }

    public int getCuotasTotales() { return cuotasTotales; }

    public int getCuotasPagadas() { return cuotasPagadas; }

    public int getSaldoPendiente() { return saldoPendiente; }

    public long getFechaPago() { return fechaPago; }

    public boolean isPagado() { return pagado; }

    // ============================================================
    // SETTERS (CON LÓGICA CONTROLADA)
    // ============================================================

    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = Math.max(0, cuotasPagadas); // 🛡️ Nunca negativo
        recalcularEstado();
    }

    public void setSaldoPendiente(int saldoPendiente) {
        this.saldoPendiente = Math.max(0, saldoPendiente); // 🛡️ Nunca negativo
        recalcularEstado();
    }

    // ============================================================
    // 🧠 LÓGICA FINANCIERA CENTRALIZADA
    // ============================================================

    /**
     * 🧮 REGLA DE ORO DEL SISTEMA FINANCIERO
     *
     * Un pago se considera COMPLETADO cuando:
     *   ✔ El saldo pendiente llega a 0
     *   ✔ O se pagaron todas las cuotas
     *
     * Nadie puede forzar manualmente el estado ❌
     * El sistema lo calcula solo 🧠
     */
    private void recalcularEstado() {

        boolean saldoPagado = saldoPendiente <= 0;
        boolean cuotasCompletas = cuotasPagadas >= cuotasTotales;

        pagado = saldoPagado || cuotasCompletas;

        // Seguridad extra: si está pagado, el saldo debe ser 0
        if (pagado) {
            saldoPendiente = 0;
        }
    }
}
