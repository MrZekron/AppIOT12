package com.example.appiot12.model; // 📦 Paquete del proyecto

/**
 * 📡 Modelo Dispositivo
 *
 * Representa el dispositivo IoT que mide la calidad del agua 💧
 */
public class Dispositivo {

    // =====================================================
    // 🆔 IDENTIDAD
    // =====================================================

    private String id; // 🆔 ID único
    private String idTanque; // 📡 ID del tanque

    // =====================================================
    // 📡 SENSORES
    // =====================================================

    private double ph; // ⚗️ pH
    private double conductividad; // ⚡ conductividad
    private double turbidez; // 🌫 turbidez
    private double ultrasonico; // 📏 nivel

    // =====================================================
    // 🆕 TIMESTAMP
    // =====================================================

    private long timestamp; // 🕒 momento de la medición

    // =====================================================
    // 🚦 ESTADOS
    // =====================================================

    private String estadoPH;
    private String estadoConductividad;
    private String estadoTurbidez;

    // =====================================================
    // 🔄 CONSTRUCTOR VACÍO (Firebase)
    // =====================================================

    public Dispositivo() {
    }

    // =====================================================
    // 🏗 CONSTRUCTOR PRINCIPAL
    // =====================================================

    public Dispositivo(
            String id,
            double ph,
            double conductividad,
            double turbidez,
            double ultrasonico
    ) {
        this.id = id;
        this.idTanque = null;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;

        this.timestamp = System.currentTimeMillis(); // 🕒 guardar momento

        actualizarEstados(); // 🔄 calcular estados
    }

    // =====================================================
    // 📤 GETTERS & SETTERS
    // =====================================================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public double getPh() { return ph; }
    public void setPh(double ph) {
        this.ph = ph;
        evaluarEstadoPH();
    }

    public double getConductividad() { return conductividad; }
    public void setConductividad(double conductividad) {
        this.conductividad = conductividad;
        evaluarEstadoConductividad();
    }

    public double getTurbidez() { return turbidez; }
    public void setTurbidez(double turbidez) {
        this.turbidez = turbidez;
        evaluarEstadoTurbidez();
    }

    public double getUltrasonico() { return ultrasonico; }
    public void setUltrasonico(double ultrasonico) { this.ultrasonico = ultrasonico; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEstadoPH() { return estadoPH; }
    public String getEstadoConductividad() { return estadoConductividad; }
    public String getEstadoTurbidez() { return estadoTurbidez; }

    // =====================================================
    // 🧠 LÓGICA
    // =====================================================

    public void actualizarEstados() {
        evaluarEstadoPH();
        evaluarEstadoConductividad();
        evaluarEstadoTurbidez();
    }

    public boolean estaAsociadoATanque() {
        return idTanque != null && !idTanque.trim().isEmpty();
    }

    public void desasociarTanque() {
        this.idTanque = null;
    }

    // =====================================================
    // 🚦 EVALUACIONES
    // =====================================================

    private void evaluarEstadoPH() {
        if (ph >= 6.5 && ph <= 8.5) estadoPH = "Normal 👍";
        else if ((ph >= 6.0 && ph < 6.5) || (ph > 8.5 && ph <= 9.0)) estadoPH = "Alerta ⚠️";
        else estadoPH = "Peligro 🔥";
    }

    private void evaluarEstadoConductividad() {
        if (conductividad <= 1500) estadoConductividad = "Normal 👍";
        else if (conductividad <= 2500) estadoConductividad = "Alerta ⚠️";
        else estadoConductividad = "Peligro 🔥";
    }

    private void evaluarEstadoTurbidez() {
        if (turbidez <= 5) estadoTurbidez = "Normal 👍";
        else if (turbidez <= 10) estadoTurbidez = "Alerta ⚠️";
        else estadoTurbidez = "Peligro 🔥";
    }

    // =====================================================
    // 🆕 ESTADO GENERAL
    // =====================================================

    public String getEstadoGeneral() {

        if (estadoPH.contains("Peligro") ||
                estadoConductividad.contains("Peligro") ||
                estadoTurbidez.contains("Peligro")) {

            return "Peligro 🔥";
        }

        if (estadoPH.contains("Alerta") ||
                estadoConductividad.contains("Alerta") ||
                estadoTurbidez.contains("Alerta")) {

            return "Alerta ⚠️";
        }

        return "Normal 👍";
    }

    // =====================================================
    // 🧾 RESUMEN (para correo)
    // =====================================================

    public String generarResumen() {
        return "📊 Estado del agua:\n" +
                "- pH: " + ph + " (" + estadoPH + ")\n" +
                "- Conductividad: " + conductividad + " (" + estadoConductividad + ")\n" +
                "- Turbidez: " + turbidez + " (" + estadoTurbidez + ")\n" +
                "- Nivel: " + ultrasonico + "\n" +
                "- Estado general: " + getEstadoGeneral();
    }

    // =====================================================
    // 🧩 DEBUG
    // =====================================================

    @Override
    public String toString() {
        return id != null && !id.trim().isEmpty()
                ? "Dispositivo " + id
                : "Dispositivo sin ID";
    }
}
