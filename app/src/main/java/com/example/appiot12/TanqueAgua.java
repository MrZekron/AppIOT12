package com.example.appiot12;

/**
 * 📦 Modelo que representa un tanque dentro del ecosistema AguaSegura.
 *
 * Responsabilidades de esta clase:
 * - Representar la entidad lógica del tanque
 * - Guardar su metadata principal
 * - Mantener referencia al dispositivo asociado
 * - Guardar estados de mantención
 * - Entregar una prioridad para ordenar listas
 *
 * Esta clase NO debe manejar sensores directamente.
 * Esa responsabilidad sigue siendo del dispositivo.
 */
public class TanqueAgua {

    // ======================================================
    // 🔑 ATRIBUTOS PRINCIPALES DEL TANQUE
    // ======================================================

    private String idTanque;              // 🆔 ID único del tanque (Firebase key)
    private String nombre;                // 🏷 Nombre del tanque
    private String capacidad;             // 💧 Capacidad total en litros
    private String color;                 // 🎨 Color físico del tanque
    private String direccion;             // 📍 Dirección física del tanque
    private String idDispositivo;         // 📡 ID del dispositivo asociado

    // ======================================================
    // 🛠 ATRIBUTOS DE MANTENCIÓN
    // ======================================================

    private boolean mantencionTanque;         // true = el tanque necesita mantención
    private boolean mantencionDispositivo;    // true = el dispositivo necesita mantención

    // ======================================================
    // 🧱 CONSTRUCTOR VACÍO
    // Requerido por Firebase para deserializar objetos
    // ======================================================
    public TanqueAgua() {
    }

    // ======================================================
    // 🏗 CONSTRUCTOR PRINCIPAL
    // Crea un tanque con valores por defecto para mantención
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
        this.direccion = direccion;
        this.idDispositivo = idDispositivo;

        // Por defecto, un tanque nuevo parte sin mantenciones pendientes
        this.mantencionTanque = false;
        this.mantencionDispositivo = false;
    }

    // ======================================================
    // 🏗 CONSTRUCTOR COMPLETO
    // Permite crear el tanque incluyendo estados de mantención
    // ======================================================
    public TanqueAgua(String idTanque,
                      String nombre,
                      String capacidad,
                      String color,
                      String direccion,
                      String idDispositivo,
                      boolean mantencionTanque,
                      boolean mantencionDispositivo) {

        this.idTanque = idTanque;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
        this.direccion = direccion;
        this.idDispositivo = idDispositivo;
        this.mantencionTanque = mantencionTanque;
        this.mantencionDispositivo = mantencionDispositivo;
    }

    // ======================================================
    // 📌 GETTERS & SETTERS PRINCIPALES
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

    /**
     * Devuelve la dirección del tanque.
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * Actualiza la dirección del tanque.
     */
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
    // 🛠 GETTERS & SETTERS DE MANTENCIÓN
    // ======================================================

    /**
     * Indica si el tanque necesita mantención.
     */
    public boolean isMantencionTanque() {
        return mantencionTanque;
    }

    /**
     * Marca si el tanque necesita mantención.
     */
    public void setMantencionTanque(boolean mantencionTanque) {
        this.mantencionTanque = mantencionTanque;
    }

    /**
     * Indica si el dispositivo necesita mantención.
     */
    public boolean isMantencionDispositivo() {
        return mantencionDispositivo;
    }

    /**
     * Marca si el dispositivo necesita mantención.
     */
    public void setMantencionDispositivo(boolean mantencionDispositivo) {
        this.mantencionDispositivo = mantencionDispositivo;
    }

    // ======================================================
    // 🧠 MÉTODOS DE NEGOCIO / UTILIDAD
    // ======================================================

    /**
     * Indica si el tanque tiene un dispositivo asociado.
     */
    public boolean tieneDispositivo() {
        return idDispositivo != null && !idDispositivo.trim().isEmpty();
    }

    /**
     * Devuelve la capacidad como número.
     * Útil para evitar parseos repetidos en adapters o controllers.
     */
    public double getCapacidadNumerica() {
        try {
            return Double.parseDouble(capacidad);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Indica si el tanque tiene dirección válida.
     */
    public boolean tieneDireccion() {
        return direccion != null && !direccion.trim().isEmpty();
    }

    /**
     * Activa el estado de mantención del tanque.
     */
    public void activarMantencionTanque() {
        this.mantencionTanque = true;
    }

    /**
     * Desactiva el estado de mantención del tanque.
     */
    public void desactivarMantencionTanque() {
        this.mantencionTanque = false;
    }

    /**
     * Activa el estado de mantención del dispositivo.
     */
    public void activarMantencionDispositivo() {
        this.mantencionDispositivo = true;
    }

    /**
     * Desactiva el estado de mantención del dispositivo.
     */
    public void desactivarMantencionDispositivo() {
        this.mantencionDispositivo = false;
    }

    /**
     * Devuelve true si el tanque o el dispositivo necesitan mantención.
     */
    public boolean necesitaAlgunaMantencion() {
        return mantencionTanque || mantencionDispositivo;
    }

    /**
     * Calcula una prioridad para ordenar la lista de tanques.
     *
     * Regla:
     * - 3 = tanque y dispositivo necesitan mantención
     * - 2 = solo tanque necesita mantención
     * - 1 = solo dispositivo necesita mantención
     * - 0 = ninguno necesita mantención
     *
     * Mientras mayor sea el valor, más arriba debería aparecer en la lista.
     */
    public int getPrioridadMantencion() {
        int prioridad = 0;

        if (mantencionTanque) {
            prioridad += 2;
        }

        if (mantencionDispositivo) {
            prioridad += 1;
        }

        return prioridad;
    }

    /**
     * Devuelve una descripción corta del estado de mantención del tanque.
     */
    public String getEstadoMantencionTanqueTexto() {
        return mantencionTanque ? "Pendiente" : "Al día";
    }

    /**
     * Devuelve una descripción corta del estado de mantención del dispositivo.
     */
    public String getEstadoMantencionDispositivoTexto() {
        return mantencionDispositivo ? "Pendiente" : "Al día";
    }

    // ======================================================
    // 🧩 UTILIDAD DE DEPURACIÓN / UI
    // ======================================================
    @Override
    public String toString() {
        return nombre != null && !nombre.trim().isEmpty()
                ? nombre
                : "Tanque sin nombre";
    }
}