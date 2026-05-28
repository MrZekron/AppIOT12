package com.example.appiot12.model;

public class TanqueAgua {

    private String idTanque;
    private String nombre;
    private String capacidad;
    private String color;
    private String direccion;
    private double latitud;
    private double longitud;
    private String codigoPostal;
    private String idDispositivo;  // dispositivo activo actual

    private long fechaCreacion;
    private long ultimoCorreoEnviado;

    private boolean mantencionTanque;
    private boolean mantencionDispositivo;

    // Firebase requiere constructor vacío
    public TanqueAgua() {}

    public TanqueAgua(String idTanque, String nombre, String capacidad, String color,
                      String direccion, String idDispositivo) {
        this.idTanque = idTanque;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
        this.direccion = direccion;
        this.idDispositivo = idDispositivo;
        this.fechaCreacion = System.currentTimeMillis();
        this.ultimoCorreoEnviado = 0;
        this.mantencionTanque = false;
        this.mantencionDispositivo = false;
    }

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

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getUltimoCorreoEnviado() { return ultimoCorreoEnviado; }
    public void setUltimoCorreoEnviado(long ultimoCorreoEnviado) { this.ultimoCorreoEnviado = ultimoCorreoEnviado; }

    public boolean isMantencionTanque() { return mantencionTanque; }
    public void setMantencionTanque(boolean mantencionTanque) { this.mantencionTanque = mantencionTanque; }

    public boolean isMantencionDispositivo() { return mantencionDispositivo; }
    public void setMantencionDispositivo(boolean mantencionDispositivo) { this.mantencionDispositivo = mantencionDispositivo; }

    public boolean necesitaEnviarCorreo() {
        long ahora = System.currentTimeMillis();
        long treintaDias = 30L * 24L * 60L * 60L * 1000L;
        if (ultimoCorreoEnviado == 0) {
            return (ahora - fechaCreacion) >= treintaDias;
        }
        return (ahora - ultimoCorreoEnviado) >= treintaDias;
    }

    public void marcarCorreoEnviado() {
        this.ultimoCorreoEnviado = System.currentTimeMillis();
    }

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

    public void activarMantencionTanque() { this.mantencionTanque = true; }
    public void desactivarMantencionTanque() { this.mantencionTanque = false; }
    public void activarMantencionDispositivo() { this.mantencionDispositivo = true; }
    public void desactivarMantencionDispositivo() { this.mantencionDispositivo = false; }

    public boolean necesitaAlgunaMantencion() {
        return mantencionTanque || mantencionDispositivo;
    }

    /**
     * Prioridad: 3 = ambos, 2 = solo tanque, 1 = solo dispositivo, 0 = ninguno
     */
    public int getPrioridadMantencion() {
        int prioridad = 0;
        if (mantencionTanque) prioridad += 2;
        if (mantencionDispositivo) prioridad += 1;
        return prioridad;
    }

    public String getEstadoMantencionTanqueTexto() {
        return mantencionTanque ? "Pendiente" : "Al día";
    }

    public String getEstadoMantencionDispositivoTexto() {
        return mantencionDispositivo ? "Pendiente" : "Al día";
    }

    @Override
    public String toString() {
        return nombre != null && !nombre.trim().isEmpty() ? nombre : "Tanque sin nombre";
    }
}
