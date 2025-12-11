package com.example.appiot12;
// Paquete oficial donde vive este modelo. Mantiene orden corporativo 📁🏢

public class AccionLog {
    // Clase que representa un registro en el historial de acciones del sistema.
    // Cada acción es como una minuta ejecutiva: quién hizo qué y cuándo 📝✨

    private String id;
    // Identificador único del log. KPI: unicidad absoluta gracias a UUID 🔑

    private String tipo;
    // Tipo de acción realizada: creado / eliminado / editado / compra
    // Esto permite clasificar comportamiento operacional 📊

    private String descripcion;
    // Mensaje descriptivo, ejemplo: "Se creó tanque X".
    // Aporta storytelling para auditorías internas 📘😎

    private long timestamp;
    // Marca de tiempo exacta del evento.
    // Usamos System.currentTimeMillis() → precisión intergaláctica ⏱️🚀

    public AccionLog() {}
    // Constructor vacío requerido por Firebase para deserializar automáticamente 🔄

    public AccionLog(String tipo, String descripcion) {
        // Constructor corporativo para crear logs listos para el comité de crisis 😄

        this.id = java.util.UUID.randomUUID().toString();
        // Generamos un ID único tipo UUID.
        // Nada de duplicados en esta operación, señor. 🎯

        this.tipo = tipo;
        // Guardamos el tipo de acción.
        // Métrica útil para segmentar comportamiento del usuario 🧩

        this.descripcion = descripcion;
        // Guardamos la descripción que literalmente cuenta “la historia del suceso” 📜

        this.timestamp = System.currentTimeMillis();
        // Registramos el instante exacto del evento.
        // Perfecto para trazabilidad estilo auditoría premium ⏰📑
    }

    public String getId() { return id; }
    // Retorna el ID del log. Ideal para búsquedas 🔍

    public String getTipo() { return tipo; }
    // Retorna el tipo de operación (creado, eliminado…).
    // Permite filtrar o aplicar colores semáforo 🟥🟨🟩

    public String getDescripcion() { return descripcion; }
    // Retorna la narrativa del evento.
    // Un CRM interno para tanques de agua 😆💧

    public long getTimestamp() { return timestamp; }
    // Retorna el timestamp.
    // Base para ordenar cronológicamente el historial y detectar patrones temporales 📈
}
