package com.example.appiot12.model; // 📦 Paquete del proyecto

/**
 * 📦 Modelo TanqueAgua
 *
 * Representa un tanque dentro del sistema Agua Segura 💧
 *
 * Responsabilidades:
 * - Guardar la información principal del tanque
 * - Relacionarlo con un dispositivo
 * - Manejar estados de mantención
 * - Soportar reportes mensuales por correo
 */
public class TanqueAgua {

    // ======================================================
    // 🔑 ATRIBUTOS PRINCIPALES
    // ======================================================

    private String idTanque; // 🆔 ID único del tanque
    private String nombre; // 🏷 Nombre del tanque
    private String capacidad; // 💧 Capacidad total en litros
    private String color; // 🎨 Color del tanque
    private String direccion; // 📍 Dirección física
    private String idDispositivo; // 📡 ID del dispositivo asociado

    // ======================================================
    // 🆕 CAMPOS PARA REPORTES
    // ======================================================

    private long fechaCreacion; // 🕒 Fecha de creación del tanque
    private long ultimoCorreoEnviado; // 📧 Última fecha en que se envió reporte

    // ======================================================
    // 🛠 ESTADOS DE MANTENCIÓN
    // ======================================================

    private boolean mantencionTanque; // 🔧 Indica si el tanque necesita mantención
    private boolean mantencionDispositivo; // ⚙️ Indica si el dispositivo necesita mantención

    // ======================================================
    // 🧱 CONSTRUCTOR VACÍO
    // Firebase lo necesita para mapear objetos automáticamente
    // ======================================================

    public TanqueAgua() {
        // ✅ Constructor vacío requerido por Firebase
    }

    // ======================================================
    // 🏗 CONSTRUCTOR PRINCIPAL
    // ======================================================

    public TanqueAgua(String idTanque,
                      String nombre,
                      String capacidad,
                      String color,
                      String direccion,
                      String idDispositivo) {

        this.idTanque = idTanque; // 🆔 Guarda el ID
        this.nombre = nombre; // 🏷 Guarda el nombre
        this.capacidad = capacidad; // 💧 Guarda la capacidad
        this.color = color; // 🎨 Guarda el color
        this.direccion = direccion; // 📍 Guarda la dirección
        this.idDispositivo = idDispositivo; // 📡 Guarda el dispositivo asociado

        this.fechaCreacion = System.currentTimeMillis(); // 🕒 Marca fecha de creación
        this.ultimoCorreoEnviado = 0; // 📧 Aún no se ha enviado correo

        this.mantencionTanque = false; // ✅ Por defecto no necesita mantención
        this.mantencionDispositivo = false; // ✅ Por defecto no necesita mantención
    }

    // ======================================================
    // 📌 GETTERS Y SETTERS
    // ======================================================

    public String getIdTanque() {
        return idTanque; // 🆔 Devuelve ID del tanque
    }

    public void setIdTanque(String idTanque) {
        this.idTanque = idTanque; // ✏️ Actualiza ID del tanque
    }

    public String getNombre() {
        return nombre; // 🏷 Devuelve nombre
    }

    public void setNombre(String nombre) {
        this.nombre = nombre; // ✏️ Actualiza nombre
    }

    public String getCapacidad() {
        return capacidad; // 💧 Devuelve capacidad como texto
    }

    public void setCapacidad(String capacidad) {
        this.capacidad = capacidad; // ✏️ Actualiza capacidad
    }

    public String getColor() {
        return color; // 🎨 Devuelve color
    }

    public void setColor(String color) {
        this.color = color; // ✏️ Actualiza color
    }

    public String getDireccion() {
        return direccion; // 📍 Devuelve dirección
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion; // ✏️ Actualiza dirección
    }

    public String getIdDispositivo() {
        return idDispositivo; // 📡 Devuelve ID del dispositivo
    }

    public void setIdDispositivo(String idDispositivo) {
        this.idDispositivo = idDispositivo; // ✏️ Actualiza ID del dispositivo
    }

    public long getFechaCreacion() {
        return fechaCreacion; // 🕒 Devuelve fecha de creación
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion; // ✏️ Actualiza fecha de creación
    }

    public long getUltimoCorreoEnviado() {
        return ultimoCorreoEnviado; // 📧 Devuelve última fecha de correo
    }

    public void setUltimoCorreoEnviado(long ultimoCorreoEnviado) {
        this.ultimoCorreoEnviado = ultimoCorreoEnviado; // ✏️ Actualiza último envío
    }

