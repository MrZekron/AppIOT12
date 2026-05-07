package com.example.appiot12.model;

/**
 * Modelo de datos para representar un evento dentro del historial del usuario.
 *
 * Se usa para guardar:
 * - eventos generales
 * - registros de sensores
 * - pagos
 *
 * Firebase necesita:
 * - constructor vacío
 * - getters y setters
 */
public class HistorialEvento {

    // =========================
    // CAMPOS BASE DEL EVENTO
    // =========================
    private String id;
    private String tipo;
    private String descripcion;
    private long timestamp;

    // =========================
    // CAMPOS OPCIONALES SENSOR
    // =========================
    private String idTanque;
    private double ph;
    private double conductividad;
    private double turbidez;
    private double nivel;

    // =========================
    // CAMPOS OPCIONALES PAGO
    // =========================
    private double monto;
    private int cuotasPagadas;

    /**
     * Constructor vacío requerido por Firebase.
     */
    public HistorialEvento() {
    }

    /**
     * Constructor principal para crear un evento base.
     *
     * @param id identificador único del evento
     * @param tipo tipo del evento
     * @param descripcion descripción del evento
     * @param timestamp fecha/hora en milisegundos
     */
    public HistorialEvento(String id, String tipo, String descripcion, long timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.timestamp = timestamp;
    }

    // =========================
    // GETTERS Y SETTERS BASE
    // =========================
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // =========================
    // GETTERS Y SETTERS SENSOR
    // =========================
    public String getIdTanque() {
        return idTanque;
    }

    public void setIdTanque(String idTanque) {
        this.idTanque = idTanque;
    }

    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    public double getConductividad() {
        return conductividad;
    }

    public void setConductividad(double conductividad) {
        this.conductividad = conductividad;
    }

    public double getTurbidez() {
        return turbidez;
    }

    public void setTurbidez(double turbidez) {
        this.turbidez = turbidez;
    }

    public double getNivel() {
        return nivel;
    }

    public void setNivel(double nivel) {
        this.nivel = nivel;
    }

    // =========================
    // GETTERS Y SETTERS PAGO
    // =========================
    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public int getCuotasPagadas() {
        return cuotasPagadas;
    }

    public void setCuotasPagadas(int cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
    }
}
