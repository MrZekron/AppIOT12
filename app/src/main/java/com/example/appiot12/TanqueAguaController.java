package com.example.appiot12;
// 📦 Controlador lógico de tanques.
// Es como el “jefe de bodega” que sabe qué tanques existen y dónde están 🧠💧

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * ⭐ CONTROLADOR DE TANQUES ⭐
 *
 * Qué hace este controlador:
 * ✔ Crea tanques
 * ✔ Edita tanques
 * ✔ Elimina tanques
 * ✔ Guarda cambios en Firebase ☁️
 * ✔ Mantiene una lista local en memoria 🧠
 *
 * Qué NO hace:
 * ❌ No escucha Firebase en tiempo real
 * ❌ No valida sensores (eso es de Dispositivo)
 *
 * Piensa en él como una libreta de trabajo ✏️
 */
public class TanqueAguaController {

    // 🧠 Lista LOCAL en memoria (cache rápida, no automática)
    private static final List<TanqueAgua> listaTanques = new ArrayList<>();

    // ============================================================
    // 🔐 OBTENER UID DEL USUARIO ACTUAL (SEGURO)
    // ============================================================
    private static String getUidSeguro() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null; // 🚫 No hay usuario logueado
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // ============================================================
    // 📍 RUTA: /usuarios/{uid}/tanques
    // ============================================================
    private static DatabaseReference getUserTanquesRef() {

        String uid = getUidSeguro();

        if (uid == null) {
            return null; // 🚫 Evita crash
        }

        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");
    }

    // ============================================================
    // ➕ AGREGAR TANQUE NUEVO
    // ============================================================
    public static String addTanque(String nombre,
                                   String capacidad,
                                   String color,
                                   String idDispositivo) {

        // 🛑 Validaciones básicas (para niños 👶)
        if (nombre == null || nombre.trim().isEmpty()) {
            return "Error: el nombre está vacío ❌";
        }

        if (idDispositivo == null || idDispositivo.trim().isEmpty()) {
            return "Error: el tanque debe tener un dispositivo 📡";
        }

        // 🚫 Evitar nombres duplicados (en memoria)
        if (findTanqueByNombre(nombre) != null) {
            return "Error: ya existe un tanque con ese nombre 🛑";
        }

        DatabaseReference ref = getUserTanquesRef();

        if (ref == null) {
            return "Error: usuario no autenticado 🔐";
        }

        // 🆔 Firebase genera ID único
        String idTanque = ref.push().getKey();

        if (idTanque == null) {
            return "Error al generar ID del tanque ❌";
        }

        // 🏗 Crear tanque
        TanqueAgua tanque = new TanqueAgua(
                idTanque,
                nombre,
                capacidad,
                color,
                idDispositivo
        );

        // ☁️ Guardar en Firebase
        ref.child(idTanque).setValue(tanque);

        // 🧠 Guardar en cache local
        listaTanques.add(tanque);

        return "Tanque agregado correctamente ✅";
    }

    // ============================================================
    // 🔍 BUSCAR TANQUE POR NOMBRE (SOLO MEMORIA)
    // ============================================================
    public static TanqueAgua findTanqueByNombre(String nombre) {

        if (nombre == null) return null;

        for (TanqueAgua t : listaTanques) {
            if (nombre.equalsIgnoreCase(t.getNombre())) {
                return t;
            }
        }
        return null;
    }

    // ============================================================
    // 🔍 BUSCAR TANQUE POR ID (REUTILIZABLE)
    // ============================================================
    private static TanqueAgua findTanqueById(String idTanque) {

        if (idTanque == null) return null;

        for (TanqueAgua t : listaTanques) {
            if (idTanque.equals(t.getIdTanque())) {
                return t;
            }
        }
        return null;
    }

    // ============================================================
    // ✏️ EDITAR TANQUE EXISTENTE
    // ============================================================
    public static String updateTanque(String idTanque,
                                      String nombre,
                                      String capacidad,
                                      String color) {

        TanqueAgua tanque = findTanqueById(idTanque);

        if (tanque == null) {
            return "Error: tanque no encontrado 🛑";
        }

        // ✍️ Actualizar datos locales
        tanque.setNombre(nombre);
        tanque.setCapacidad(capacidad);
        tanque.setColor(color);

        DatabaseReference ref = getUserTanquesRef();

        if (ref == null) {
            return "Error: usuario no autenticado 🔐";
        }

        // ☁️ Guardar cambios en Firebase
        ref.child(idTanque).setValue(tanque);

        return "Tanque actualizado correctamente ✨";
    }

    // ============================================================
    // 🗑 ELIMINAR TANQUE
    // ============================================================
    public static String eliminarTanque(String idTanque) {

        TanqueAgua tanque = findTanqueById(idTanque);

        if (tanque == null) {
            return "Error: tanque no encontrado ❌";
        }

        // 🧠 Eliminar de memoria
        listaTanques.remove(tanque);

        DatabaseReference ref = getUserTanquesRef();

        if (ref == null) {
            return "Error: usuario no autenticado 🔐";
        }

        // ☁️ Eliminar de Firebase
        ref.child(idTanque).removeValue();

        return "Tanque eliminado correctamente 🗑️";
    }

    // ============================================================
    // 📋 OBTENER LISTA LOCAL (LECTURA)
    // ============================================================
    public static List<TanqueAgua> getListaTanques() {
        return new ArrayList<>(listaTanques); // 🔒 Copia defensiva
    }
}
