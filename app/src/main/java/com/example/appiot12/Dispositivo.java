package com.example.appiot12;
// 📦 Modelo central del proyecto Agua Segura.
// Representa el “cerebro digital” que mide el agua 💧🤖

/**
 * 🌟 CLASE Dispositivo 🌟
 *
 * ¿Qué es?
 * 👉 Es el objeto que representa un dispositivo IoT (ESP32 + sensores).
 *
 * ¿Qué hace?
 * 👉 Guarda mediciones del agua
 * 👉 Indica si el agua está bien o mal
 * 👉 Se puede asociar a UN tanque
 *
 * Explicado para un niño:
 * 👉 Es como un robot que vive en el tanque y avisa cómo está el agua 🤖💧
 */
public class Dispositivo {

    // =====================================================
    // 🆔 IDENTIDAD
    // =====================================================

    // 🔑 ID único del dispositivo (UUID)
    private String id;

    // 🛢️ ID del tanque al que está conectado
    // 👉 Si es null, el dispositivo está libre
    private String idTanque;

    // =====================================================
    // 📡 SENSORES (DATOS CRUDOS)
    // =====================================================

    // 🧪 Nivel de acidez del agua
    private double ph;

    // ⚡ Conductividad (sales disueltas)
    private double conductividad;

    // 🌫️ Turbidez (qué tan clara está el agua)
    private double turbidez;

    // 📏 Nivel del agua medido con ultrasonido
    private double ultrasonico;

    // =====================================================
    // 🚦 ESTADOS CALCULADOS (LECTURA HUMANA)
    // =====================================================

    // 👍 Normal | ⚠️ Alerta | 🔥 Peligro
    private String estadoPH;
    private String estadoConductividad;
    private String estadoTurbidez;

    // =====================================================
    // 🔄 CONSTRUCTOR VACÍO (OBLIGATORIO PARA FIREBASE)
    // =====================================================
    public Dispositivo() {
        // Firebase necesita este constructor para reconstruir el objeto ☁️
    }

    // =====================================================
    // 🛠️ CONSTRUCTOR PRINCIPAL
    // =====================================================
    public Dispositivo(
            String id,
            double ph,
            double conductividad,
            double turbidez,
            double ultrasonico
    ) {

        this.id = id;                   // 🆔 ID único
        this.ph = ph;                   // 🧪 Valor inicial de pH
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;

        // 🚦 Estados iniciales (aún no evaluados)
        this.estadoPH = "N/A";
        this.estadoConductividad = "N/A";
        this.estadoTurbidez = "N/A";

        // 🛢️ Dispositivo nuevo → no pertenece a ningún tanque
        this.idTanque = null;
    }

    // =====================================================
    // 📤 GETTERS Y SETTERS
    // =====================================================

    // 🆔 ID del dispositivo
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // 🛢️ Tanque asociado
    public String getIdTanque() {
        return idTanque;
    }

    public void setIdTanque(String idTanque) {
        this.idTanque = idTanque;
    }

    // 🧪 pH
    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
        evaluarEstadoPH(); // 🔄 Cada vez que cambia, revisamos el estado
    }

    // ⚡ Conductividad
    public double getConductividad() {
        return conductividad;
    }

    public void setConductividad(double conductividad) {
        this.conductividad = conductividad;
        evaluarEstadoConductividad();
    }

    // 🌫️ Turbidez
    public double getTurbidez() {
        return turbidez;
    }

    public void setTurbidez(double turbidez) {
        this.turbidez = turbidez;
        evaluarEstadoTurbidez();
    }

    // 📏 Ultrasonido
    public double getUltrasonico() {
        return ultrasonico;
    }

    public void setUltrasonico(double ultrasonico) {
        this.ultrasonico = ultrasonico;
    }

    // 🚦 Estados visibles
    public String getEstadoPH() {
        return estadoPH;
    }

    public String getEstadoConductividad() {
        return estadoConductividad;
    }

    public String getEstadoTurbidez() {
        return estadoTurbidez;
    }

    // =====================================================
    // 🧠 LÓGICA SIMPLE DE EVALUACIÓN (SIN REDUNDANCIA)
    // =====================================================

    /**
     * 🧪 Evalúa el estado del pH
     */
    private void evaluarEstadoPH() {
        if (ph >= 6.5 && ph <= 8.5) {
            estadoPH = "Normal 👍";
        } else if ((ph >= 6.0 && ph < 6.5) || (ph > 8.5 && ph <= 9.0)) {
            estadoPH = "Alerta ⚠️";
        } else {
            estadoPH = "Peligro 🔥";
        }
    }

    /**
     * ⚡ Evalúa el estado de la conductividad
     */
    private void evaluarEstadoConductividad() {
        if (conductividad <= 1500) {
            estadoConductividad = "Normal 👍";
        } else if (conductividad <= 2500) {
            estadoConductividad = "Alerta ⚠️";
        } else {
            estadoConductividad = "Peligro 🔥";
        }
    }

    /**
     * 🌫️ Evalúa el estado de la turbidez
     */
    private void evaluarEstadoTurbidez() {
        if (turbidez <= 5) {
            estadoTurbidez = "Normal 👍";
        } else if (turbidez <= 10) {
            estadoTurbidez = "Alerta ⚠️";
        } else {
            estadoTurbidez = "Peligro 🔥";
        }
    }
}
