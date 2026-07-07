package com.example.appiot12.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;

@IgnoreExtraProperties
public class Alerta implements Serializable {

    private String idAlerta;
    private String idDispositivo;   // FK → Dispositivo
    private String idCliente;       // FK → Cliente
    // tipo: ph_alto / ph_bajo / turbidez / conductividad / nivel_bajo / dispositivo_offline
    private String tipo;
    private double valorDetectado;
    private double umbralConfigurado;
    private long timestamp;
    private String estado;          // activa / resuelta
    private String resolucionNota;  // null si activa

    public Alerta() {}

    public Alerta(String idAlerta, String idDispositivo, String idCliente,
                  String tipo, double valorDetectado, double umbralConfigurado) {
        this.idAlerta = idAlerta;
        this.idDispositivo = idDispositivo;
        this.idCliente = idCliente;
        this.tipo = tipo;
        this.valorDetectado = valorDetectado;
        this.umbralConfigurado = umbralConfigurado;
        this.timestamp = System.currentTimeMillis();
        this.estado = "activa";
        this.resolucionNota = null;
    }

    public String getIdAlerta() { return idAlerta; }
    public void setIdAlerta(String idAlerta) { this.idAlerta = idAlerta; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getValorDetectado() { return valorDetectado; }
    public void setValorDetectado(double valorDetectado) { this.valorDetectado = valorDetectado; }

    public double getUmbralConfigurado() { return umbralConfigurado; }
    public void setUmbralConfigurado(double umbralConfigurado) { this.umbralConfigurado = umbralConfigurado; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getResolucionNota() { return resolucionNota; }
    public void setResolucionNota(String resolucionNota) { this.resolucionNota = resolucionNota; }

    public boolean isActiva() { return "activa".equals(estado); }

    public void resolver(String nota) {
        this.estado = "resuelta";
        this.resolucionNota = nota;
    }

    @Override
    public String toString() {
        return "[" + tipo + "] " + estado + " — valor: " + valorDetectado + " (umbral: " + umbralConfigurado + ")";
    }
}
