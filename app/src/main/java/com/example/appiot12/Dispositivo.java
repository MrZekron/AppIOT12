package com.example.appiot12; // 📦 Este archivo pertenece al paquete principal

/**
 * 🌟 CLASE DISPOSITIVO 🌟
 *
 * Representa el "corazón tecnológico" del tanque 🧠💧.
 *
 * Aquí se guardan:
 * - Datos reales de sensores
 * - Estados calculados
 * - Información del PAGO del dispositivo 💰 (nuevo)
 *
 * El dispositivo ahora sabe cuánto cuesta, si está pagado, cuántas cuotas quedan, etc.
 */

public class Dispositivo {

    // 🆔 Identificador único del dispositivo
    private String id;

    // === SENSORES REALES ===
    private double ph;              // 🧪 Sensor de pH
    private double conductividad;   // ⚡ Sensor de conductividad
    private double turbidez;        // 🌫 Sensor de turbidez
    private double ultrasonico;     // 📏 Nivel de agua

    // === ESTADOS CALCULADOS ===
    private String estadoPH;
    private String estadoConductividad;
    private String estadoTurbidez;

    // === 💸 PAGO DEL DISPOSITIVO ===
    // NUEVO: cada dispositivo tiene su propio pago
    private Pago pago;

    // === CONSTRUCTOR VACÍO (Firebase lo necesita) ===
    public Dispositivo() {}

    // === CONSTRUCTOR COMPLETO ===
    public Dispositivo(String id, double ph, double conductividad, double turbidez, double ultrasonico) {
        this.id = id;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;

        // Estados iniciales
        this.estadoPH = "N/A";
        this.estadoConductividad = "N/A";
        this.estadoTurbidez = "N/A";

        // Pago aún no asignado
        this.pago = null;
    }

    // === GETTERS & SETTERS ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) { this.conductividad = conductividad; }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) { this.turbidez = turbidez; }

    public double getUltrasonico() { return ultrasonico; }
    public void setUltrasonico(double ultrasonico) { this.ultrasonico = ultrasonico; }

    // === ESTADOS ===
    public String getEstadoPH() { return estadoPH; }
    public void setEstadoPH(String estadoPH) { this.estadoPH = estadoPH; }

    public String getEstadoConductividad() { return estadoConductividad; }
    public void setEstadoConductividad(String estadoConductividad) { this.estadoConductividad = estadoConductividad; }

    public String getEstadoTurbidez() { return estadoTurbidez; }
    public void setEstadoTurbidez(String estadoTurbidez) { this.estadoTurbidez = estadoTurbidez; }

    // === 💸 PAGO (NUEVO) ===
    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }
}
