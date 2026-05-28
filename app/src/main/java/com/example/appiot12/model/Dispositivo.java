package com.example.appiot12.model;

public class Dispositivo {

    private String id;
    private String idTanque;
    private boolean activo = true;      // false = dispositivo dado de baja
    private long fechaInstalacion;
    private long fechaBaja;             // 0 si aún activo

    private double ph;
    private double conductividad;
    private double turbidez;
    private double ultrasonico;

    private long timestamp;

    private String estadoPH;
    private String estadoConductividad;
    private String estadoTurbidez;

    public Dispositivo() {}

    public Dispositivo(String id, double ph, double conductividad, double turbidez, double ultrasonico) {
        this.id = id;
        this.idTanque = null;
        this.activo = true;
        this.fechaInstalacion = System.currentTimeMillis();
        this.fechaBaja = 0;
        this.ph = ph;
        this.conductividad = conductividad;
        this.turbidez = turbidez;
        this.ultrasonico = ultrasonico;
        this.timestamp = System.currentTimeMillis();
        actualizarEstados();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public long getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(long fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }

    public long getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(long fechaBaja) { this.fechaBaja = fechaBaja; }

    public void darDeBaja() {
        this.activo = false;
        this.fechaBaja = System.currentTimeMillis();
        this.idTanque = null;
    }

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

    public String getEstadoGeneral() {
        actualizarEstados();
        if (estadoPH.contains("Peligro") || estadoConductividad.contains("Peligro") || estadoTurbidez.contains("Peligro")) {
            return "Peligro 🔥";
        }
        if (estadoPH.contains("Alerta") || estadoConductividad.contains("Alerta") || estadoTurbidez.contains("Alerta")) {
            return "Alerta ⚠️";
        }
        return "Normal 👍";
    }

    public String generarResumen() {
        return "Estado del agua:\n" +
                "- pH: " + ph + " (" + estadoPH + ")\n" +
                "- Conductividad: " + conductividad + " (" + estadoConductividad + ")\n" +
                "- Turbidez: " + turbidez + " (" + estadoTurbidez + ")\n" +
                "- Nivel: " + ultrasonico + "\n" +
                "- Estado general: " + getEstadoGeneral();
    }

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

    @Override
    public String toString() {
        return id != null && !id.trim().isEmpty() ? "Dispositivo " + id : "Dispositivo sin ID";
    }
}
