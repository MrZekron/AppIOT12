package com.example.appiot12;

/**
 * 📜 Evento del historial del sistema
 */
public class HistorialEvento {

    public String id;
    public String tipo;        // SENSOR, TANQUE, COMPRA, PAGO
    public String descripcion;
    public long fecha;         // 🔥 ESTA ES LA CLAVE

    // Datos sensores (opcionales)
    public double ph;
    public double conductividad;
    public double turbidez;
    public double nivel;

    public String idTanque;

    // Constructor vacío requerido por Firebase
    public HistorialEvento() {}

    public HistorialEvento(String id, String tipo, String descripcion, long fecha) {
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }
}
