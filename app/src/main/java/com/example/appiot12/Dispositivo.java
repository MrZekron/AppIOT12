package com.example.appiot12;

/**
 * 📦 Modelo central del proyecto Agua Segura.
 * Representa el dispositivo IoT que mide la calidad del agua.
 *
 * Responsabilidades de esta clase:
 * - Guardar datos crudos de sensores
 * - Evaluar estados legibles para el usuario
 * - Asociarse a un tanque
 *
 * Esta clase NO debe encargarse del orden de la lista de tanques.
 * Esa lógica pertenece al modelo TanqueAgua o a la capa que gestiona listas.
 */
public class Dispositivo {

    // =====================================================
    // 🆔 IDENTIDAD DEL DISPOSITIVO
    // =====================================================

    private String id;          // ID único del dispositivo
    private String idTanque;    // ID del tanque asociado (null o vacío = libre)

    // =====================================================
    // 📡 LECTURAS DE SENSORES
    // =====================================================

    private double ph;              // Acidez del agua
    private double conductividad;   // Sales disueltas / conductividad
    private double turbidez;        // Claridad del agua
    private double ultrasonico;     // Nivel medido con sensor ultrasónico

    // =====================================================
    // 🚦 ESTADOS CALCULADOS
    // =====================================================

    private String estadoPH;
    private String estadoConductividad;
    private String estadoTurbidez;

    // =====================================================
    // 🔄 CONSTRUCTOR VACÍO
    // Obligatorio para Firebase
    // =====================================================
    public Dispositivo() {
        // Constructor requerido por Firebase
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
        this.id = id;
        this.idTanque = null;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;

        // Evaluamos estados de inmediato para que el objeto
        // no quede con "N/A" si ya tiene datos reales.
        actualizarEstados();
    }

    // =====================================================
    // 📤 GETTERS Y SETTERS
    // =====================================================

    /**
     * Devuelve el ID único del dispositivo.
     */
    public String getId() {
        return id;
    }

    /**
     * Actualiza el ID del dispositivo.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Devuelve el ID del tanque asociado.
     */
    public String getIdTanque() {
        return idTanque;
    }

    /**
     * Asocia el dispositivo a un tanque mediante su ID.
     */
    public void setIdTanque(String idTanque) {
        this.idTanque = idTanque;
    }

    /**
     * Devuelve el valor actual de pH.
     */
    public double getPh() {
        return ph;
    }

    /**
     * Actualiza el pH y recalcula su estado.
     */
    public void setPh(double ph) {
        this.ph = ph;
        evaluarEstadoPH();
    }

    /**
     * Devuelve la conductividad actual.
     */
    public double getConductividad() {
        return conductividad;
    }

    /**
     * Actualiza la conductividad y recalcula su estado.
     */
    public void setConductividad(double conductividad) {
        this.conductividad = conductividad;
        evaluarEstadoConductividad();
    }

    /**
     * Devuelve la turbidez actual.
     */
    public double getTurbidez() {
        return turbidez;
    }

    /**
     * Actualiza la turbidez y recalcula su estado.
     */
    public void setTurbidez(double turbidez) {
        this.turbidez = turbidez;
        evaluarEstadoTurbidez();
    }

    /**
     * Devuelve la lectura del sensor ultrasónico.
     */
    public double getUltrasonico() {
        return ultrasonico;
    }

    /**
     * Actualiza la lectura ultrasónica.
     */
    public void setUltrasonico(double ultrasonico) {
        this.ultrasonico = ultrasonico;
    }

    /**
     * Devuelve el estado evaluado del pH.
     */
    public String getEstadoPH() {
        return estadoPH;
    }

    /**
     * Devuelve el estado evaluado de la conductividad.
     */
    public String getEstadoConductividad() {
        return estadoConductividad;
    }

    /**
     * Devuelve el estado evaluado de la turbidez.
     */
    public String getEstadoTurbidez() {
        return estadoTurbidez;
    }

    // =====================================================
    // 🧠 LÓGICA DE NEGOCIO
    // =====================================================

    /**
     * Recalcula todos los estados de sensores.
     * Útil cuando el dispositivo se crea con datos ya cargados.
     */
    public void actualizarEstados() {
        evaluarEstadoPH();
        evaluarEstadoConductividad();
        evaluarEstadoTurbidez();
    }

    /**
     * Indica si el dispositivo está asociado a un tanque.
     */
    public boolean estaAsociadoATanque() {
        return idTanque != null && !idTanque.trim().isEmpty();
    }

    /**
     * Desasocia el dispositivo del tanque actual.
     */
    public void desasociarTanque() {
        this.idTanque = null;
    }

    /**
     * Evalúa el estado del pH.
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
     * Evalúa el estado de la conductividad.
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
     * Evalúa el estado de la turbidez.
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

    // =====================================================
    // 🧩 UTILIDAD PARA UI Y DEPURACIÓN
    // =====================================================

    @Override
    public String toString() {
        return id != null && !id.trim().isEmpty()
                ? "Dispositivo " + id
                : "Dispositivo sin ID";
    }
}