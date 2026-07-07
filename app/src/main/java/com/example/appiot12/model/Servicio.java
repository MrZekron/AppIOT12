package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Servicio implements Serializable {

    private String idServicio;
    private String nombre;
    private String descripcion;
    private int precioMensual;       // mensualidad en CLP
    private int precioMantenimiento; // mantención en CLP
    private boolean activo;

    public Servicio() {}

    public Servicio(String idServicio, String nombre, String descripcion,
                    int precioMensual, int precioMantenimiento) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioMensual = Math.max(0, precioMensual);
        this.precioMantenimiento = Math.max(0, precioMantenimiento);
        this.activo = true;
    }

    public String getIdServicio() { return idServicio; }
    public void setIdServicio(String idServicio) { this.idServicio = idServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(int precioMensual) { this.precioMensual = Math.max(0, precioMensual); }

    public int getPrecioMantenimiento() { return precioMantenimiento; }
    public void setPrecioMantenimiento(int precioMantenimiento) { this.precioMantenimiento = Math.max(0, precioMantenimiento); }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " | Mensual: $" + precioMensual + " | Mantención: $" + precioMantenimiento;
    }
}
