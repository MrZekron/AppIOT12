package com.example.appiot12;
// 📦 Paquete del proyecto Agua Segura.
// Aquí viven los modelos de datos, ordenados y sin caos 🗂️💧

import java.util.UUID;
// 🔑 Usamos UUID para generar identificadores únicos (sin choques, sin estrés)

/**
 * 🧾 AccionLog
 *
 * Esta clase representa una acción realizada dentro de la app.
 * Cada vez que el usuario hace algo importante, se guarda un AccionLog.
 *
 * En palabras simples:
 * 👉 Es el “diario de vida” del sistema 📔🙂
 */
public class AccionLog {

    // 🆔 Identificador único del registro
    private String id;

    // 🏷️ Tipo de acción (CREAR, EDITAR, ELIMINAR, COMPRA, etc.)
    private String tipo;

    // 📝 Descripción de lo que pasó
    private String descripcion;

    // ⏰ Momento exacto en que ocurrió la acción (en milisegundos)
    private long timestamp;

    /**
     * 🔄 Constructor vacío
     *
     * Firebase lo necesita para poder:
     * 👉 Leer los datos desde la nube
     * 👉 Convertirlos en un objeto AccionLog
     *
     * Aunque no haga nada, es MUY importante ⚠️
     */
    public AccionLog() {
        // Firebase trabaja en silencio aquí 🤫☁️
    }

    /**
     * 🛠️ Constructor principal
     *
     * Se usa cuando queremos crear un nuevo registro de acción.
     *
     * @param tipo        tipo de acción realizada
     * @param descripcion explicación corta de lo ocurrido
     */
    public AccionLog(String tipo, String descripcion) {

        // 🔑 Generamos un ID único automáticamente
        this.id = UUID.randomUUID().toString();

        // 🏷️ Guardamos el tipo de acción
        this.tipo = tipo;

        // 📝 Guardamos la descripción del evento
        this.descripcion = descripcion;

        // ⏱️ Guardamos el momento exacto en que ocurrió
        this.timestamp = System.currentTimeMillis();
    }

    // =========================
    // 📤 MÉTODOS GET (lectura)
    // =========================

    /**
     * 🆔 Devuelve el ID del registro
     */
    public String getId() {
        return id;
    }

    /**
     * 🏷️ Devuelve el tipo de acción
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * 📝 Devuelve la descripción del evento
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * ⏰ Devuelve el momento en que ocurrió la acción
     */
    public long getTimestamp() {
        return timestamp;
    }
}