    public boolean isMantencionTanque() {
        return mantencionTanque; // 🔧 Devuelve estado de mantención del tanque
    }

    public void setMantencionTanque(boolean mantencionTanque) {
        this.mantencionTanque = mantencionTanque; // ✏️ Actualiza mantención del tanque
    }

    public boolean isMantencionDispositivo() {
        return mantencionDispositivo; // ⚙️ Devuelve estado de mantención del dispositivo
    }

    public void setMantencionDispositivo(boolean mantencionDispositivo) {
        this.mantencionDispositivo = mantencionDispositivo; // ✏️ Actualiza mantención del dispositivo
    }

    // ======================================================
    // 📧 LÓGICA DE REPORTES
    // ======================================================

    /**
     * 📆 Indica si ya corresponde enviar correo mensual
     */
    public boolean necesitaEnviarCorreo() {
        long ahora = System.currentTimeMillis(); // 🕒 Hora actual
        long treintaDias = 30L * 24L * 60L * 60L * 1000L; // 📅 30 días en ms

        if (ultimoCorreoEnviado == 0) { // 📧 Si nunca se ha enviado correo
            return (ahora - fechaCreacion) >= treintaDias; // ✅ Revisa desde creación
        }

        return (ahora - ultimoCorreoEnviado) >= treintaDias; // ✅ Revisa desde último envío
    }

    /**
     * 📧 Marca que ya se envió un correo
     */
    public void marcarCorreoEnviado() {
        this.ultimoCorreoEnviado = System.currentTimeMillis(); // 🕒 Guarda fecha actual
    }

    // ======================================================
    // 🧠 MÉTODOS DE UTILIDAD
    // ======================================================

    /**
     * 📡 Indica si tiene un dispositivo asociado
     */
    public boolean tieneDispositivo() {
        return idDispositivo != null && !idDispositivo.trim().isEmpty();
    }

    /**
     * 🔢 Devuelve la capacidad como número
     */
    public double getCapacidadNumerica() {
        try {
            return Double.parseDouble(capacidad);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 📍 Indica si tiene dirección válida
     */
    public boolean tieneDireccion() {
        return direccion != null && !direccion.trim().isEmpty();
    }

    /**
     * 🔧 Activa mantención del tanque
     */
    public void activarMantencionTanque() {
        this.mantencionTanque = true;
    }

    /**
     * ✅ Desactiva mantención del tanque
     */
    public void desactivarMantencionTanque() {
        this.mantencionTanque = false;
    }

    /**
     * ⚙️ Activa mantención del dispositivo
     */
    public void activarMantencionDispositivo() {
        this.mantencionDispositivo = true;
    }

    /**
     * ✅ Desactiva mantención del dispositivo
     */
    public void desactivarMantencionDispositivo() {
        this.mantencionDispositivo = false;
    }

    /**
     * 🚨 Devuelve true si el tanque o el dispositivo necesitan mantención
     */
    public boolean necesitaAlgunaMantencion() {
        return mantencionTanque || mantencionDispositivo;
    }

    /**
     * 📊 Calcula la prioridad de mantención
     *
     * Reglas:
     * - 3 = tanque y dispositivo con mantención
     * - 2 = solo tanque
     * - 1 = solo dispositivo
     * - 0 = ninguno
     */
    public int getPrioridadMantencion() {
        int prioridad = 0; // 🧮 Inicia contador

        if (mantencionTanque) {
            prioridad += 2; // ➕ Suma 2 si el tanque necesita mantención
        }

        if (mantencionDispositivo) {
            prioridad += 1; // ➕ Suma 1 si el dispositivo necesita mantención
        }

        return prioridad; // ✅ Devuelve prioridad final
    }

    /**
     * 📝 Texto corto del estado de mantención del tanque
     */
    public String getEstadoMantencionTanqueTexto() {
        return mantencionTanque ? "Pendiente" : "Al día";
    }

    /**
     * 📝 Texto corto del estado de mantención del dispositivo
     */
    public String getEstadoMantencionDispositivoTexto() {
        return mantencionDispositivo ? "Pendiente" : "Al día";
    }

    // ======================================================
    // 🧩 DEPURACIÓN / UI
    // ======================================================

    @Override
    public String toString() {
        return nombre != null && !nombre.trim().isEmpty()
                ? nombre
                : "Tanque sin nombre"; // 🧾 Texto visible por defecto
    }
}
