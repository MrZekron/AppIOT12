package com.example.appiot12;

// ⭐ MODELO DE TANQUE DE AGUA ⭐
// El tanque es solamente un contenedor lógico.
// NO guarda sensores ni pagos ni estados del agua.
// Solo guarda un ID de dispositivo asociado (si existe).

public class TanqueAgua {

    private String idTanque;      // ID único en Firebase
    private String nombre;
    private String capacidad;
    private String color;

    // ⭐ SOLO la referencia al dispositivo (UNO o NINGUNO)
    private String idDispositivo; // null = no tiene dispositivo asignado

    public TanqueAgua() {}

    public TanqueAgua(String idTanque, String nombre, String capacidad,
                      String color, String idDispositivo) {

        this.idTanque = idTanque;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
        this.idDispositivo = idDispositivo; // puede ser null
    }

    // GETTERS & SETTERS

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCapacidad() { return capacidad; }
    public void setCapacidad(String capacidad) { this.capacidad = capacidad; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    @Override
    public String toString() {
        return nombre != null ? nombre : "Tanque sin nombre";
    }
}
