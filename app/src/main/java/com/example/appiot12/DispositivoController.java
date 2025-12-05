package com.example.appiot12; // 📦 Aquí vive este archivo dentro del proyecto

import java.util.ArrayList; // 📚 Lista dinámica para guardar objetos

// 🧠 Este controlador es como el "jefe de los dispositivos" 🤖💼
// Guarda, busca y crea dispositivos con sensores.
public class DispositivoController {

    // 🗃️ Aquí guardamos TODOS los dispositivos que existen.
    // Es como una caja llena de sensores mágicos ✨🤖
    private static ArrayList<Dispositivo> listDispositivos = new ArrayList<>();

    // ➕ Método para agregar un dispositivo a la lista
    // Recibe datos en modo "listo para cocinar": id, pH, conductividad, turbidez, ultrasonido.
    public static void addDispositivo(String id, int ph, int conductividad, int turbidez, int ultrasonido) {

        // 🍳 Creamos un nuevo dispositivo con los ingredientes enviados
        Dispositivo dispositivo = new Dispositivo(id, ph, conductividad, turbidez, ultrasonido);

        // 📥 Lo metemos dentro de la caja de dispositivos
        listDispositivos.add(dispositivo);
    }

    // 🔍 Método para encontrar un dispositivo por su ID
    // Es como decir: "¡Oye jefe! ¿Dónde está el dispositivo #3?"
    public static Dispositivo findDispositivo(String id) {

        // 🚶‍♂️ Recorremos la lista de uno en uno
        for (Dispositivo dispositivo : listDispositivos) {

            // 👀 Si encontramos uno cuyo ID coincide…
            if (dispositivo.getId().equals(id)) {
                return dispositivo; // 🎉 ¡Lo encontramos!
            }
        }

        // 😢 Si llegamos aquí, significa que NO estaba en la lista
        return null;
    }

    // 🌱 Método para llenar la lista con datos iniciales (dispositivos de muestra)
    // Ideal para pruebas, como decir: "¡Traigan varios sensores para jugar!"
    public static void fillDispositivo() {

        // 🛑 Solo rellenamos si está vacía (para no duplicar)
        if (listDispositivos.isEmpty()) {

            // 🧪🔥 Creamos varios dispositivos de prueba
            listDispositivos.add(new Dispositivo("1", 7, 500, 1, 50));
            listDispositivos.add(new Dispositivo("2", 6, 450, 15, 650));
            listDispositivos.add(new Dispositivo("3", 10, 550, 5, 1000));
            listDispositivos.add(new Dispositivo("4", 7, 520, 8, 90));
            listDispositivos.add(new Dispositivo("5", 6, 490, 11, 85));
            listDispositivos.add(new Dispositivo("6", 7, 470, 14, 75));
            listDispositivos.add(new Dispositivo("7", 10, 480, 13, 65));
            listDispositivos.add(new Dispositivo("8", 7, 530, 9, 95));
            listDispositivos.add(new Dispositivo("9", 6, 460, 16, 55));
            listDispositivos.add(new Dispositivo("10", 7, 500, 10, 100));

            // 🎉 Ahora tenemos 10 dispositivos listos para trabajar
        }
    }
}
