package com.example.appiot12;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TanqueAguaController {

    private static ArrayList<TanqueAgua> listaTanques = new ArrayList<>();

    private static DatabaseReference getUserTanquesRef() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("tanques");
    }

    private static DatabaseReference getUserDispositivosRef() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(uid)
                .child("dispositivos");
    }

    // 🔥 NUEVO: agregar tanque con idDispositivo
    public static String addTanque(String nombre,
                                   String capacidad,
                                   String color,
                                   String idDispositivo) {

        // verificar nombre repetido
        for (TanqueAgua tanque : listaTanques) {
            if (tanque.getNombre().equalsIgnoreCase(nombre)) {
                return "Error: Ya existe un tanque con ese nombre.";
            }
        }

        // verificar dispositivo existe
        Dispositivo dispositivo = DispositivoController.findDispositivo(idDispositivo);
        if (dispositivo == null) {
            return "Error: Dispositivo no encontrado.";
        }

        // verificar que el dispositivo esté libre
        if (dispositivo.getIdTanque() != null) {
            return "Error: Este dispositivo ya está asociado a un tanque.";
        }

        // crear tanque
        TanqueAgua t = new TanqueAgua();
        t.setNombre(nombre);
        t.setCapacidad(capacidad);
        t.setColor(color);
        t.setIdDispositivo(idDispositivo); // ⭐ ahora solo el ID

        // crear ID en Firebase
        DatabaseReference ref = getUserTanquesRef();
        String idTanque = ref.push().getKey();
        t.setIdTanque(idTanque);

        // guardar tanque
        ref.child(idTanque).setValue(t);

        // 🔥 asociar tanque → dispositivo
        dispositivo.setIdTanque(idTanque);
        getUserDispositivosRef()
                .child(idDispositivo)
                .child("idTanque")
                .setValue(idTanque);

        // agregar local
        listaTanques.add(t);

        return "Tanque agregado exitosamente: " + nombre;
    }

    public static TanqueAgua findTanque(String nombre) {
        for (TanqueAgua t : listaTanques) {
            if (t.getNombre().equalsIgnoreCase(nombre)) {
                return t;
            }
        }
        return null;
    }

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

    // 🔥 NUEVO: liberar dispositivo al borrar tanque
    public static void eliminarTanque(String idTanque) {

        TanqueAgua eliminar = null;

        for (TanqueAgua t : listaTanques) {
            if (t.getIdTanque().equals(idTanque)) {
                eliminar = t;
                break;
            }
        }

        if (eliminar != null) {
            String idDispositivo = eliminar.getIdDispositivo();

            // liberar dispositivo
            if (idDispositivo != null) {
                getUserDispositivosRef()
                        .child(idDispositivo)
                        .child("idTanque")
                        .setValue(null);
            }

            listaTanques.remove(eliminar);
        }

        // eliminar tanque en firebase
        getUserTanquesRef().child(idTanque).removeValue();
    }

    public static List<TanqueAgua> getListaTanques() {
        return listaTanques;
    }
}
