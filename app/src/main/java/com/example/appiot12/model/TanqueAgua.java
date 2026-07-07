package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class TanqueAgua implements Serializable {

    private String idTanque;
    private String idCliente;        // FK → Cliente
    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private String codigoPostal;
    private int capacidadLitros;
    private long fechaInstalacion;
    private boolean activo;

    private String idDispositivo;    // dispositivo activo actual (referencia rápida)
    private String color;            // presentación visual en app
    private long ultimoCorreoEnviado;
    private boolean mantencionTanque;
    private boolean mantencionDispositivo;

    public TanqueAgua() {}

    public TanqueAgua(String idTanque, String idCliente, String nombre,
                      int capacidadLitros, String direccion, String idDispositivo) {
        this.idTanque = idTanque;
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.capacidadLitros = capacidadLitros;
        this.direccion = direccion;
        this.idDispositivo = idDispositivo;
        this.fechaInstalacion = System.currentTimeMillis();
        this.activo = true;
        this.ultimoCorreoEnviado = 0;
        this.mantencionTanque = false;
        this.mantencionDispositivo = false;
    }

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public int getCapacidadLitros() { return capacidadLitros; }
    public void setCapacidadLitros(int capacidadLitros) { this.capacidadLitros = capacidadLitros; }

    public long getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(long fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getUltimoCorreoEnviado() { return ultimoCorreoEnviado; }
    public void setUltimoCorreoEnviado(long ultimoCorreoEnviado) { this.ultimoCorreoEnviado = ultimoCorreoEnviado; }

    public boolean isMantencionTanque() { return mantencionTanque; }
    public void setMantencionTanque(boolean mantencionTanque) { this.mantencionTanque = mantencionTanque; }

    public boolean isMantencionDispositivo() { return mantencionDispositivo; }
    public void setMantencionDispositivo(boolean mantencionDispositivo) { this.mantencionDispositivo = mantencionDispositivo; }

    public boolean necesitaEnviarCorreo() {
        long ahora = System.currentTimeMillis();
        long treintaDias = 30L * 24L * 60L * 60L * 1000L;
        if (ultimoCorreoEnviado == 0) return (ahora - fechaInstalacion) >= treintaDias;
        return (ahora - ultimoCorreoEnviado) >= treintaDias;
    }

    public void marcarCorreoEnviado() { this.ultimoCorreoEnviado = System.currentTimeMillis(); }

    public boolean tieneDispositivo() {
        return idDispositivo != null && !idDispositivo.trim().isEmpty();
    }

    public boolean tieneDireccion() {
        return direccion != null && !direccion.trim().isEmpty();
    }

    public void activarMantencionTanque() { this.mantencionTanque = true; }
    public void desactivarMantencionTanque() { this.mantencionTanque = false; }
    public void activarMantencionDispositivo() { this.mantencionDispositivo = true; }
    public void desactivarMantencionDispositivo() { this.mantencionDispositivo = false; }

    public boolean necesitaAlgunaMantencion() { return mantencionTanque || mantencionDispositivo; }

    public int getPrioridadMantencion() {
        int prioridad = 0;
        if (mantencionTanque) prioridad += 2;
        if (mantencionDispositivo) prioridad += 1;
        return prioridad;
    }

    public String getEstadoMantencionTanqueTexto() { return mantencionTanque ? "Pendiente" : "Al día"; }
    public String getEstadoMantencionDispositivoTexto() { return mantencionDispositivo ? "Pendiente" : "Al día"; }

    @Override
    public String toString() {
        return nombre != null && !nombre.trim().isEmpty() ? nombre : "Tanque sin nombre";
    }
}
