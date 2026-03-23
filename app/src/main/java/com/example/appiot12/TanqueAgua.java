package com.example.appiot12; // 📦 Paquete del proyecto

/**
 * 📦 Modelo TanqueAgua
 *
 * Representa un tanque dentro del sistema Agua Segura 💧
 */
public class TanqueAgua {

    // ======================================================
    // 🔑 ATRIBUTOS PRINCIPALES
    // ======================================================

    private String idTanque; // 🆔 ID único
    private String nombre; // 🏷 Nombre
    private String capacidad; // 💧 Capacidad
    private String color; // 🎨 Color
    private String direccion; // 📍 Dirección
    private String idDispositivo; // 📡 Dispositivo asociado

    // ======================================================
    // 🆕 CAMPOS PARA REPORTES
    // ======================================================

    private long fechaCreacion; // 🕒 Cuándo se creó el tanque
    private long ultimoCorreoEnviado; // 📧 Último envío de reporte

    // ======================================================
    // 🛠 MANTENCIÓN
    // ======================================================

    private boolean mantencionTanque; // 🔧 Estado tanque
    private boolean mantencionDispositivo; // ⚙️ Estado dispositivo

    // ======================================================
    // 🧱 CONSTRUCTOR VACÍO (Firebase)
    // ======================================================

    public TanqueAgua() {
        // ⚠️ Firebase necesita esto
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

        this.idTanque = idTanque;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
        this.direccion = direccion;
        this.idDispositivo = idDispositivo;

        // 🆕 Inicialización automática
        this.fechaCreacion = System.currentTimeMillis(); // 🕒 ahora
        this.ultimoCorreoEnviado = 0; // 📧 aún no enviado

        this.mantencionTanque = false;
        this.mantencionDispositivo = false;
    }

    // ======================================================
    // 📌 GETTERS & SETTERS
    // ======================================================

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCapacidad() { return capacidad; }
    public void setCapacidad(String capacidad) { this.capacidad = capacidad; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    // =========================
    // 🆕 REPORTES
    // =========================

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getUltimoCorreoEnviado() { return ultimoCorreoEnviado; }
    public void setUltimoCorreoEnviado(long ultimoCorreoEnviado) { this.ultimoCorreoEnviado = ultimoCorreoEnviado; }

    /**
     * 📆 Verifica si corresponde enviar correo
     */
    public boolean necesitaEnviarCorreo() {

        long ahora = System.currentTimeMillis();
        long DIAS_30 = 30L * 24 * 60 * 60 * 1000;

        if (ultimoCorreoEnviado == 0) {
            return (ahora - fechaCreacion) >= DIAS_30;
        }

        return (ahora - ultimoCorreoEnviado) >= DIAS_30;
    }

    /**
     * 📧 Marca que se envió el correo
     */
    public void marcarCorreoEnviado() {
        this.ultimoCorreoEnviado = System.currentTimeMillis();
    }

    // ======================================================
    // 🛠 MANTENCIÓN
    // ======================================================

    public boolean isMantencionTanque() { return mantencionTanque; }
    public void setMantencionTanque(boolean mantencionTanque) { this.mantencionTanque = mantencionTanque; }

    public boolean isMantencionDispositivo() { return mantencionDispositivo; }
    public void setMantencionDispositivo(boolean mantencionDispositivo) { this.mantencionDispositivo = mantencionDispositivo; }

    // ======================================================
    // 🧠 UTILIDAD
    // ======================================================

    public boolean tieneDispositivo() {
        return idDispositivo != null && !idDispositivo.trim().isEmpty();
    }

    public double getCapacidadNumerica() {
        try {
            return Double.parseDouble(capacidad);
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean tieneDireccion() {
        return direccion != null && !direccion.trim().isEmpty();
    }

    public boolean necesitaAlgunaMantencion() {
        return mantencionTanque || mantencionDispositivo;
    }

    // ======================================================
    // 🧩 DEBUG
    // ======================================================

    @Override
    public String toString() {
        return nombre != null && !nombre.trim().isEmpty()
                ? nombre
                : "Tanque sin nombre";
    }
}