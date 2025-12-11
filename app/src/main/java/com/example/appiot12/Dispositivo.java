package com.example.appiot12;
// 📦 Clase dentro del ecosistema AguaSegura: el “cerebro digital” de cada tanque 💧🤖

/**
 * 🌟 CLASE DISPOSITIVO 🌟
 *
 * Representa el módulo IoT que envía mediciones desde el tanque hacia Firebase.
 * Podríamos decir que es el "sensor multipropósito premium" del sistema 😎📡.
 *
 * REGLAS CORPORATIVAS:
 * - No maneja pagos (solo sensores, estado y asociación).
 * - Cada dispositivo puede pertenecer a *un* solo tanque.
 * - Si idTanque = null → dispositivo libre, listo para ser asignado.
 */

public class Dispositivo {

    // 🆔 Identificador único del dispositivo (UUID generado al comprarlo)
    private String id;

    // ⭐ Identificador del tanque al que pertenece este dispositivo.
    //    Si es null, significa que el dispositivo aún no está asignado.
    private String idTanque;

    // === SENSORES REALES DEL ESP32 ===
    // Estos valores llegan desde el módulo IoT: mediciones del agua en tiempo real.
    private double ph;            // 🧪 Nivel de acidez
    private double conductividad; // ⚡ Sales disueltas
    private double turbidez;      // 🌫 Claridad del agua
    private double ultrasonico;   // 📡 Nivel del tanque (distancia medida)

    // === ESTADOS CALCULADOS ===
    // Basados en rangos configurados por la OMS / normas chilenas.
    private String estadoPH;            // 👍 Normal | ⚠️ Alerta | 🔥 Peligro
    private String estadoConductividad; // Idem pero con sales
    private String estadoTurbidez;      // Idem pero con turbidez

    // =========================================================
    // CONSTRUCTOR VACÍO → NECESARIO PARA FIREBASE
    // =========================================================
    public Dispositivo() {}

    // =========================================================
    // CONSTRUCTOR COMPLETO → Inicializa un dispositivo nuevo
    // =========================================================
    public Dispositivo(String id, double ph, double conductividad, double turbidez, double ultrasonico) {

        this.id = id;                   // ID único del dispositivo
        this.ph = ph;                   // Valor inicial (placeholder)
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;

        // Estados no evaluados aún
        this.estadoPH = "N/A";
        this.estadoConductividad = "N/A";
        this.estadoTurbidez = "N/A";

        // Nuevo dispositivo → sin tanque asignado
        this.idTanque = null;
    }

    // =========================================================
    // GETTERS & SETTERS — Acceso total estilo empresa ordenada
    // =========================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public double getPh() { return ph; }
    public void setPh(double ph) { this.ph = ph; }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) { this.conductividad = conductividad; }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) { this.turbidez = turbidez; }

    public double getUltrasonico() { return ultrasonico; }
    public void setUltrasonico(double ultrasonico) { this.ultrasonico = ultrasonico; }

    public String getEstadoPH() { return estadoPH; }
    public void setEstadoPH(String estadoPH) { this.estadoPH = estadoPH; }

    public String getEstadoConductividad() { return estadoConductividad; }
    public void setEstadoConductividad(String estadoConductividad) { this.estadoConductividad = estadoConductividad; }

    public String getEstadoTurbidez() { return estadoTurbidez; }
    public void setEstadoTurbidez(String estadoTurbidez) { this.estadoTurbidez = estadoTurbidez; }
}
