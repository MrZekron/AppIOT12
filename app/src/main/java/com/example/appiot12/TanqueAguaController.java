package com.example.appiot12;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TanqueAguaController {

    private static ArrayList<TanqueAgua> listaTanques = new ArrayList<>();

    // ⭐ TANQUES DEL USUARIO ACTUAL
    private static DatabaseReference getUserTanquesRef() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");
    }

    // ⭐ DISPOSITIVOS GLOBALES
    private static DatabaseReference getDispositivosRef() {
        return FirebaseDatabase.getInstance()
                .getReference("dispositivos");
    }

    // ============================================================
    //    AGREGAR TANQUE CON DISPOSITIVO ASOCIADO
    // ============================================================
    public static String addTanque(String nombre,
                                   String capacidad,
                                   String color,
                                   String idDispositivo) {

        // Validar nombre duplicado
        for (TanqueAgua tanque : listaTanques) {
            if (tanque.getNombre().equalsIgnoreCase(nombre)) {
                return "Error: Ya existe un tanque con ese nombre.";
            }
        }

        // Validar que el dispositivo exista
        DatabaseReference dispRef = getDispositivosRef().child(idDispositivo);

        // No podemos acceder sincrónicamente a Firebase,
        // así que solo verificamos que el ID no esté vacío:
        if (idDispositivo == null || idDispositivo.trim().isEmpty()) {
            return "Error: ID de dispositivo inválido.";
        }

        // Crear tanque
        TanqueAgua t = new TanqueAgua();
        t.setNombre(nombre);
        t.setCapacidad(capacidad);
        t.setColor(color);
        t.setIdDispositivo(idDispositivo);

        // Generar ID
        DatabaseReference ref = getUserTanquesRef();
        String idTanque = ref.push().getKey();
        t.setIdTanque(idTanque);

        // Guardar tanque
        ref.child(idTanque).setValue(t);

        // Agregar a memoria
        listaTanques.add(t);

        return "Tanque agregado exitosamente: " + nombre;
    }

    // ============================================================
    //    BUSCAR TANQUE POR NOMBRE
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
    //    EDITAR TANQUE
    // ============================================================
    public static String updateTanque(String idTanque,
                                      String nombre,
                                      String capacidad,
                                      String color) {

        TanqueAgua tanque = null;

        for (TanqueAgua t : listaTanques) {
            if (t.getIdTanque().equals(idTanque)) {
                tanque = t;
                break;
            }
        }

        if (tanque == null) return "Error: Tanque no encontrado.";

        tanque.setNombre(nombre);
        tanque.setCapacidad(capacidad);
        tanque.setColor(color);

        getUserTanquesRef()
                .child(idTanque)
                .setValue(tanque);

        return "Tanque actualizado: " + nombre;
    }

    // ============================================================
    //    ELIMINAR TANQUE
    // ============================================================
    public static void eliminarTanque(String idTanque) {

        TanqueAgua eliminar = null;

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
    //    LISTA LOCAL DE TANQUES
    // ============================================================
    public static List<TanqueAgua> getListaTanques() {
        return listaTanques;
    }
}
