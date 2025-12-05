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

    public static String addTanque(String nombre,
                                   String capacidad,
                                   String color,
                                   String idDispositivo,
                                   String estadoPH,
                                   String estadoConductividad,
                                   String estadoTurbidez) {

        // verificar nombre repetido
        for (TanqueAgua tanque : listaTanques) {
            if (tanque.getNombre().equalsIgnoreCase(nombre)) {
                return "Error: Ya existe un tanque con ese nombre.";
            }
        }

        // buscar dispositivo real
        Dispositivo dispositivo = DispositivoController.findDispositivo(idDispositivo);
        if (dispositivo == null) {
            return "Error: Dispositivo no encontrado.";
        }

        // ahora los estados se guardan en el dispositivo, no en el tanque
        dispositivo.setEstadoPH(estadoPH);
        dispositivo.setEstadoConductividad(estadoConductividad);
        dispositivo.setEstadoTurbidez(estadoTurbidez);

        // crear tanque
        TanqueAgua t = new TanqueAgua();
        t.setNombre(nombre);
        t.setCapacidad(capacidad);
        t.setColor(color);
        t.setDispositivo(dispositivo);

        // guardar ID
        DatabaseReference ref = getUserTanquesRef();
        String idTanque = ref.push().getKey();
        t.setIdTanque(idTanque);

        // guardar en firebase
        ref.child(idTanque).setValue(t);

        // guardar local
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

        // solo actualiza datos del tanque, no del dispositivo
        getUserTanquesRef()
                .child(idTanque)
                .setValue(tanque);

        return "Tanque actualizado: " + nombre;
    }

    public static void eliminarTanque(String idTanque) {
        getUserTanquesRef().child(idTanque).removeValue();
        listaTanques.removeIf(t -> t.getIdTanque().equals(idTanque));
    }

    public static List<TanqueAgua> getListaTanques() {
        return listaTanques;
    }
}
