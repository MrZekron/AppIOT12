package com.example.appiot12;
// 📦 El modelo que representa un tanque dentro del ecosistema AguaSegura.
// Este objeto NO almacena sensores ni estados del agua: para eso existe Dispositivo.
// Aquí solo vive la metadata del tanque + su idDispositivo (si existe).

/**
 * ⭐ MODELO DE TANQUE DE AGUA ⭐
 *
 * Contiene:
 *   ✔ idTanque        → Identificador único en Firebase
 *   ✔ nombre          → Nombre asignado por el usuario
 *   ✔ capacidad       → Capacidad en litros (string para flexibilidad)
 *   ✔ color           → Color físico del tanque (blanco, azul, negro…)
 *   ✔ idDispositivo   → ID del dispositivo asociado o null si no tiene
 *
 * Este modelo es simple, limpio y directo.
 * Funciona como "contendor lógico" dentro del sistema.
 */
public class TanqueAgua {

    // ============================
    // 🔑 CAMPOS DEL MODELO
    // ============================

    private String idTanque;      // 🆔 Clave única en Firebase
    private String nombre;        // 🏷 Nombre amigable del tanque
    private String capacidad;     // 💧 Capacidad total (texto por flexibilidad)
    private String color;         // 🎨 Color físico del tanque (blanco/negro/azul)

    // ⭐ SOLO referencia al dispositivo asociado
    //    null → no tiene dispositivo asignado
    private String idDispositivo;

    // ============================
    // 🧱 CONSTRUCTOR VACÍO
    // Obligatorio para Firebase
    // ============================
    public TanqueAgua() {}

    // ============================
    // 🏗 CONSTRUCTOR COMPLETO
    // ============================
    public TanqueAgua(String idTanque, String nombre, String capacidad,
                      String color, String idDispositivo) {

        this.idTanque = idTanque;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
        this.idDispositivo = idDispositivo; // puede ser null
    }

    // ============================
    // 📌 GETTERS & SETTERS
    // ============================

    public String getIdTanque() { return idTanque; }
    public void setIdTanque(String idTanque) { this.idTanque = idTanque; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCapacidad() { return capacidad; }
    public void setCapacidad(String capacidad) { this.capacidad = capacidad; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String idDispositivo) { this.idDispositivo = idDispositivo; }

    // ============================
    // 🧩 UTILIDAD DE DEPURACIÓN
    // ============================
    @Override
    public String toString() {
        // Lo que se muestra cuando el tanque aparece en un Spinner o debug log
        return nombre != null ? nombre : "Tanque sin nombre";
    }
}
