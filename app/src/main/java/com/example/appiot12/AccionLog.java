package com.example.appiot12;

public class AccionLog {

    private String id;
    private String tipo;        // creado / eliminado / editado / compra
    private String descripcion; // "Se creó tanque X"
    private long timestamp;     // System.currentTimeMillis()

    public AccionLog() {}

    public AccionLog(String tipo, String descripcion) {
        this.id = java.util.UUID.randomUUID().toString();
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public long getTimestamp() { return timestamp; }
}
