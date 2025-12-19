package com.example.appiot12;
// 📦 Modelo que representa un tanque dentro del ecosistema AguaSegura.
// Contiene SOLO metadata del tanque + referencia al dispositivo asociado.

/**
 * ⭐ MODELO TANQUE DE AGUA ⭐
 *
 * Rol en la arquitectura:
 * 👉 Representar el tanque como entidad lógica
 * 👉 NO manejar sensores (eso es responsabilidad de Dispositivo)
 * 👉 Servir como nodo estable en Firebase
 *
 * Principio aplicado:
 * ✔ Single Responsibility (SRP)
 */
public class TanqueAgua {

    // ======================================================
    // 🔑 ATRIBUTOS DEL MODELO
    // ======================================================

    private String idTanque;        // 🆔 ID único (key Firebase)
    private String nombre;          // 🏷 Nombre asignado por el usuario
    private String capacidad;       // 💧 Capacidad total en litros (String validado)
    private String color;           // 🎨 Color físico del tanque
    private String direccion;       // 📍 Dirección física (OPCIONAL)
    private String idDispositivo;   // 📡 Dispositivo asociado (null = libre)

    // ======================================================
    // 🧱 CONSTRUCTOR VACÍO
    // Requerido obligatoriamente por Firebase
    // ======================================================
    public TanqueAgua() {}

    // ======================================================
    // 🏗 CONSTRUCTOR COMPLETO
    // ======================================================
    public TanqueAgua(String idTanque,
                      String nombre,
                      String capacidad,
                      String color,
                      String direccion,
                      String idDispositivo) {

        this.idTanque = idTanque;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
        this.direccion = direccion;       // ✅ puede ser null o ""
        this.idDispositivo = idDispositivo;
    }

    // ======================================================
    // 📌 GETTERS & SETTERS
    // ======================================================

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

    // =========================
    // 📍 DIRECCIÓN (OPCIONAL)
    // =========================

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    // ======================================================
    // 🧠 MÉTODOS DE UTILIDAD (NEGOCIO LIGERO)
    // ======================================================

    /**
     * ✔ Indica si el tanque tiene un dispositivo asignado
     */
    public boolean tieneDispositivo() {
        return idDispositivo != null && !idDispositivo.isEmpty();
    }

    /**
     * ✔ Devuelve la capacidad como número
     * Evita parseos repetidos en adapters/controllers
     */
    public double getCapacidadNumerica() {
        try {
            return Double.parseDouble(capacidad);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * ✔ Indica si el tanque tiene dirección registrada
     * Útil para mapas, filtros o reportes
     */
    public boolean tieneDireccion() {
        return direccion != null && !direccion.trim().isEmpty();
    }

    // ======================================================
    // 🧩 UTILIDAD DE DEPURACIÓN / UI
    // ======================================================
    @Override
    public String toString() {
        return nombre != null && !nombre.isEmpty()
                ? nombre
                : "Tanque sin nombre";
    }
}
