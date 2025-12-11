package com.example.appiot12;
// 📦 Controlador encargado de la administración lógica de los tanques.
// Maneja creación, edición, eliminación y una lista local en memoria.

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * ⭐ CONTROLADOR DE TANQUES ⭐
 *
 * Actúa como capa lógica "cliente" para:
 *   ✔ Crear tanques
 *   ✔ Editarlos
 *   ✔ Eliminarlos
 *   ✔ Guardarlos en Firebase
 *   ✔ Llevar una lista local de trabajo (no sincronizada)
 *
 * IMPORTANTE:
 *   - No escucha Firebase automáticamente.
 *   - Funciona como cache temporal.
 *   - Actividades como Lista cargan datos directamente desde Firebase.
 */
public class TanqueAguaController {

    // Lista LOCAL en memoria (cache rápida)
    private static ArrayList<TanqueAgua> listaTanques = new ArrayList<>();

    // ============================================================
    //   RUTA: TANQUES DEL USUARIO ACTUAL
    // ============================================================
    private static DatabaseReference getUserTanquesRef() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");
    }

    // ============================================================
    //   RUTA CORRECTA: DISPOSITIVOS DEL USUARIO ACTUAL
    // ============================================================
    private static DatabaseReference getUserDispositivosRef() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos");
    }

    // ============================================================
    //    AGREGAR TANQUE CON DISPOSITIVO ASOCIADO
    // ============================================================
    public static String addTanque(String nombre,
                                   String capacidad,
                                   String color,
                                   String idDispositivo) {

        // 🚫 Validar nombres duplicados en la cache local
        for (TanqueAgua tanque : listaTanques) {
            if (tanque.getNombre().equalsIgnoreCase(nombre)) {
                return "Error: Ya existe un tanque con ese nombre.";
            }
        }

        // 🚫 Validar ID de dispositivo
        if (idDispositivo == null || idDispositivo.trim().isEmpty()) {
            return "Error: ID de dispositivo inválido.";
        }

        // ⚠ NO podemos verificar la existencia del dispositivo en Firebase aquí
        // porque Firebase es asíncrono. La verificación real ocurre en UI.

        // Crear tanque nuevo
        TanqueAgua t = new TanqueAgua();
        t.setNombre(nombre);
        t.setCapacidad(capacidad);
        t.setColor(color);
        t.setIdDispositivo(idDispositivo);

        // Generar ID con push()
        DatabaseReference ref = getUserTanquesRef();
        String idTanque = ref.push().getKey(); // Firebase autogenera clave única
        t.setIdTanque(idTanque);

        // Guardar en Firebase
        ref.child(idTanque).setValue(t);

        // Guardar en memoria
        listaTanques.add(t);

        return "Tanque agregado exitosamente: " + nombre;
    }

    // ============================================================
    //    BUSCAR TANQUE POR NOMBRE (solo en cache)
    // ============================================================
    public static TanqueAgua findTanque(String nombre) {
        for (TanqueAgua t : listaTanques) {
            if (t.getNombre().equalsIgnoreCase(nombre)) {
                return t;
            }
        }
        return null;
    }

    // ============================================================
    //    EDITAR TANQUE (local + Firebase)
    // ============================================================
    public static String updateTanque(String idTanque,
                                      String nombre,
                                      String capacidad,
                                      String color) {

        TanqueAgua tanque = null;

        // Buscar en la cache local
        for (TanqueAgua t : listaTanques) {
            if (t.getIdTanque().equals(idTanque)) {
                tanque = t;
                break;
            }
        }

        if (tanque == null) {
            return "Error: Tanque no encontrado en memoria.";
        }

        // Actualizar objeto local
        tanque.setNombre(nombre);
        tanque.setCapacidad(capacidad);
        tanque.setColor(color);

        // Guardar en Firebase
        getUserTanquesRef()
                .child(idTanque)
                .setValue(tanque);

        return "Tanque actualizado: " + nombre;
    }

    // ============================================================
    //    ELIMINAR TANQUE (local + Firebase)
    // ============================================================
    public static void eliminarTanque(String idTanque) {

        TanqueAgua eliminar = null;

        // Buscar en la cache
        for (TanqueAgua t : listaTanques) {
            if (t.getIdTanque().equals(idTanque)) {
                eliminar = t;
                break;
            }
        }

        if (eliminar != null) {
            listaTanques.remove(eliminar);
        }

        // Eliminar en Firebase
        getUserTanquesRef().child(idTanque).removeValue();
    }

    // ============================================================
    //    LISTA LOCAL SINCRONIZADA MANUALMENTE
    // ============================================================
    public static List<TanqueAgua> getListaTanques() {
        return listaTanques;
    }
}
