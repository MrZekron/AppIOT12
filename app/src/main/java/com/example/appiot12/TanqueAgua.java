package com.example.appiot12;

// ⭐ MODELO DE TANQUE DE AGUA ⭐
// El tanque describe el contenedor:
// - idTanque
// - nombre
// - capacidad
// - color
// - dispositivo asociado (que contiene los sensores y estados)
//
// Ya NO contiene: estadoPH, estadoConductividad, estadoTurbidez.
// Estos pertenecen al dispositivo.

public class TanqueAgua {

    // === CAMPOS PRINCIPALES DEL TANQUE ===
    private String idTanque;     // 🆔 ID único del tanque en Firebase
    private String nombre;       // 🏷 Nombre del tanque
    private String capacidad;    // 💧 Capacidad máxima (litros)
    private String color;        // 🎨 Color del tanque

    // === DISPOSITIVO ASOCIADO ===
    private Dispositivo dispositivo; // 🔌 Sensores asociados al tanque

    // 🔧 Constructor vacío requerido por Firebase
    public TanqueAgua() {}

    // 🔧 Constructor completo (sin estados)
    public TanqueAgua(String idTanque, String nombre, String capacidad, String color,
                      Dispositivo dispositivo) {

        this.idTanque = idTanque;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
        this.dispositivo = dispositivo;
    }

    // === GETTERS & SETTERS ===

    public String getIdTanque() {
        return idTanque;
    }

    public void setIdTanque(String idTanque) {
        this.idTanque = idTanque;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(String capacidad) {
        this.capacidad = capacidad;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Dispositivo getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(Dispositivo dispositivo) {
        this.dispositivo = dispositivo;
    }

    // Para mostrar en listas
    @Override
    public String toString() {
        return nombre != null ? nombre : "Tanque sin nombre";
    }
}
