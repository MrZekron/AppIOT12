package com.example.appiot12.controller;
// 🧠 Controlador de Tanques de Agua
// Maneja lógica de negocio (NO UI, NO Firebase Auth)

import com.example.appiot12.model.TanqueAgua;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 🚰 TanqueAguaController
 *
 * Explicado fácil 👶:
 * 👉 Aquí se crean tanques
 * 👉 Se guardan en Firebase
 * 👉 Se guardan en memoria
 * 👉 Se pueden buscar después
 */
public class TanqueAguaController {

    // 📦 Cache en memoria (rápido)
    private static final List<TanqueAgua> listaTanques = new ArrayList<>();

    // ☁️ Firebase (tanques del usuario)
    private static final DatabaseReference ref =
            FirebaseDatabase.getInstance().getReference("tanques");

    /**
     * ➕ Agregar tanque nuevo
     *
     * @param nombre        Nombre del tanque
     * @param capacidad     Capacidad en litros (String validado)
     * @param color         Color del tanque
     * @param direccion     Dirección (puede ser null)
     * @param idDispositivo ID del dispositivo asociado
     * @return mensaje de resultado
     */
    public static String addTanque(String nombre,
                                   String capacidad,
                                   String color,
                                   String direccion,
                                   String idDispositivo) {

        // 🆔 ID único
        String idTanque = UUID.randomUUID().toString();

        // 💧 Crear tanque (constructor CORRECTO)
        TanqueAgua tanque = new TanqueAgua(
                idTanque,
                nombre,
                capacidad,
                color,
                direccion,
                idDispositivo
        );

        // ☁️ Guardar en Firebase
        ref.child(idTanque).setValue(tanque);

        // 💾 Guardar en memoria
        listaTanques.add(tanque);

        return "Tanque agregado correctamente ✅";
    }

    // =====================================================
    // 🔍 BUSCAR TANQUE POR NOMBRE (SOLO MEMORIA)
    // =====================================================
    public static TanqueAgua findTanqueByNombre(String nombre) {

        if (nombre == null) return null;

        for (TanqueAgua t : listaTanques) {
            if (nombre.equalsIgnoreCase(t.getNombre())) {
                return t;
            }
        }
        return null;
    }
}
